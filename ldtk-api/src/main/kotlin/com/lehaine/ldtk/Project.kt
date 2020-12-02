package com.lehaine.ldtk

open class Project(val projectFilePath: String) {

    var bgColorInt: Int = 0x0
    var bgColorHex: String = "#000000"

    private val _allUntypedLevels = mutableListOf<Level>()
    val allUntypedLevels get() = _allUntypedLevels

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

    fun parseJson(jsonString: String) {
        val json = LDtkApi.parseLDtkFile(jsonString) ?: error("Unable to parse LDtk file content!")
        bgColorHex = json.bgColor
        bgColorInt = hexToInt(json.bgColor)

        json.levels.forEach { levelJson ->
            val level = instantiateLevel(this, levelJson)
            level?.let {
                _allUntypedLevels.add(it)
            }
        }
    }

    open fun instantiateLevel(project: Project, json: LevelJson): Level? {
        return null
    }
}