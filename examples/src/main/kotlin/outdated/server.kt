package outdated

import getFileFromResource
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.server.netty.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import kotlinx.html.*
import org.jetbrains.kotlinx.dl.api.inference.loaders.ONNXModelHub
import org.jetbrains.kotlinx.dl.api.inference.objectdetection.DetectedObject
import org.jetbrains.kotlinx.dl.api.inference.onnx.ONNXModels
import org.jetbrains.kotlinx.dl.api.inference.onnx.objectdetection.SSDObjectDetectionModel
import org.jetbrains.kotlinx.dl.dataset.image.ColorOrder
import org.jetbrains.kotlinx.dl.dataset.preprocessor.*
import org.jetbrains.kotlinx.dl.dataset.preprocessor.image.resize
import toBufferedImage
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Stroke
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs

fun main(args: Array<String>) {
    val modelHub =
        ONNXModelHub(cacheDirectory = File("cache/pretrainedModels"))
    val model = ONNXModels.ObjectDetection.SSD.pretrainedModel(modelHub)

    // TODO: add image recognition API call
    embeddedServer(Netty, 8000) {
        routing {
            get("{id}") {
                val id = call.parameters["id"] ?: return@get call.respondText(
                    "Missing or malformed image id",
                    status = HttpStatusCode.BadRequest
                )

                try {
                    val imageFile = getFileFromResource("recognition/image$id.jpg")
                    val result = formStringWithDetectedObject(model, imageFile)

                    call.respondText(result, ContentType.Text.Plain)
                } catch (e: IllegalArgumentException) {
                    return@get call.respondText(
                        "No image with id $id",
                        status = HttpStatusCode.NotFound
                    )
                }
            }

            var fileDescription = ""
            var fileName = ""

            post("/upload") {
                val multipartData = call.receiveMultipart()

                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            fileDescription = part.value
                        }
                        is PartData.FileItem -> {
                            fileName = part.originalFileName as String
                            val fileBytes = part.streamProvider().readBytes()
                            // TODO: mkdir serverFiles if not exists
                            val imageFile = File("serverFiles/$fileName")
                            imageFile.writeBytes(fileBytes)

                            val result = formStringWithDetectedObject(model, imageFile)
                            call.respondText(result, ContentType.Text.Plain)
                        }
                    }
                }

                //call.respondText("$fileDescription is uploaded to '$fileName'")
            }

            post("/download") {
                val multipartData = call.receiveMultipart()
                // TODO: extract from part topK and classlabel (if empty detect all)
                var newFileName = ""
                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            fileDescription = part.value
                        }
                        is PartData.FileItem -> {
                            fileName = part.originalFileName as String
                            newFileName = fileName.replace(".jpg", "withPredictedObjects.jpg")
                            val fileBytes = part.streamProvider().readBytes()
                            val imageFile = File("serverFiles/$fileName")
                            imageFile.writeBytes(fileBytes)

                            val detectedObjects =
                                model.detectObjects(imageFile = imageFile, topK = 20)

                            val filteredObjects =
                                detectedObjects.filter { it.classLabel == "car" || it.classLabel == "person" || it.classLabel == "bicycle" }

                            visualise(newFileName, imageFile, filteredObjects) // todo: rename

                            val file = File("serverFiles/$newFileName")
                            call.response.header(
                                HttpHeaders.ContentDisposition,
                                ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, newFileName)
                                    .toString()
                            )
                            call.respondFile(file)
                        }
                    }
                }

               // TODO
            }

           /* post("/signup") {
                val formParameters = call.receiveParameters()
                val username = formParameters["username"].toString()
                call.respondText("The '$username' account is created")
            }*/

            get("/") {
                call.respondHtml {
                    body {
                        form(action = "/download", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                            p {
                                +"Your image: "
                                fileInput (name = "image")
                            }
                            p {
                                +"TopK: "
                                numberInput (name = "topK")
                            }
                            p {
                                +"Class to detect: "
                                textInput(name = "classLabelName")
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

private fun formStringWithDetectedObject(
    model: SSDObjectDetectionModel,
    imageFile: File
): String {
    val detectedObjects =
        model.detectObjects(imageFile = imageFile, topK = 50)

    var result = ""

    detectedObjects.forEach {
        result += "Found ${it.classLabel} with probability ${it.probability}\n"
    }
    return result
}


private fun visualise(
    fileName: String,
    imageFile: File,
    detectedObjects: List<DetectedObject>
) {
    val preprocessing: Preprocessing = preprocess {
        load {
            pathToData = imageFile
            imageShape = ImageShape(224, 224, 3)
            colorMode = ColorOrder.BGR
        }
        transformImage {
            resize {
                outputWidth = 1200
                outputHeight = 1200
            }
        }
        transformTensor {
            rescale {
                scalingCoefficient = 255f
            }
        }
    }

    val rawImage = preprocessing().first

    drawAndSaveDetectedObjects(fileName, rawImage, ImageShape(1200, 1200, 3), detectedObjects)
}

private fun drawAndSaveDetectedObjects(
    fileName: String,
    image: FloatArray,
    imageShape: ImageShape,
    detectedObjects: List<DetectedObject>
) {
    val bufferedImage = image.toBufferedImage(imageShape)

    val newGraphics = bufferedImage.createGraphics()
    newGraphics.drawImage(bufferedImage, 0, 0, null)

    detectedObjects.forEach {

        val top = it.yMin * imageShape.height!!
        val left = it.xMin * imageShape.width!!
        val bottom = it.yMax * imageShape.height!!
        val right = it.xMax * imageShape.width!!
        if (abs(top - bottom) > 400 || abs(right - left) > 400) return@forEach
        // left, bot, right, top

        // y = columnIndex
        // x = rowIndex
        val yRect = bottom
        val xRect = left

        newGraphics as Graphics2D
        val stroke1: Stroke = BasicStroke(4f)
        when (it.classLabel) {
            "person" -> newGraphics.color = Color.RED
            "bicycle" -> newGraphics.color = Color.BLUE
            "car" -> newGraphics.color = Color.GREEN
            else -> newGraphics.color = Color.MAGENTA
        }
        newGraphics.stroke = stroke1
        newGraphics.drawRect(xRect.toInt(), yRect.toInt(), (right - left).toInt(), (top - bottom).toInt())
    }

    ImageIO.write(bufferedImage, "jpg", File("serverFiles/$fileName"))
}
