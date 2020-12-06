package com.lehaine.gdx

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.lehaine.ldtk.LayerInstanceJson
import com.lehaine.ldtk.LayerTiles
import com.lehaine.ldtk.TilesetDefJson

open class GdxLayerTiles(tilesetDefJson: TilesetDefJson, json: LayerInstanceJson) : LayerTiles(tilesetDefJson, json) {


    fun render(batch: Batch, tilesTexture: Texture) {
        val tileset = getTileset() as? GdxTileset ?: error("Unable to load tileset for $identifier layer!")

        val tiles = TextureRegion.split(tilesTexture, tileset.tileGridSize, tileset.tileGridSize)

        for (cy in 0..cHeight) {
            for (cx in 0..cWidth) {
                if (hasAnyTileAt(cx, cy)) {
                    getTileStackAt(cx, cy).forEach { tileInfo ->
                        tileset.getTextureRegion(tiles, tileInfo.tileId, tileInfo.flipBits)?.also {
                            batch.draw(
                                it,
                                (cx * gridSize + pxTotalOffsetX).toFloat(),
                                -(cy * gridSize + pxTotalOffsetY).toFloat() // LDtk is y-down, so invert it
                            )
                        }
                    }
                }
            }
        }
    }
}