import org.jetbrains.kotlinx.dl.api.inference.loaders.ONNXModelHub
import org.jetbrains.kotlinx.dl.api.inference.objectdetection.DetectedObject
import org.jetbrains.kotlinx.dl.api.inference.onnx.ONNXModels
import org.jetbrains.kotlinx.dl.api.inference.onnx.objectdetection.SSDObjectDetectionModel
import org.jetbrains.kotlinx.dl.dataset.image.ColorOrder
import org.jetbrains.kotlinx.dl.dataset.preprocessor.*
import org.jetbrains.kotlinx.dl.dataset.preprocessor.image.resize
import java.io.File

class ObjectDetection(private val cacheDirectory: String = "cache/pretrainedModels") {

    fun objectDetection(imageFile:File): List<DetectedObject> {
        val modelHub =
            ONNXModelHub(cacheDirectory = File(cacheDirectory))
        val model = ONNXModels.ObjectDetection.SSD.pretrainedModel(modelHub)

        model.use { detectionModel ->
            println(detectionModel)

            //val imageFile = getFileFromResource("detection/image2.png")
            val detectedObjects =
                detectionModel.detectObjects(imageFile = imageFile, topK = 1000)

            detectedObjects.forEach {
                println("Found ${it.classLabel} with probability ${it.probability}")
            }
            return detectedObjects
        }
    }

    private fun detectObjects(imageFile: File, topK: Int = 5): List<DetectedObject> {
        val preprocessing: Preprocessing = preprocess {
            load {
                pathToData = imageFile
                imageShape = ImageShape(224, 224, 3)
                colorMode = ColorOrder.BGR
            }
            transformImage {
                resize {
                    outputHeight = 1200
                    outputWidth = 1200
                }
            }
        }

        val (data, shape) = preprocessing()

        val preprocessedData = ONNXModels.ObjectDetection.SSD.preprocessInput(
            data,
            longArrayOf(shape.width!!, shape.height!!, shape.channels) // TODO: refactor to the imageShape
        )

        return SSDObjectDetectionModel().detectObjects(preprocessedData, topK)
    }
}