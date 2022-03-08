import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import org.jetbrains.kotlinx.dl.api.inference.objectdetection.DetectedObject

@Composable
@Preview
fun App(viewModel: ObjectDetectionViewModel = ObjectDetectionViewModel()) {
    val imageName = "detection/image2.png"

    val bitmap = loadImageBitmap(getFileFromResource(imageName).inputStream())

    val viewState by viewModel.uiState.collectAsState(
        ObjectDetectionUiState.Beginning(),
        viewModel.viewModelScope.coroutineContext
    )


    LaunchedEffect(true) {
        viewModel.detect(getFileFromResource(imageName))
    }


    MaterialTheme {
        Row {
            Column {
                Box {
                    Image(
                        painterResource(imageName),
                        contentDescription = null,
                        contentScale = ContentScale.None,
                        modifier = Modifier.size(bitmap.width.dp, bitmap.height.dp)
                    )
                    if (viewState.detectionState == DetectionUiStateType.DONE) {
                        DetectedObjectCanvas(
                            viewState.detectedObjects,
                            Size(bitmap.width.toFloat(), bitmap.height.toFloat())
                        )
                    }
                }
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
}

@Composable
fun DetectedObjectCanvas(
    objects: List<DetectedObject>,
    bufferedImage: Size,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier,
        onDraw = {
            // Head
            objects.forEach { obj ->
                val color = when (obj.classLabel) {
                    "dog" -> Color.Blue
                    "car" -> Color.Green
                    "clock" -> Color.Yellow
                    "bicycle" -> Color.Magenta
                    "person" -> Color.Red
                    else -> Color.White
                }
                val top = obj.yMin * bufferedImage.height
                val left = obj.xMin * bufferedImage.width
                val bottom = obj.yMax * bufferedImage.height
                val right = obj.xMax * bufferedImage.width

                if (color != Color.White) {
                    drawLine(Color.Blue, Offset(left, top), Offset(right, top))
                    drawLine(Color.Green, Offset(right, top), Offset(right, bottom))
                    drawLine(Color.Yellow, Offset(right, bottom), Offset(left, bottom))
                    drawLine(Color.Red, Offset(left, bottom), Offset(left, top))
                }
            }
        }
    )
}


fun main() = singleWindowApplication(
    title = "Object detection",
    state = WindowState(width = 1280.dp, height = 768.dp),
    icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
) {
    App()
}
