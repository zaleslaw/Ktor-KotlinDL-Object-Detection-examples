package demo.ktor.example_1

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.kotlinx.dl.api.inference.keras.loaders.TFModelHub
import org.jetbrains.kotlinx.dl.api.inference.keras.loaders.TFModels
import java.io.File

fun main(args: Array<String>) {
    val modelHub = TFModelHub(cacheDirectory = File("cache/pretrainedModels"))
    val model = modelHub[TFModels.CV.MobileNetV2]

    embeddedServer(Netty, 8000) {
        routing {
            post("/recognize") {
                val multipartData = call.receiveMultipart()

                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            val fileName = part.originalFileName as String
                            val fileBytes = part.streamProvider().readBytes()

                            val imageFile = File("serverFiles/$fileName")
                            imageFile.writeBytes(fileBytes)

                            val recognizedObject = model.predictObject(imageFile = imageFile)
                            val top5 = model.predictTopKObjects(imageFile = imageFile, topK = 5)

                            call.respondText(
                                "Recognized object: $recognizedObject\nTop-5 objects: $top5",
                                ContentType.Text.Plain
                            )
                        }
                    }
                }
            }
        }
    }.start(wait = true)

    model.close()
}
