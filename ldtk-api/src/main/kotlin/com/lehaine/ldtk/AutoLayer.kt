package com.lehaine.ldtk

class AutoLayer(json: LayerInstanceJson) : Layer(json) {

    val autoTiles = json.autoLayerTiles.map {
        AutoTile(it.t, it.f, it.px[0], it.px[1])
    }

    data class AutoTile(val tileId: Int, val flips: Int, val renderX: Int, val renderY: Int)
}