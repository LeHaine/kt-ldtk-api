package com.lehaine.gdx

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.lehaine.ldtk.LayerAutoLayer
import com.lehaine.ldtk.Tileset
import com.lehaine.ldtk.TilesetDefJson

open class GdxTileset(json: TilesetDefJson) : Tileset(json) {

    /**
     * Grabs the correct texture region based on tile id.
     * @tiles the double array of texture regions to select from
     * @tileId the tile id
     * @flipBits the tile flip value; 0 = none, 1 = flip X, 2 = flip Y, 3 = flip XY
     */
    fun getTextureRegion(tiles: Array<Array<TextureRegion>>, tileId: Int, flipBits: Int = 0): TextureRegion? {
        if (tileId < 0) {
            return null
        }

        val region = tiles[getAtlasX(tileId)][getAtlasY(tileId)]
        return when (flipBits) {
            0 -> region
            1 -> {
                region.flip(true, false)
                region
            }
            2 -> {
                region.flip(false, true)
                region
            }
            3 -> {
                region.flip(true, true)
                region
            }
            else -> error("Unsupported flipBits value")
        }
    }

    fun getAutoLayerTextureRegion(
        tiles: Array<Array<TextureRegion>>,
        autoTile: LayerAutoLayer.AutoTile
    ): TextureRegion? {
        if (autoTile.tileId < 0) {
            return null
        }
        return getTextureRegion(tiles, autoTile.tileId, autoTile.flips)
    }
}