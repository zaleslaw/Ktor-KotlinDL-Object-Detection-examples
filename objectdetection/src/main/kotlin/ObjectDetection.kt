import org.jetbrains.kotlinx.dl.api.inference.loaders.ONNXModelHub
import org.jetbrains.kotlinx.dl.api.inference.objectdetection.DetectedObject
import org.jetbrains.kotlinx.dl.api.inference.onnx.ONNXModels
import org.jetbrains.kotlinx.dl.dataset.image.ColorMode
import org.jetbrains.kotlinx.dl.dataset.preprocessor.*
import org.jetbrains.kotlinx.dl.dataset.preprocessor.image.convert
import org.jetbrains.kotlinx.dl.dataset.preprocessor.image.resize
import java.io.File
import java.io.InputStream
import kotlin.io.path.createTempFile


class ObjectDetection(private val cacheDirectory: String = "cache/pretrainedModels") {

    fun objectDetection(imageFile: InputStream): List<DetectedObject> {
        val modelHub =
            ONNXModelHub(cacheDirectory = File(cacheDirectory))
        val model = ONNXModels.ObjectDetection.SSD.pretrainedModel(modelHub)
        val modelType = ONNXModels.ObjectDetection.SSD


        model.use { detectionModel ->

            val preprocessing: Preprocessing = preprocess {
                transformImage {
                    resize {
                        outputHeight = 1200
                        outputWidth = 1200
                    }
                    convert { colorMode = ColorMode.BGR }
                }
            }


            //val image = imageFile.use { inputStream -> ImageConverter.toBufferedImage(inputStream) }

            val file: File = createTempFile().toFile()

            imageFile.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }


            val inputData = modelType.preprocessInput(
                file,
                preprocessing
            )


            val start = System.currentTimeMillis()
            val yhat = detectionModel.predictRaw(inputData)// { output -> output.getFloatArray(0) }
            val end = System.currentTimeMillis()
            println("Prediction took ${end - start} ms")

            val detectedObjects =
                detectionModel.detectObjects(imageFile = file, topK = 20)

            detectedObjects.forEach {
                println("Found ${it.classLabel} with probability ${it.probability}")
            }


            val a = yhat.values.toTypedArray()

            println(yhat.values.toTypedArray().contentDeepToString())

            return detectedObjects
        }
    }

}