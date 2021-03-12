package com.lehaine.ldtk

import kotlin.math.ceil

open class Tileset(val json: TilesetDefinition) {
    val identifier = json.identifier
    val relPath = json.relPath
    val tileGridSize = json.tileGridSize
    val pxWidth = json.pxWid
    val pxHeight = json.pxHei
    val cWidth get() = ceil((pxWidth / tileGridSize).toDouble()).toInt()

    /**
     * Get the X pixel coordinate (in the atlas image) from a specified tile ID
     */
    fun getAtlasX(tileId: Int): Int {
        return (tileId - (tileId / cWidth) * cWidth)
    }

    /**
     * Get the Y pixel coordinate (in the atlas image) from a specified tile ID
     */
    fun getAtlasY(tileId: Int): Int {
        return tileId / cWidth
    }
}