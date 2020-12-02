package com.lehaine.ldtk

open class Project(val projectFilePath: String) {

    var bgColorInt: Int = 0x0
    var bgColorHex: String = "#000000"

    companion object {
        fun intToHex(color: Int, leadingZeros: Int = 6): String {
            var hex = color.toLong().toString(16)
            while (hex.length < leadingZeros) {
                hex = "0$hex"
            }
            return "#$hex"
        }

        fun hexToInt(hex: String): Int {
            return hex.toLong(16).toInt()
        }
    }
}