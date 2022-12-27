package widget

import DetectionUiStateType
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import org.jetbrains.kotlinx.dl.api.inference.objectdetection.DetectedObject
import kotlin.math.roundToInt


@Composable
@Preview
fun ImageWithDetectedObjects(
    image: ImageBitmap,
    state: DetectionUiStateType,
    objects: List<DetectedObject>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val customPainter =
            OverlayImagePainter(
                image,
                objects,
            )


        Box {
            Image(
                contentDescription = null,
                contentScale = ContentScale.Fit,
                painter = customPainter
            )
        }
        if (state != DetectionUiStateType.DONE) {
            LinearProgressIndicator()
        }
    }
}

class OverlayImagePainter constructor(
    private val image: ImageBitmap,
    private val objects: List<DetectedObject>,
    private val srcOffset: IntOffset = IntOffset.Zero,
    private val srcSize: IntSize = IntSize(image.width, image.height),
) : Painter() {

    private val size: IntSize = validateSize(srcOffset, srcSize)
    override fun DrawScope.onDraw() {
        print("Real size ${size}")
        // draw the first image without any blend mode
        drawImage(
            image,
            srcOffset,
            srcSize,
            dstSize = IntSize(
                this@onDraw.size.width.roundToInt(),
                this@onDraw.size.height.roundToInt()
            )
        )

        objects
            .filter { it.probability > 0.5 }
            .forEach { obj ->
                val color = when (obj.classLabel) {
                    "dog" -> Color.Blue
                    "car" -> Color.Green
                    "clock" -> Color.Yellow
                    "bicycle" -> Color.Magenta
                    "person" -> Color.Red
                    else -> Color.White
                }
                val top = obj.yMin * size.height
                val left = obj.xMin * size.width
                val bottom = obj.yMax * size.height
                val right = obj.xMax * size.width

                if (color != Color.White) {

                    drawLine(color, Offset(left, top), Offset(right, top))
                    drawLine(color, Offset(right, top), Offset(right, bottom))
                    drawLine(color, Offset(right, bottom), Offset(left, bottom))
                    drawLine(color, Offset(left, bottom), Offset(left, top))
                }
            }
    }

    /**
     * Return the dimension of the underlying [ImageBitmap] as it's intrinsic width and height
     */
    override val intrinsicSize: Size get() = size.toSize()

    private fun validateSize(srcOffset: IntOffset, srcSize: IntSize): IntSize {
        require(
            srcOffset.x >= 0 &&
                    srcOffset.y >= 0 &&
                    srcSize.width >= 0 &&
                    srcSize.height >= 0 &&
                    srcSize.width <= image.width &&
                    srcSize.height <= image.height
        )
        return srcSize
    }
}

