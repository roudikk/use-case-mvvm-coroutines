package com.example.usecasetest

import kotlinx.coroutines.channels.SendChannel
import kotlin.coroutines.CoroutineContext

class TaskUseCase(
    executionContext: CoroutineContext,
    postExecutionContext: CoroutineContext
) : BaseUseCase<BaseUseCase.TaskCompletion, Nothing>(executionContext, postExecutionContext) {

    override suspend fun run(channel: SendChannel<TaskCompletion>, params: Nothing?) {
        var i = 0
        repeat(1000000000) {
            i++
        }
        channel.send(TaskCompletion())
        channel.close()
    }
}