package com.lehaine.ldtk

import com.squareup.moshi.Moshi

object LDtkApi {

    private val moshi = Moshi.Builder().build()
    private val projectAdapter = moshi.adapter(ProjectJson::class.java)
    private val levelAdapter = moshi.adapter(LevelJson::class.java)

    /**
     * Parse an entire LDtk project file
     */
    fun parseLDtkFile(json: String): ProjectJson? {
        return projectAdapter.fromJson(json)
    }

    /**
     * Parse an entire LDtk level file
     */
    fun parseLDtkLevelFile(json: String): LevelJson? {
        return levelAdapter.fromJson(json)
    }


    const val ENTITY_PREFIX = "Entity"
    const val LEVEL_SUFFIX = "Level"
    const val LAYER_PREFIX = "Layer"

}