package com.lehaine.ldtk

import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json


object LDtkApi {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    /**
     * Parse an entire LDtk project file
     */
    fun parseLDtkFile(jsonString: String): ProjectJson? {
        return json.decodeFromString<ProjectJson>(jsonString)
    }

    /**
     * Parse an entire LDtk level file
     */
    fun parseLDtkLevelFile(jsonString: String): LevelDefinition? {
        return json.decodeFromString<LevelDefinition>(jsonString)
    }


    const val ENTITY_PREFIX = "Entity"
    const val LEVEL_SUFFIX = "Level"
    const val LAYER_PREFIX = "Layer"
}