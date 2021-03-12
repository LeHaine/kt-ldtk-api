package com.lehaine.ldtk

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


object LDtkApi {

    /**
     * Parse an entire LDtk project file
     */
    fun parseLDtkFile(json: String): ProjectJson? {
        return Json {
            ignoreUnknownKeys = true
        }.decodeFromString<ProjectJson>(json)
    }

    /**
     * Parse an entire LDtk level file
     */
    fun parseLDtkLevelFile(json: String): LevelDefinition? {
        return Json {
            ignoreUnknownKeys = true
        }.decodeFromString<LevelDefinition>(json)
    }


    const val ENTITY_PREFIX = "Entity"
    const val LEVEL_SUFFIX = "Level"
    const val LAYER_PREFIX = "Layer"

}