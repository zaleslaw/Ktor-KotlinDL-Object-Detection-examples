import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.kotlinx.dl.api.inference.objectdetection.DetectedObject
import java.io.File
import java.io.InputStream

sealed interface ObjectDetectionUiState {

    val detectionState: DetectionUiStateType
    val detectedObjects: List<DetectedObject>

    data class Beginning(
        override val detectionState: DetectionUiStateType = DetectionUiStateType.BEGINNING,
        override val detectedObjects: List<DetectedObject> = emptyList(),
    ) : ObjectDetectionUiState

    data class Loading(
        override val detectionState: DetectionUiStateType = DetectionUiStateType.LOADING,
        override val detectedObjects: List<DetectedObject> = emptyList(),
    ) : ObjectDetectionUiState

    data class Detecting(
        override val detectionState: DetectionUiStateType = DetectionUiStateType.DETECTING,
        override val detectedObjects: List<DetectedObject> = emptyList(),
    ) : ObjectDetectionUiState

    data class Detected(
        override val detectionState: DetectionUiStateType = DetectionUiStateType.DONE,
        override val detectedObjects: List<DetectedObject>,
    ) : ObjectDetectionUiState
}

enum class DetectionUiStateType {
    BEGINNING,
    LOADING,
    DETECTING,
    DONE
}

data class ObjectDetectionState(
    val detectedObjects: List<DetectedObject> = emptyList(),
    val stateType: DetectionUiStateType = DetectionUiStateType.BEGINNING
) {

    fun toUiState(): ObjectDetectionUiState = when (stateType) {
        DetectionUiStateType.BEGINNING -> ObjectDetectionUiState.Beginning()
        DetectionUiStateType.LOADING -> ObjectDetectionUiState.Loading()
        DetectionUiStateType.DETECTING -> ObjectDetectionUiState.Detecting()
        DetectionUiStateType.DONE -> ObjectDetectionUiState.Detected(detectedObjects = detectedObjects)
    }

}

class ObjectDetectionViewModel {

    val viewModelScope: CoroutineScope = GlobalScope

    private val viewModelState = MutableStateFlow(ObjectDetectionState())

    // UI state exposed to the UI
    val uiState = viewModelState
        .map { it.toUiState() }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )

    suspend fun detect(file: InputStream) {
        val detector = ObjectDetection()
        viewModelState.value = ObjectDetectionState(emptyList(), DetectionUiStateType.DETECTING)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val detectedObject = detector.objectDetection(file)
                viewModelState.value = ObjectDetectionState(detectedObject, DetectionUiStateType.DONE)
            }
        }
    }
}