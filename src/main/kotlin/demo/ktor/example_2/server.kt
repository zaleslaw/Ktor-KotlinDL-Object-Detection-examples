package demo.ktor.example_2

import io.ktor.application.*
import io.ktor.server.netty.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import org.jetbrains.kotlinx.dl.api.inference.loaders.ONNXModelHub
import org.jetbrains.kotlinx.dl.api.inference.objectdetection.DetectedObject
import org.jetbrains.kotlinx.dl.api.inference.onnx.ONNXModels
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
    val modelHub = ONNXModelHub(cacheDirectory = File("cache/pretrainedModels"))
    val model = ONNXModels.ObjectDetection.SSD.pretrainedModel(modelHub)

    embeddedServer(Netty, 8001) {
        routing {
            post("/detect") {
                val multipartData = call.receiveMultipart()
                var newFileName = ""
                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            val fileName = part.originalFileName as String
                            newFileName = fileName.replace("image", "detectedObjects")
                            val fileBytes = part.streamProvider().readBytes()
                            val imageFile = File("serverFiles/$fileName")
                            imageFile.writeBytes(fileBytes)

                            val detectedObjects =
                                model.detectObjects(imageFile = imageFile, topK = 20)

                            val filteredObjects =
                                detectedObjects.filter { it.classLabel == "car" || it.classLabel == "person" || it.classLabel == "bicycle" }

                            visualise(newFileName, imageFile, filteredObjects)

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
            }
        }
    }.start(wait = true)

    model.close()
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
