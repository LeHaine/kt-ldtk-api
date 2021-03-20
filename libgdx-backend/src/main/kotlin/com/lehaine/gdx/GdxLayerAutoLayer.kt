package com.lehaine.gdx

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.lehaine.ldtk.LayerAutoLayer
import com.lehaine.ldtk.LayerInstance
import com.lehaine.ldtk.TilesetDefinition

open class GdxLayerAutoLayer(
    tilesetDefJson: TilesetDefinition,
    json: LayerInstance
) : LayerAutoLayer(
    tilesetDefJson,
    json
) {
    /**
     * Renders the layer. Due to LDtks coordinate system being flipped for LibGDX we need to negate the Y-pos and transform
     * it by the level height
     * @param batch the batch to use for drawing
     * @param tilesTexture the tile texture
     * @param pixelHeight the height of the level `level.pxHei`
     */
    fun render(batch: Batch, tilesTexture: Texture, pixelHeight: Int) {
        val tileset = getTileset() as? GdxTileset ?: error("Unable to load tileset for $identifier layer!")

        val tiles = TextureRegion.split(tilesTexture, tileset.tileGridSize, tileset.tileGridSize)

        autoTiles.forEach { autoTile ->
            tileset.getAutoLayerLDtkTile(tiles, autoTile)?.also {
                batch.draw(
                    it.region.texture,
                    (autoTile.renderX + pxTotalOffsetX).toFloat(),
                    -(autoTile.renderY + pxTotalOffsetY - pixelHeight).toFloat(), // LDtk is y-down, so invert it
                    0f,
                    0f,
                    gridSize.toFloat(),
                    gridSize.toFloat(),
                    1f,
                    1f,
                    0f,
                    it.region.regionX,
                    it.region.regionY,
                    it.region.regionWidth,
                    it.region.regionHeight,
                    it.flipX,
                    it.flipY
                )
            }
        }
    }
}