package com.example.usecasetest

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlin.coroutines.CoroutineContext

class UploadUseCase(
    executionContext: CoroutineContext,
    postExecutionContext: CoroutineContext
) : BaseUseCase<Upload, Nothing>(executionContext, postExecutionContext) {

    var throwError: Boolean = false

    override suspend fun run(channel: Channel<Upload>, params: Nothing?) {
        println("UseCase: Running")
        var progress = 0
        throwError = false
        repeat(11) {
            if (throwError) {
                throw Exception("Error thrown !")
            }
            val upload = Upload(
                progress = progress,
                result = if (progress == 100) {
                    "Image"
                } else {
                    null
                }
            )
            println("UseCase: Sending result")
            channel.send(upload)
            progress += 10
            delay(300)
        }
        println("UseCase: Closing channel")
        channel.close()
    }
}

data class Upload(val progress: Int, val result: String? = null)