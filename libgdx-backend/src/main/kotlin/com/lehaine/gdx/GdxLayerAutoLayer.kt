package com.lehaine.gdx

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.lehaine.ldtk.LayerAutoLayer
import com.lehaine.ldtk.LayerInstanceJson
import com.lehaine.ldtk.TilesetDefJson

open class GdxLayerAutoLayer(
    tilesetDefJson: TilesetDefJson?,
    json: LayerInstanceJson
) : LayerAutoLayer(
    tilesetDefJson,
    json
) {

    fun render(batch: Batch, tilesTexture: Texture) {
        val tileset = getTileset() as? GdxTileset ?: error("Unable to load tileset for $identifier layer!")

        val tiles = TextureRegion.split(tilesTexture, tileset.tileGridSize, tileset.tileGridSize)

        autoTiles.forEach { autoTile ->
            tileset.getAutoLayerTextureRegion(tiles, autoTile)?.also {
                batch.draw(
                    it,
                    (autoTile.renderX + pxTotalOffsetX).toFloat(),
                    -(autoTile.renderY + pxTotalOffsetY).toFloat() // LDtk is y-down, so invert it
                )
            }
        }
    }
}