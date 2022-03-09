/*
 * Copyright 2020 JetBrains s.r.o. and Kotlin Deep Learning project contributors. All Rights Reserved.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package demo.objectdetection

import getFileFromResource
import org.jetbrains.kotlinx.dl.api.inference.loaders.ONNXModelHub
import org.jetbrains.kotlinx.dl.api.inference.objectdetection.DetectedObject
import org.jetbrains.kotlinx.dl.api.inference.onnx.ONNXModels
import java.awt.*
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.abs


fun main() {
    val modelHub = ONNXModelHub(cacheDirectory = File("cache/pretrainedModels"))
    val model = modelHub.loadPretrainedModel(ONNXModels.ObjectDetection.SSD)

    model.use { detectionModel ->
        println(detectionModel)

        val imageFile = getFileFromResource("detection/image2.jpg")
        val detectedObjects = detectionModel.detectObjects(imageFile = imageFile, topK = 20)

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
    val frame = JFrame("Detected Objects")
    @Suppress("UNCHECKED_CAST")
    frame.contentPane.add(ObjectDetectionPanel(imageFile, detectedObjects))
    frame.pack()
    frame.setLocationRelativeTo(null)
    frame.isVisible = true
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isResizable = false
}

private class ObjectDetectionPanel(
    val image: File,
    private val detectedObjects: List<DetectedObject>
) : JPanel() {
    private var bufferedImage = ImageIO.read(image)

    override fun paint(graphics: Graphics) {
        super.paint(graphics)
        graphics.drawImage(bufferedImage, 0, 0, null)

        detectedObjects.forEach {
            val top = it.yMin * bufferedImage.height
            val left = it.xMin * bufferedImage.width
            val bottom = it.yMax * bufferedImage.height
            val right = it.xMax * bufferedImage.width
            if (abs(top - bottom) > 300 || abs(right - left) > 300) return@forEach

            graphics.color = Color.ORANGE
            graphics.font = Font("Courier New", 1, 17)
            graphics.drawString(" ${it.classLabel} : ${it.probability}", left.toInt(), bottom.toInt() - 8)

            graphics as Graphics2D
            val stroke1: Stroke = BasicStroke(6f)
            graphics.setColor(Color.RED)
            graphics.stroke = stroke1
            graphics.drawRect(left.toInt(), bottom.toInt(), (right - left).toInt(), (top - bottom).toInt())
        }
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(bufferedImage.width, bufferedImage.height)
    }

    override fun getMinimumSize(): Dimension {
        return Dimension(bufferedImage.width, bufferedImage.height)
    }
}



