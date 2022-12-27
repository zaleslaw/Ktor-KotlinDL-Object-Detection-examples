import java.io.File
import java.io.InputStream
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

fun getFileFromResourceAsStream(fileName: String): InputStream {

    // The class loader that loaded the class
    val classLoader: ClassLoader = object {}.javaClass.classLoader
    return classLoader.getResourceAsStream(fileName)
}
