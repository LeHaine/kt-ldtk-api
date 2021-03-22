package com.lehaine.ldtk

open class LayerAutoLayer(
    val tilesetDefJson: TilesetDefinition, json: LayerInstance
) : Layer(json) {
    val untypedTileset = Tileset(tilesetDefJson)
    val autoTiles = json.autoLayerTiles.map {
        AutoTile(it.t, it.f, it.px[0], it.px[1])
    }

    private val _autoTilesCoordIdMap = mutableMapOf<Int, AutoTile>()
    val autoTilesCoordIdMap: Map<Int, AutoTile>

    init {
        json.autoLayerTiles.forEach {
            val autoTile = AutoTile(
                tileId = it.t,
                flips = it.f,
                renderX = it.px[0],
                renderY = it.px[1]
            )
            _autoTilesCoordIdMap[getCoordId(autoTile.renderX / gridSize, autoTile.renderY / gridSize)] = autoTile
        }
        autoTilesCoordIdMap = _autoTilesCoordIdMap.toMap()
    }

    open fun getTileset(): Tileset {
        return untypedTileset
    }

    data class AutoTile(val tileId: Int, val flips: Int, val renderX: Int, val renderY: Int)

}