/*
 * Copyright 2020 JetBrains s.r.o. and Kotlin Deep Learning project contributors. All Rights Reserved.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package objectdetection

import getFileFromResource
import org.jetbrains.kotlinx.dl.api.inference.loaders.ONNXModelHub
import org.jetbrains.kotlinx.dl.api.inference.objectdetection.DetectedObject
import org.jetbrains.kotlinx.dl.api.inference.onnx.ONNXModels
import org.jetbrains.kotlinx.dl.dataset.image.ColorOrder
import org.jetbrains.kotlinx.dl.dataset.preprocessor.*
import org.jetbrains.kotlinx.dl.dataset.preprocessor.image.*
import toBufferedImage
import java.awt.*
import java.io.File
import javax.swing.JFrame
import javax.swing.JPanel

fun main() {
    val modelHub =
        ONNXModelHub(cacheDirectory = File("cache/pretrainedModels"))
    val model = ONNXModels.ObjectDetection.SSD.pretrainedModel(modelHub)

    model.use { detectionModel ->
        println(detectionModel)

        val imageFile = getFileFromResource("detection/image1.jpg")
        val detectedObjects =
            detectionModel.detectObjects(imageFile = imageFile, topK = 20)

        detectedObjects.forEach {
            println("Found ${it.classLabel} with probability ${it.probability}")
        }

        visualise(imageFile, detectedObjects)
    }
}

private fun visualise(
    imageFile: File,
    detectedObjects: List<DetectedObject>
) {
    val preprocessing: Preprocessing = preprocess {
        load {
            pathToData = imageFile
            imageShape = ImageShape(224, 224, 3)
            colorMode = ColorOrder.BGR
        }
        transformImage {
            resize {
                outputWidth = 1200
                outputHeight = 1200
            }
        }
        transformTensor {
            rescale {
                scalingCoefficient = 255f
            }
        }
    }

    val rawImage = preprocessing().first

    drawDetectedObjects(rawImage, ImageShape(1200, 1200, 3), detectedObjects)
}

private fun drawDetectedObjects(dst: FloatArray, imageShape: ImageShape, detectedObjects: List<DetectedObject>) {
    val frame = JFrame("Filters")
    @Suppress("UNCHECKED_CAST")
    frame.contentPane.add(JPanel(dst, imageShape, detectedObjects))
    frame.pack()
    frame.setLocationRelativeTo(null)
    frame.isVisible = true
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isResizable = false
}

class JPanel(
    val image: FloatArray,
    val imageShape: ImageShape,
    private val detectedObjects: List<DetectedObject>
) : JPanel() {
    private val bufferedImage = image.toBufferedImage(imageShape)

    override fun paint(graphics: Graphics) {
        super.paint(graphics)

        graphics.drawImage(bufferedImage, 0, 0, null)

        detectedObjects.forEach {
            val top = it.yMin * imageShape.height!!
            val left = it.xMin * imageShape.width!!
            val bottom = it.yMax * imageShape.height!!
            val right = it.xMax * imageShape.width!!
            // left, bot, right, top

            // y = columnIndex
            // x = rowIndex
            val yRect = bottom
            val xRect = left
            graphics.color = Color.ORANGE
            graphics.font = Font("Courier New", 1, 17)
            graphics.drawString(" ${it.classLabel} : ${it.probability}", xRect.toInt(), yRect.toInt() - 8)

            graphics as Graphics2D
            val stroke1: Stroke = BasicStroke(6f)
            graphics.setColor(Color.RED)
            graphics.stroke = stroke1
            graphics.drawRect(xRect.toInt(), yRect.toInt(), (right - left).toInt(), (top - bottom).toInt())
        }
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(bufferedImage.width, bufferedImage.height)
    }

    override fun getMinimumSize(): Dimension {
        return Dimension(bufferedImage.width, bufferedImage.height)
    }
}



