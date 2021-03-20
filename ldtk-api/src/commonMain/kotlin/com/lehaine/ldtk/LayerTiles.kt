package com.lehaine.ldtk

open class LayerTiles(
    val tilesetDefJson: TilesetDefinition, json: LayerInstance
) : Layer(json) {
    data class TileInfo(val tileId: Int, val flipBits: Int)

    val untypedTileset = Tileset(tilesetDefJson)

    private val _tiles = mutableMapOf<Int, List<TileInfo>>()
    val tiles get() = _tiles.toMap()

    init {
        json.gridTiles.forEach {
            if (!_tiles.containsKey(it.d[0])) {
                _tiles[it.d[0]] = mutableListOf(TileInfo(it.t, it.f))
            } else {
                val mutList = _tiles[it.d[0]] as MutableList
                mutList.add(TileInfo(it.t, it.f))
            }
        }
    }

    fun getTileStackAt(cx: Int, cy: Int): List<TileInfo> {
        return if (isCoordValid(cx, cy) && _tiles.contains(getCoordId(cx, cy))) {
            _tiles[getCoordId(cx, cy)] ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun hasAnyTileAt(cx: Int, cy: Int): Boolean {
        return _tiles.contains(getCoordId(cx, cy))
    }

    protected open fun getTileset(): Tileset {
        return untypedTileset
    }

}