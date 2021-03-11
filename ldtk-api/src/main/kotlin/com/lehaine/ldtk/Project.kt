package com.lehaine.ldtk

open class Project(val projectFilePath: String) {

    val bgColorInt: Int
    val bgColorHex: String
    val worldLayout: WorldLayout
    val defs: DefinitionJson

    val tilesets = mutableMapOf<Int, Tileset>()

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

    init {
        val jsonString =
            javaClass.classLoader.getResource(projectFilePath)?.readText() ?: error("Unable to load LDtk file content!")
        val json = LDtkApi.parseLDtkFile(jsonString) ?: error("Unable to parse LDtk file content!")
        defs = json.defs

        json.levels.forEach { levelJson ->
            val level = instantiateLevel(this, levelJson)
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

    open fun instantiateLevel(project: Project, json: LevelJson): Level? {
        return null
    }


    fun getLayerDef(uid: Int?, identifier: String? = ""): LayerDefJson? {
        if (uid == null && identifier == null) {
            return null
        }
        return defs.layers.find { it.uid == uid || it.identifier == identifier }
    }

    fun getTilesetDef(uid: Int?, identifier: String? = ""): TilesetDefJson? {
        if (uid == null && identifier == null) {
            return null
        }
        return defs.tilesets.find { it.uid == uid || it.identifier == identifier }
    }
}