package com.lehaine.ldtk

open class LayerIntGridAutoLayer(json: LayerInstanceJson) : LayerIntGrid(json) {

    val autoTiles: List<AutoLayer.AutoTile> =
        json.autoLayerTiles.map {
            AutoLayer.AutoTile(
                tileId = it.t,
                flips = it.f,
                renderX = it.px[0],
                renderY = it.px[1]
            )
        }
}