package demo.ktor.example_2

import getFileFromResource
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import java.io.File

fun main() {
    runBlocking {
        val client = HttpClient(CIO)

        val response: HttpResponse = client.submitFormWithBinaryData(
            url = "http://localhost:8001/detect",
            formData = formData {
                append("image", getFileFromResource("detection/image2.jpg").readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, "image/jpg")
                    append(HttpHeaders.ContentDisposition, "filename=image2.jpg")
                })
            }
        )

        val imageFile = File("clientFiles/detectedObjects2.jpg")
        imageFile.writeBytes(response.readBytes())
    }
}
