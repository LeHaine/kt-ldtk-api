package com.lehaine.gdx

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.lehaine.ldtk.LayerAutoLayer
import com.lehaine.ldtk.Project
import com.lehaine.ldtk.Tileset
import com.lehaine.ldtk.TilesetDefinition

open class GdxTileset(project: Project, json: TilesetDefinition) : Tileset(project, json) {

    data class LDtkTile(val region: TextureRegion, val flipX: Boolean = false, val flipY: Boolean = false)

    private var cachedTexture: Texture? = null
    private var cachedTextureRegions: Array<Array<TextureRegion>>? = null

    fun getTiles(textureTarget: Texture?): Array<Array<TextureRegion>> {
        if (cachedTextureRegions == null) {
            if (textureTarget == null) {
                val imgBytes = project.getAsset(relPath)
                val pixmap = Pixmap(imgBytes, 0, imgBytes.size)
                cachedTexture = Texture(pixmap)
                pixmap.dispose()
            } else {
                cachedTexture = textureTarget
            }
            cachedTextureRegions = TextureRegion.split(cachedTexture, tileGridSize, tileGridSize)
        }
        return cachedTextureRegions!!
    }

    /**
     * Grabs the correct texture region based on tile id.
     * @tiles the double array of texture regions to select from
     * @tileId the tile id
     * @flipBits the tile flip value; 0 = none, 1 = flip X, 2 = flip Y, 3 = flip XY
     */
    fun getLDtkTile(tileId: Int, flipBits: Int = 0, textureTarget: Texture? = null): LDtkTile? {
        if (tileId < 0) {
            return null
        }

        val tiles = getTiles(textureTarget)
        val tx = getAtlasX(tileId)
        val ty = getAtlasY(tileId)
        if (ty >= tiles.size) {
            return null
        }
        if (tx >= tiles[ty].size) {
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
        autoTile: LayerAutoLayer.AutoTile,
        textureTarget: Texture? = null
    ): LDtkTile? {
        if (autoTile.tileId < 0) {
            return null
        }
        return getLDtkTile(autoTile.tileId, autoTile.flips, textureTarget)
    }

    fun dispose() {
        cachedTexture?.dispose()
        cachedTextureRegions = null
    }
}