package com.lehaine.ldtk


enum class LayerType {
    IntGrid,
    Titles,
    Entities,
    AutoLayer,
    Unknown
}

open class Layer(val json: LayerInstanceJson) {
    val identifier: String = json.__identifier
    val type: LayerType = LayerType.valueOf(json.__type)

    /**
    Grid size in pixels
     **/
    val gridSize: Int = json.__gridSize

    /**
    Grid-based layer width
     **/
    val cWid: Int = json.__cWid

    /**
    Grid-based layer height
     **/
    val cHei: Int = json.__cHei

    /**
    Pixel-based layer X offset (includes both instance and definition offsets)
     **/
    val pxTotalOffsetX: Int = json.__pxTotalOffsetX

    /**
    Pixel-based layer Y offset (includes both instance and definition offsets)
     **/
    val pxTotalOffsetY: Int = json.__pxTotalOffsetY

    /** Layer opacity (0-1) **/
    val opacity: Float = json.__opacity

    /**
    Return TRUE if grid-based coordinates are within layer bounds.
     **/
    fun isCoordValid(cx: Int, cy: Int): Boolean {
        return cx in 0 until cWid && cy >= 0 && cy < cHei
    }


    fun getCx(coordId: Int): Int {
        return coordId - coordId / cWid * cWid
    }

    fun getCy(coordId: Int): Int {
        return coordId / cWid
    }

    fun getCoordId(cx: Int, cy: Int) = cx + cy * cWid
}