package com.example.usecasetest

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
abstract class BaseUseCase<T, in Params>(
    private val executionContext: CoroutineContext,
    private val postExecutionContext: CoroutineContext
) {

    protected var doBefore: () -> Unit = { }
    protected var doAfter: () -> Unit = { }
    protected var onError: (Throwable?) -> Unit = { }
    protected var onResult: (T) -> Unit = { }
    protected var onCancel: () -> Unit = { }

    private var job: Job? = null
    private var childJob: Job? = null
    private var channel: Channel<T>? = null
    private var scope = CoroutineScope(executionContext)
    private var postScope = CoroutineScope(postExecutionContext)

    abstract suspend fun run(channel: Channel<T>, params: Params? = null)

    open operator fun invoke(params: Params? = null): Job {
        if (isActive()) {
            restart()
        } else {
            reset()
        }
        channel = Channel()
        job = scope.launch {
            try {
                println("UseCase: Parent job starting")
                postScope.launch { doBefore() }
                childJob = scope.launch {
                    println("UseCase: Child job starting")
                    try {
                        run(channel!!, params)
                    } catch (throwable: Throwable) {
                        handleException(throwable)
                        if (throwable !is RestartCancellationException) {
                            cleanup()
                        }
                    }
                }
                for (i in channel!!) {
                    println("UseCase: Value back")
                    postScope.launch { onResult(i) }
                }
                childJob?.join()
                postScope.launch { doAfter() }
                reset()
                cleanup()
            } catch (throwable: Throwable) {
                println("UseCase: Handling error: $throwable")
                handleException(throwable)
                if (throwable !is RestartCancellationException) {
                    cleanup()
                }
            }
        }
        return job!!
    }

    /**
     * @param exception
     *
     * Handles the given [exception]:
     *  - Ignores [CancellationException], use has been cancelled internally.
     *  - Calls [onCancel] on [ForcedCancellationException], use case ahs been cancelled
     *  - Calls [doAfter] on [ClosedSendChannelException], use case finished sending results.
     *  - Calls [onError] otherwise, use case threw an error.
     */
    private fun handleException(exception: Throwable?) {
        when (exception) {
            is ClosedSendChannelException -> postScope.launch { doAfter() }
            is ForcedCancellationException -> postScope.launch { onCancel() }
            is RestartCancellationException -> {
                // Ignores the exception.
            }
            is CancellationException -> {
                // Ignores the exception.
            }
            else -> postScope.launch { onError(exception) }
        }
    }

    /**
     * Returns true if [job] is currently active.
     */
    fun isActive(): Boolean {
        return job?.isActive == true
    }

    /**
     * Sets [job], [childJob] and [channel] to null.
     */
    private fun cleanup() {
        job = null
        childJob = null
        channel = null
    }

    /**
     * Cancels the [BaseUseCase] with a [CancellationException].
     * This doesn't result in [onCancel] being called.
     */
    fun reset() {
        job?.cancel()
        childJob?.cancel()
        channel?.close()
        cleanup()
    }

    /**
     * Cancels the [BaseUseCase] with a [RestartCancellationException]
     * This doesn't result in [onCancel] being called.
     * [cleanup] is also not called so [KotlinNullPointerException]
     * doesn't get throw when the job is restarted.
     */
    fun restart() {
        job?.cancel(RestartCancellationException())
        childJob?.cancel(RestartCancellationException())
        channel?.close()
    }

    /**
     * Cancels the [BaseUseCase] and throws a [ForcedCancellationException].
     * This results in [onCancel] being called.
     */
    fun cancel() {
        job?.cancel(ForcedCancellationException())
        childJob?.cancel(CancellationException())
        channel?.close(CancellationException())
        cleanup()
    }

    /**
     * Triggered before the execution of the task.
     *
     * Runs on [postExecutionContext]
     */
    fun doBefore(delegate: () -> Unit): (BaseUseCase<T, Params>) {
        doBefore = delegate
        return this
    }

    /**
     * Triggered after the job has completed, this is a good place
     * to clean up.
     *
     * Runs on [postExecutionContext]
     */
    fun doAfter(delegate: () -> Unit): (BaseUseCase<T, Params>) {
        doAfter = delegate
        return this
    }

    /**
     * Triggered after successfully receiving a result from
     * the job.
     *
     * Runs on [postExecutionContext]
     */
    fun onResult(delegate: (T) -> Unit): (BaseUseCase<T, Params>) {
        onResult = delegate
        return this
    }

    /**
     * Triggered when an exception has occurred.
     *
     * Runs on [postExecutionContext]
     */
    fun onError(delegate: (Throwable?) -> Unit): (BaseUseCase<T, Params>) {
        onError = delegate
        return this
    }

    /**
     * Triggered when a cancellation has occurred.
     *
     * Runs on [postExecutionContext]
     */
    fun onCancel(delegate: () -> Unit): (BaseUseCase<T, Params>) {
        onCancel = delegate
        return this
    }

    class ForcedCancellationException : CancellationException()
    class RestartCancellationException : CancellationException()

    class TaskCompletion
}