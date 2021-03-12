package com.lehaine.gdx

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.lehaine.ldtk.IntGridValue
import com.lehaine.ldtk.LayerInstanceJson
import com.lehaine.ldtk.LayerIntGridAutoLayer
import com.lehaine.ldtk.TilesetDefJson

open class GdxLayerIntGridAutoLayer(
    tilesetDefJson: TilesetDefJson?,
    intGridValues: List<IntGridValue>, json: LayerInstanceJson
) : LayerIntGridAutoLayer(tilesetDefJson, intGridValues, json) {


    fun render(batch: Batch, tilesTexture: Texture, pixelHeight:Int) {
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