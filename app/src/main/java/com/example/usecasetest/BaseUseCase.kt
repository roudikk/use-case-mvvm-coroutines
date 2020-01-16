package com.example.usecasetest

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
abstract class BaseUseCase<T, in Params>(
    private val executionContext: CoroutineContext,
    private val postExecutionContext: CoroutineContext
) {
    private var scope = CoroutineScope(executionContext)
    private var postScope = CoroutineScope(postExecutionContext)

    protected var doBefore: () -> Unit = { }
    protected var doAfter: () -> Unit = { }
    protected var onError: (Throwable?) -> Unit = { }
    protected var onResult: (T) -> Unit = { }
    protected var onCancel: () -> Unit = { }

    private var job: Job? = null
    private var channel: ReceiveChannel<T>? = null
    private var throwable: Throwable? = null

    abstract suspend fun run(channel: SendChannel<T>, params: Params? = null)

    /**
     * [job] will run in [executionContext]
     */
    open operator fun invoke(params: Params? = null): Job? {
        if (isActive()) restart() else reset()
        job = scope.launch {
            throwable = null
            postScope.launch { doBefore() }
            launchChildJob(params)
            postChildJobCompletion()
        }
        return job
    }

    /**
     * [channel] will run in [executionContext]
     * Elements emitted from [channel] will be emitted on [postExecutionContext]
     */
    private suspend fun launchChildJob(params: Params?) {
        channel = scope.produce {
            try {
                run(channel, params)
            } catch (throwable: Throwable) {
                handleException(throwable)
            }
        }
        channel?.consumeEach {
            postScope.launch { onResult(it) }
        }
    }

    private fun postChildJobCompletion() {
        if (throwable == null) {
            postScope.launch { doAfter() }
        }
        reset()
        cleanup()
    }

    /**
     * @param throwable
     *
     * Handles the given [throwable]:
     *  - Ignores [CancellationException], use case has been cancelled internally.
     *  - Ignores [RestartCancellationException], use case has been restarted.
     *  - Calls [onCancel] on [ForcedCancellationException], use case has been cancelled
     *  - Calls [doAfter] on [ClosedSendChannelException], use case finished sending results.
     *  - Calls [onError] otherwise, use case threw an error.
     */
    private fun handleException(throwable: Throwable?) {
        this.throwable = throwable
        when (throwable) {
            is ClosedSendChannelException -> postScope.launch { doAfter() }
            is ForcedCancellationException -> postScope.launch { onCancel() }
            is RestartCancellationException -> {
                // Ignores the exception.
            }
            is CancellationException -> {
                // Ignores the exception.
            }
            else -> postScope.launch { onError(throwable) }
        }
        if (throwable !is RestartCancellationException) {
            cleanup()
        }
    }

    /**
     * Returns true if [job] is currently active.
     */
    fun isActive(): Boolean {
        return job?.isActive == true
    }

    /**
     * Sets [job], and [channel] to null.
     */
    private fun cleanup() {
        job = null
        channel = null
    }

    /**
     * Cancels the [BaseUseCase] with a [CancellationException].
     * This doesn't result in [onCancel] being called.
     */
    private fun reset() {
        job?.cancel()
        cleanup()
    }

    /**
     * Cancels the [BaseUseCase] with a [RestartCancellationException]
     * This doesn't result in [onCancel] being called.
     * [cleanup] is also not called so [KotlinNullPointerException]
     * doesn't get throw when the job is restarted.
     */
    private fun restart() {
        job?.cancel(RestartCancellationException())
    }

    /**
     * Cancels the [BaseUseCase] and throws a [ForcedCancellationException].
     * This results in [onCancel] being called.
     */
    fun cancel() {
        job?.cancel(ForcedCancellationException())
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