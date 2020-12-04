package com.lehaine.ldtk

open class LayerIntGrid(val intGridValues: List<IntGridValue>, json: LayerInstanceJson) : Layer(json) {

    data class ValueInfo(val identifier: String?, val color: Int)

    private val valueInfos = intGridValues.map {
        ValueInfo(it.identifier, it.color.substring(1).toInt(16))

    }
    private val _intGrid = mutableMapOf<Int, Int>().apply {
        json.intGrid.forEach {
            put(it.coordId, it.v)
        }
    }

    /**
     * IntGrid integer values, map is based on coordIds
     */
    val intGrid get() = _intGrid.toMap()

    /**
     * Get the Integer value at selected coordinates
     * @return -1 if none.
     */
    fun getInt(cx: Int, cy: Int): Int {
        return if (!isCoordValid(cx, cy) || !intGrid.contains(getCoordId(cx, cy))) {
            -1
        } else {
            intGrid[getCoordId(cx, cy)] ?: error("Selected coordinates are not valid for this IntGrid Layer")
        }
    }

    /**
     * @param cx grid x coord
     * @param cy grid y coord
     * @param value optional parameter allows to check for a specific integer value
     * @return true if there is any value at selected coordinates.
     */
    fun hasValue(cx: Int, cy: Int, value: Int? = null): Boolean {
        return value == null && getInt(cx, cy) != -1 ||
                value != null && getInt(cx, cy) == value
    }

    /**
     * Get the value String identifier at selected coordinates.
     * @return null if none.
     */
    fun getName(cx: Int, cy: Int): String? {
        return if (!hasValue(cx, cy)) {
            null
        } else {
            valueInfos[getInt(cx, cy)].identifier
        }
    }

    /**
     * Get the value color (0xrrggbb Unsigned-Int format) at selected coordinates.
     * @return null if none.
     */
    fun getColorInt(cx: Int, cy: Int): Int? {
        return if (!hasValue(cx, cy)) {
            null
        } else {
            valueInfos[getInt(cx, cy)].color
        }
    }

    /**
     * Get the value color ("#rrggbb" string format) at selected coordinates.
     * @return null if none.
     */
    fun getColorHex(cx: Int, cy: Int): String? {
        return if (!hasValue(cx, cy)) {
            null
        } else {
            Project.intToHex(valueInfos[getInt(cx, cy)].color)
        }
    }
}