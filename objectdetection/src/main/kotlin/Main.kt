import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import widget.ImageWithDetectedObjects
import java.awt.Container
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import java.io.File


@Composable
@Preview
fun App(dropTarget: Container, viewModel: ObjectDetectionViewModel = ObjectDetectionViewModel()) {
    val imageName = "detection/image2.png"

    val viewState by viewModel.uiState.collectAsState(
        ObjectDetectionUiState.Beginning(),
        viewModel.viewModelScope.coroutineContext
    )

    LaunchedEffect(true) {
        viewModel.detect(getFileFromResourceAsStream(imageName))
    }

    val name = remember { mutableStateOf(TextFieldValue(System.getProperty("user.home"))) }

    MaterialTheme {
        Row(Modifier.onGloballyPositioned { coordinates ->
            // This will be the size of the Column.
            print("Windows size ${coordinates.size}")
        }) {
            Column {
                TextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    singleLine = true,
                    placeholder = {
                        Text("..")
                    },
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        keyboardType = KeyboardType.Uri,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(onAny = {

                    })
                )
                ImageWithDetectedObjects(
                    loadImageBitmap(File("/Users/mharakal/projects/private/ds/ObjectDetectionMH/objectdetection/src/image2.png").inputStream()),
                    viewState.detectionState,
                    viewState.detectedObjects,
                    modifier = Modifier.heightIn(min = 100.dp, max = 500.dp)
                )
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

val target = object : DropTarget() {
    @Synchronized
    override fun drop(evt: DropTargetDropEvent) {
        try {
            evt.acceptDrop(DnDConstants.ACTION_REFERENCE)
            val droppedFiles = evt
                .transferable.getTransferData(
                    DataFlavor.javaFileListFlavor
                ) as List<*>
            droppedFiles.first()?.let {
                //name.value = TextFieldValue((it as File).absolutePath)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
//dropTarget.dropTarget = target
//}


fun main() = singleWindowApplication(
    title = "Object detection",
    state = WindowState(width = 1280.dp, height = 768.dp),
    icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
) {

    App(window.contentPane)
}
