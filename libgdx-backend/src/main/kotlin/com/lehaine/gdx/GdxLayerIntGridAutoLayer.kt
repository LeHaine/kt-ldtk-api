package com.lehaine.gdx

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.lehaine.ldtk.*

open class GdxLayerIntGridAutoLayer(
    project: Project,
    tilesetDefJson: TilesetDefinition,
    intGridValues: List<IntGridValueDefinition>, json: LayerInstance
) : LayerIntGridAutoLayer(project, tilesetDefJson, intGridValues, json) {

    /**
     * Renders the layer. Due to LDtks coordinate system being flipped for LibGDX we need to negate the Y-pos and transform
     * it by the level height
     * @param batch the batch to use for drawing
     * @param tilesTexture the target tiles texture to render
     */
    fun render(batch: Batch, tilesTexture: Texture? = null) {
        val tileset = tileset as? GdxTileset ?: error("Unable to load tileset for $identifier layer!")

        autoTiles.forEach { autoTile ->
            tileset.getAutoLayerLDtkTile(autoTile, tilesTexture)?.also {
                batch.draw(
                    it.region.texture,
                    (autoTile.renderX + pxTotalOffsetX).toFloat(),
                    -(autoTile.renderY + pxTotalOffsetY - cHeight * gridSize).toFloat(), // LDtk is y-down, so invert it
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