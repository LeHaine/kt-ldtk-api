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


    fun render(batch: Batch, tilesTexture: Texture) {
        val tileset = getTileset() as? GdxTileset ?: error("Unable to load tileset for $identifier layer!")

        val tiles = TextureRegion.split(tilesTexture, tileset.tileGridSize, tileset.tileGridSize)

        autoTiles.forEach {
            batch.draw(
                tileset.getAutoLayerTextureRegion(tiles, it),
                (it.renderX + pxTotalOffsetX).toFloat(),
                (it.renderY + pxTotalOffsetY).toFloat()
            )
        }
    }
}