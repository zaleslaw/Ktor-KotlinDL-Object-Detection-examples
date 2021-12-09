package outdated

import getFileFromResource
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.kotlinx.dl.api.inference.loaders.ONNXModelHub
import org.jetbrains.kotlinx.dl.api.inference.onnx.ONNXModels
import java.io.File

fun main() {
	val modelHub =
		ONNXModelHub(cacheDirectory = File("cache/pretrainedModels"))
	val detectionModel = ONNXModels.ObjectDetection.SSD.pretrainedModel(modelHub)

	embeddedServer(Netty, port = 8000) {
		routing {
			get("/") {
				val imageFile = getFileFromResource("datasets/vgg/image9.jpg")
				val detectedObjects =
					detectionModel.detectObjects(imageFile = imageFile, topK = 50)

				var result = ""

				detectedObjects.forEach {
					result += "Found ${it.classLabel} with probability ${it.probability}\n"
				}

				call.respondText(result)
			}
		}
	}.start(wait = true)

	detectionModel.close()
}
