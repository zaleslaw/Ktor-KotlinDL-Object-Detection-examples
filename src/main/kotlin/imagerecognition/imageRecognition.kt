package imagerecognition

import getFileFromResource
import org.jetbrains.kotlinx.dl.api.inference.keras.loaders.TFModelHub
import org.jetbrains.kotlinx.dl.api.inference.keras.loaders.TFModels
import java.io.File

fun main() {
    val modelHub =
        TFModelHub(cacheDirectory = File("cache/pretrainedModels"))

    val model = modelHub[TFModels.CV.ResNet50] // TODO: explain about models tradeoff (accuracy/latency)

    model.use {
        for (i in 1..8) {
            val imageFile = getFileFromResource("recognition/image$i.jpg")

            val recognizedObject = it.predictObject(imageFile = imageFile)
            println(recognizedObject)
            
            val top5 = it.predictTopKObjects(imageFile = imageFile, topK = 5)
            println(top5.toString())
        }
    }
}
