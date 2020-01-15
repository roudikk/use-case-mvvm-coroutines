package com.example.usecasetest

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import java.lang.IllegalStateException
import kotlin.coroutines.CoroutineContext

class TaskUseCase(
    executionContext: CoroutineContext,
    postExecutionContext: CoroutineContext
) : BaseUseCase<BaseUseCase.TaskCompletion, Nothing>(executionContext, postExecutionContext) {

    override suspend fun run(channel: Channel<TaskCompletion>, params: Nothing?) {
        delay(2000)
        channel.send(TaskCompletion())
        channel.close()
    }
}