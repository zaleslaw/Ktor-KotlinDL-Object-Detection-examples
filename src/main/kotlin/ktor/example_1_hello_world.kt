package ktor

import getFileFromResource
import io.ktor.application.*
import io.ktor.server.netty.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import org.jetbrains.kotlinx.dl.api.inference.loaders.ONNXModelHub
import org.jetbrains.kotlinx.dl.api.inference.onnx.ONNXModels
import java.io.File

fun main(args: Array<String>) {
    val modelHub =
        ONNXModelHub(cacheDirectory = File("cache/pretrainedModels"))
    val model = ONNXModels.ObjectDetection.SSD.pretrainedModel(modelHub)

    embeddedServer(Netty, 8080) {
        routing {
            get("{id}") {
                val id = call.parameters["id"] ?: return@get call.respondText(
                    "Missing or malformed image id",
                    status = HttpStatusCode.BadRequest
                )

                try {
                    val imageFile = getFileFromResource("recognition/image$id.jpg")
                    val detectedObjects =
                        model.detectObjects(imageFile = imageFile, topK = 50)

                    var result = ""

                    detectedObjects.forEach {
                        result += "Found ${it.classLabel} with probability ${it.probability}\n"
                    }

                    call.respondText(result, ContentType.Text.Plain)
                } catch (e: IllegalArgumentException) {
                    return@get call.respondText(
                        "No image with id $id",
                        status = HttpStatusCode.NotFound
                    )
                }
            }
        }
    }.start(wait = true)

    model.close()
}
