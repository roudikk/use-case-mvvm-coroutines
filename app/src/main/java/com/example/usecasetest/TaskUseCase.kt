package com.example.usecasetest

import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.CoroutineContext

class TaskUseCase(
    executionContext: CoroutineContext,
    postExecutionContext: CoroutineContext
) : BaseUseCase<BaseUseCase.TaskCompletion, Nothing>(executionContext, postExecutionContext) {

    override suspend fun run(channel: Channel<TaskCompletion>, params: Nothing?) {
        var i = 0
        repeat(100) {
            i++
        }
        channel.send(TaskCompletion())
        channel.close()
    }
}