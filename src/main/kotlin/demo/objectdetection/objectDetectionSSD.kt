/*
 * Copyright 2020 JetBrains s.r.o. and Kotlin Deep Learning project contributors. All Rights Reserved.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package demo.objectdetection

import getFileFromResource
import org.jetbrains.kotlinx.dl.api.inference.loaders.ONNXModelHub
import org.jetbrains.kotlinx.dl.api.inference.onnx.ONNXModels
import java.io.File

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
    }
}



