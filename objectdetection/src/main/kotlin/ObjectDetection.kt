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

            val detectedObjects =
                detectionModel.detectObjects(imageFile = imageFile, topK = 5)

            detectedObjects.forEach {
                println("Found ${it.classLabel} with probability ${it.probability}")
            }
            return detectedObjects
        }
    }

}