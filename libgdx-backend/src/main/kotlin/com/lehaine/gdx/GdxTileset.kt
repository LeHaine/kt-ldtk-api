package com.lehaine.gdx

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.lehaine.ldtk.LayerAutoLayer
import com.lehaine.ldtk.Tileset
import com.lehaine.ldtk.TilesetDefJson

open class GdxTileset(json: TilesetDefJson) : Tileset(json) {

    data class LDtkTile(val region: TextureRegion, val flipX: Boolean = false, val flipY: Boolean = false)

    /**
     * Grabs the correct texture region based on tile id.
     * @tiles the double array of texture regions to select from
     * @tileId the tile id
     * @flipBits the tile flip value; 0 = none, 1 = flip X, 2 = flip Y, 3 = flip XY
     */
    fun getLDtkTile(tiles: Array<Array<TextureRegion>>, tileId: Int, flipBits: Int = 0): LDtkTile? {
        if (tileId < 0) {
            return null
        }

        val tx = getAtlasX(tileId)
        val ty = getAtlasY(tileId)
        if (ty >= tiles.size) {
            return null
        }
        if (tx >= tiles[tx].size) {
            return null
        }
        val region = tiles[ty][tx]
        return when (flipBits) {
            0 -> LDtkTile(region)
            1 -> LDtkTile(region, true)
            2 -> LDtkTile(region, flipX = false, flipY = true)
            3 -> LDtkTile(region, flipX = true, flipY = true)
            else -> error("Unsupported flipBits value")
        }
    }

    fun getAutoLayerLDtkTile(
        tiles: Array<Array<TextureRegion>>,
        autoTile: LayerAutoLayer.AutoTile
    ): LDtkTile? {
        if (autoTile.tileId < 0) {
            return null
        }
        return getLDtkTile(tiles, autoTile.tileId, autoTile.flips)
    }
}