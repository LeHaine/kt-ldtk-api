package com.lehaine.ldtk


enum class LayerType {
    IntGrid,
    Tiles,
    Entities,
    AutoLayer,
    Unknown
}

open class Layer(val project: Project, val json: LayerInstance) {
    val identifier: String = json.identifier
    val type: LayerType = LayerType.valueOf(json.type)

    /**
     * Grid size in pixels
     */
    val gridSize: Int = json.gridSize

    /**
     * Grid-based layer width
     */
    val cWidth: Int = json.cWid

    /**
     * Grid-based layer height
     */
    val cHeight: Int = json.cHei

    /**
     * Pixel-based layer X offset (includes both instance and definition offsets)
     */
    val pxTotalOffsetX: Int = json.pxTotalOffsetX

    /**
     * Pixel-based layer Y offset (includes both instance and definition offsets)
     */
    val pxTotalOffsetY: Int = json.pxTotalOffsetY

    /** Layer opacity (0-1) **/
    val opacity: Float = json.opacity

    /**
     * @return TRUE if grid-based coordinates are within layer bounds.
     */
    fun isCoordValid(cx: Int, cy: Int): Boolean {
        return cx in 0 until cWidth && cy >= 0 && cy < cHeight
    }


    fun getCx(coordId: Int): Int {
        return coordId - coordId / cWidth * cWidth
    }

    fun getCy(coordId: Int): Int {
        return coordId / cWidth
    }

    fun getCoordId(cx: Int, cy: Int) = cx + cy * cWidth
}