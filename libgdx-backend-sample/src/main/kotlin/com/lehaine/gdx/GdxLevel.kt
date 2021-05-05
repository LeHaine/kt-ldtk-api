package com.lehaine.gdx

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.lehaine.ldtk.Level
import com.lehaine.ldtk.LevelDefinition
import com.lehaine.ldtk.Project

open class GdxLevel(project: Project, definition: LevelDefinition) : Level(project, definition), Renderable {

    private var cachedTextureRegion: TextureRegion? = null

    fun getBgImage(): TextureRegion? {
        if (cachedTextureRegion == null && hasBgImage) {
            val imgBytes = project.getAsset(bgImageInfos!!.relFilePath)
            val pixmap = Pixmap(imgBytes, 0, imgBytes.size)
            val texture = Texture(pixmap)
            val crop = bgImageInfos!!.cropRect
            cachedTextureRegion = TextureRegion(texture).apply {
                setRegion(crop.x.toInt(), crop.y.toInt(), crop.w.toInt(), crop.h.toInt())
            }
            pixmap.dispose()

        }
        return cachedTextureRegion
    }


    fun renderBgImage(batch: Batch) {
        if (!hasBgImage) {
            return
        }

        bgImageInfos?.let { bgImageInfo ->
            val bgImage = getBgImage()
            bgImage?.let {
                batch.draw(
                    it.texture,
                    bgImageInfo.topLeftX.toFloat(),
                    bgImageInfo.topLeftY.toFloat(),
                    0f,
                    0f,
                    it.regionWidth.toFloat(),
                    it.regionHeight.toFloat(),
                    bgImageInfo.scaleX,
                    bgImageInfo.scaleY,
                    0f,
                    it.regionX,
                    it.regionY,
                    it.regionWidth,
                    it.regionHeight,
                    false,
                    false
                )
            }
        }
    }

    override fun render(batch: Batch, targetTexture: Texture?) {
        renderBgImage(batch)
        for (i in allUntypedLayers.indices.reversed()) {
            val layer = allUntypedLayers[i]
            if (layer is Renderable) {
                layer.render(batch, targetTexture)
            }
        }
    }

    fun dispose() {
        cachedTextureRegion?.texture?.dispose()
        project.tilesets.forEach {
            if (it is GdxTileset) {
                it.dispose()
            }
        }
    }

}