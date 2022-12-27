package demo.ktor.example_3

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.*
import org.jetbrains.kotlinx.dl.api.inference.loaders.ONNXModelHub
import org.jetbrains.kotlinx.dl.api.inference.objectdetection.DetectedObject
import org.jetbrains.kotlinx.dl.api.inference.onnx.ONNXModels
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Stroke
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs

fun main(args: Array<String>) {
    val modelHub = ONNXModelHub(cacheDirectory = File("cache/pretrainedModels"))
    val model = ONNXModels.ObjectDetection.SSD.pretrainedModel(modelHub)

    embeddedServer(Netty, 8002) {
        routing {
            post("/detect") {
                val multipartData = call.receiveMultipart()
                var imageFile: File? = null
                var newFileName = ""
                var topK = 20
                val classLabels = mutableListOf<String>()
                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            val fileName = part.originalFileName as String
                            val fileBytes = part.streamProvider().readBytes()

                            newFileName = fileName.replace("image", "detectedObjects")
                            imageFile = File("serverFiles/$fileName")
                            imageFile!!.writeBytes(fileBytes)
                        }
                        is PartData.FormItem -> {
                            when (part.name) {
                                "topK" -> topK = if (part.value.isNotBlank()) part.value.toInt() else 20
                                "classLabelNames" -> part.value.split(",").forEach {
                                    classLabels += it.trim()
                                }
                            }
                        }
                        is PartData.BinaryItem -> TODO()
                    }
                }

                val detectedObjects =
                    model.detectObjects(imageFile = imageFile!!, topK = topK)

                val filteredObjects = detectedObjects.filter {
                        if (classLabels.isNotEmpty()) {
                            it.classLabel in classLabels
                        } else {
                            it.classLabel == "car" || it.classLabel == "person" || it.classLabel == "bicycle"
                        }
                    }

                drawRectanglesForDetectedObjects(newFileName, imageFile!!, filteredObjects)

                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, newFileName)
                        .toString()
                )
                call.respondFile(File("serverFiles/$newFileName"))
            }

            get("/") {
                call.respondHtml {
                    body {
                        form(action = "/detect", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                            p {
                                +"Your image: "
                                fileInput(name = "image")
                            }
                            p {
                                +"TopK: "
                                numberInput(name = "topK")
                            }
                            p {
                                +"Classes to detect: "
                                textInput(name = "classLabelNames")
                            }
                            p {
                                submitInput() { value = "Detect objects" }
                            }
                        }
                    }
                }
            }
        }
    }.start(wait = true)

    model.close()
}

private fun drawRectanglesForDetectedObjects(
    fileName: String,
    image: File,
    detectedObjects: List<DetectedObject>
) {
    val bufferedImage = ImageIO.read(image)

    val newGraphics = bufferedImage.createGraphics()
    newGraphics.drawImage(bufferedImage, 0, 0, null)

    detectedObjects.forEach {

        val top = it.yMin * bufferedImage.height
        val left = it.xMin * bufferedImage.width
        val bottom = it.yMax * bufferedImage.height
        val right = it.xMax * bufferedImage.width
        if (abs(top - bottom) > 400 || abs(right - left) > 400) return@forEach

        newGraphics as Graphics2D
        val stroke1: Stroke = BasicStroke(4f)
        when (it.classLabel) {
            "person" -> newGraphics.color = Color.RED
            "bicycle" -> newGraphics.color = Color.BLUE
            "car" -> newGraphics.color = Color.GREEN
            "traffic light" -> newGraphics.color = Color.ORANGE
            "train" -> newGraphics.color = Color.PINK

            else -> newGraphics.color = Color.MAGENTA
        }
        newGraphics.stroke = stroke1
        newGraphics.drawRect(left.toInt(), bottom.toInt(), (right - left).toInt(), (top - bottom).toInt())
    }

    ImageIO.write(bufferedImage, "jpg", File("serverFiles/$fileName"))
}
