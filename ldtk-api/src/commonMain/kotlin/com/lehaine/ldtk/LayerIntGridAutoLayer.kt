package com.lehaine.ldtk

open class LayerIntGridAutoLayer(
    project: Project,
    val tilesetDefJson: TilesetDefinition,
    intGridValues: List<IntGridValueDefinition>,
    json: LayerInstance
) : LayerIntGrid(project, intGridValues, json) {

    open val tileset = project.tilesets[json.tilesetDefUid]!!
    val autoTiles: List<LayerAutoLayer.AutoTile> =
        json.autoLayerTiles.map {
            LayerAutoLayer.AutoTile(
                tileId = it.t,
                flips = it.f,
                renderX = it.px[0],
                renderY = it.px[1]
            )
        }
    private val _autoTilesCoordIdMap = mutableMapOf<Int, LayerAutoLayer.AutoTile>()
    val autoTilesCoordIdMap: Map<Int, LayerAutoLayer.AutoTile>

    init {
        json.autoLayerTiles.forEach {
            val autoTile = LayerAutoLayer.AutoTile(
                tileId = it.t,
                flips = it.f,
                renderX = it.px[0],
                renderY = it.px[1]
            )
            _autoTilesCoordIdMap[getCoordId(autoTile.renderX / gridSize, autoTile.renderY / gridSize)] = autoTile
        }
        autoTilesCoordIdMap = _autoTilesCoordIdMap.toMap()
    }
}