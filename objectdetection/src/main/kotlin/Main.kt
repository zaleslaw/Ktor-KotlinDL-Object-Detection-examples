import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication

@Composable
@Preview
fun App(viewModel: ObjectDetectionViewModel = ObjectDetectionViewModel()) {
    val imageName = "detection/image2.png"

    val viewState by viewModel.uiState.collectAsState(
        ObjectDetectionUiState.Beginning(),
        viewModel.viewModelScope.coroutineContext
    )


    LaunchedEffect(true) {
        viewModel.detect(getFileFromResource(imageName))
    }


    MaterialTheme {
        Column {
            Image(
                painterResource(imageName),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            println("YYYYYYY")
            val text = when (viewState.detectionState) {
                DetectionUiStateType.BEGINNING -> "Starting"
                DetectionUiStateType.LOADING -> "Loading"
                DetectionUiStateType.DETECTING -> "Detecting"
                DetectionUiStateType.DONE -> "Done"
            }
            Text(text)
        }
    }
}

fun main() = singleWindowApplication(
    title = "Object detection",
    state = WindowState(width = 1280.dp, height = 768.dp),
    icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
) {
    App()
}
