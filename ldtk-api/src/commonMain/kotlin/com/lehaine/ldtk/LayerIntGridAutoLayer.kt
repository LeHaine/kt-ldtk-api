package com.lehaine.ldtk

open class LayerIntGridAutoLayer(
    val tilesetDefJson: TilesetDefinition,
    intGridValues: List<IntGridValueDefinition>,
    json: LayerInstance
) : LayerIntGrid(intGridValues, json) {

    val untypedTileset = Tileset(tilesetDefJson)
    val autoTiles: List<LayerAutoLayer.AutoTile> =
        json.autoLayerTiles.map {
            LayerAutoLayer.AutoTile(
                tileId = it.t,
                flips = it.f,
                renderX = it.px[0],
                renderY = it.px[1]
            )
        }

    open fun getTileset(): Tileset {
        return untypedTileset
    }
}