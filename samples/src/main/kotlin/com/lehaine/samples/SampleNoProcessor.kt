package com.lehaine.samples

import com.lehaine.ldtk.LayerIntGrid
import com.lehaine.ldtk.LayerType
import com.lehaine.ldtk.Project

/**
 * A sample not using the ldtk-processor.
 */
fun main(args: Array<String>) {
    val proj = Project("sample.ldtk")
    val level = proj.allUntypedLevels[0]
    level.load() // this is only needed if levels are saved in separate files!
    val gridSize = 16
    level.allUntypedEntities?.forEach { entity ->
        val x = entity.cx * gridSize
        val y = entity.cy * gridSize
        entity.json.fieldInstances.forEach {
            if (it.identifier == "Color") {
                val color = it.value!!.content
            }

        }
    }
    level.allUntypedLayers.forEach { layer ->
        if (layer.type == LayerType.IntGrid) {
            val intGridLayer = layer as LayerIntGrid
            intGridLayer.getInt(0, 5)
        }
    }
}