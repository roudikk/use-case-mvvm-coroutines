package com.example.usecasetest

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlin.coroutines.CoroutineContext

class UploadUseCase(
    executionContext: CoroutineContext,
    postExecutionContext: CoroutineContext
) : BaseUseCase<Upload, UploadUseCase.Params>(executionContext, postExecutionContext) {

    var throwError: Boolean = false

    /**
     * [channel].send() to send out the result to the consumer
     * [channel].close() when finish sending out results.
     */
    override suspend fun run(channel: SendChannel<Upload>, params: Params?) {
        requireNotNull(params)
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
            channel.send(upload)
            progress += 10
            if (progress <= 100) {
                delay(300)
            }
        }
        channel.close()
    }

    data class Params(val sessionId: String)
}


data class Upload(val progress: Int, val result: String? = null)