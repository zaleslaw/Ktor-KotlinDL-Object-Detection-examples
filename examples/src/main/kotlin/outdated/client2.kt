package outdated

import getFileFromResource
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import java.io.File

fun main() {
    runBlocking {
        val client = HttpClient(CIO)

        val response: HttpResponse = client.submitFormWithBinaryData(
            url = "http://localhost:8000/download",
            formData = formData {
                append("description", "Ktor logo")
                append("image", getFileFromResource("detection/image1.jpg").readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, "image/jpg")
                    append(HttpHeaders.ContentDisposition, "filename=image1.jpg")
                })
            }
        ) {
            onUpload { bytesSentTotal, contentLength ->
                println("Sent $bytesSentTotal bytes from $contentLength")
            }
        }
 // TODO: mkdir if not exist clientFiles
        val imageFile = File("clientFiles/image1withPredictedObjects.jpg")
        imageFile.writeBytes(response.readBytes())
    }
}
