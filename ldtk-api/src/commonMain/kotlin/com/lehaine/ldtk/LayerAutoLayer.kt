package com.lehaine.ldtk

open class LayerAutoLayer(
    val tilesetDefJson: TilesetDefinition, json: LayerInstance
) : Layer(json) {
    val untypedTileset = Tileset(tilesetDefJson)
    val autoTiles = json.autoLayerTiles.map {
        AutoTile(it.t, it.f, it.px[0], it.px[1])
    }

    data class AutoTile(val tileId: Int, val flips: Int, val renderX: Int, val renderY: Int)


    open fun getTileset(): Tileset {
        return untypedTileset
    }
}