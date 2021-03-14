package com.lehaine.ldtk

import com.soywiz.korio.async.runBlockingNoSuspensions
import com.soywiz.korio.file.std.resourcesVfs

open class Project(val projectFilePath: String) {

    var bgColorInt: Int = 0
        protected set
    var bgColorHex: String = "#000000"
        protected set
    var worldLayout: WorldLayout? = null
        protected set
    var defs: Definitions? = null
        protected set

    val tilesets = mutableMapOf<Int, Tileset>()
    private val assetCache = mutableMapOf<String, ByteArray>()

    private val _allUntypedLevels = mutableListOf<Level>()
    val allUntypedLevels get() = _allUntypedLevels.toList()


    companion object {
        fun intToHex(color: Int, leadingZeros: Int = 6): String {
            var hex = color.toLong().toString(16)
            while (hex.length < leadingZeros) {
                hex = "0$hex"
            }
            return "#$hex"
        }

        fun hexToInt(hex: String): Int {
            return hex.substring(1).toLong(16).toInt()
        }
    }

    fun load() {
        val jsonString = loadLDtkJson()

        if (jsonString.isEmpty()) {
            error("An empty file was passed in.")
        }

        val json = LDtkApi.parseLDtkFile(jsonString) ?: error("Unable to parse LDtk file content!")
        defs = json.defs

        json.levelDefinitions.forEach { levelJson ->
            val level = instantiateLevel(levelJson)
            level?.let {
                _allUntypedLevels.add(it)
            }
        }

        json.defs.tilesets.forEach {
            tilesets[it.uid] = Tileset(it)

        }
        worldLayout = json.worldLayout
        bgColorHex = json.bgColor
        bgColorInt = hexToInt(json.bgColor)
    }

    open fun instantiateLevel(json: LevelDefinition): Level? {
        return Level(this, json)
    }

    protected open fun loadLDtkJson(): String {
        return runBlockingNoSuspensions {
            resourcesVfs[projectFilePath].readString()
        }
    }

    open fun getAsset(assetPath: String): ByteArray {
        if (assetCache.contains(assetPath)) {
            return assetCache[assetPath] ?: error("Unable to load asset from asset cache!")
        }

        return runBlockingNoSuspensions {
            resourcesVfs[assetPath].readBytes()
        }
    }

    fun getLayerDef(uid: Int?, identifier: String? = ""): LayerDefinition? {
        if (uid == null && identifier == null) {
            return null
        }
        return defs?.layers?.find { it.uid == uid || it.identifier == identifier }
    }

    fun getTilesetDef(uid: Int?, identifier: String? = ""): TilesetDefinition? {
        if (uid == null && identifier == null) {
            return null
        }
        return defs?.tilesets?.find { it.uid == uid || it.identifier == identifier }
    }
}