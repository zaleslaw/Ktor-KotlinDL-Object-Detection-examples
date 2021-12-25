import java.io.File
import java.net.URISyntaxException
import java.net.URL

@Throws(URISyntaxException::class)
fun getFileFromResource(fileName: String): File {
    val classLoader: ClassLoader = object {}.javaClass.classLoader
    val resource: URL? = classLoader.getResource(fileName)
    return if (resource == null) {
        throw IllegalArgumentException("File not found! $fileName")
    } else {
        File(resource.toURI())
    }
}
