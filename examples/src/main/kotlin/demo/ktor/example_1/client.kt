package demo.ktor.example_1

import getFileFromResource
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val client = HttpClient(CIO)

        val response: HttpResponse = client.submitFormWithBinaryData(
            url = "http://localhost:8000/recognize",
            formData = formData {
                append("image", getFileFromResource("recognition/image8.jpg").readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, "image/jpg")
                    append(HttpHeaders.ContentDisposition, "filename=image8.jpg")
                })
            }
        )

        println(response.readText())
    }
}
