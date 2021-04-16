package com.lehaine.ldtk

open class Tileset(val project: Project, val json: TilesetDefinition) {
    val identifier = json.identifier
    val relPath = json.relPath
    val tileGridSize = json.tileGridSize
    val pxWidth = json.pxWid
    val pxHeight = json.pxHei
    val cWidth = json.cWid
    val cHeight = json.cHei
    val customData = json.customData
    val tags = run {
        val map = mutableMapOf<String, List<Int>>()
        json.enumTags.forEach {
            map[it.enumValueId] = it.tileIds
        }
        map.toMap()
    }

    /**
     * Checks if the tag exists for the specified tile id
     */
    fun hasTag(tag: String, tileId: Int) = tags[tag]?.contains(tileId) ?: false

    /**
     * Returns a newly created List of tags that are associated with the given tile id
     */
    fun getAllTags(tileId: Int): List<String> {
        val allTags = mutableListOf<String>()
        tags.forEach { entry ->
            if (entry.value.contains(tileId)) {
                allTags += entry.key
            }
        }
        return allTags.toList()
    }

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