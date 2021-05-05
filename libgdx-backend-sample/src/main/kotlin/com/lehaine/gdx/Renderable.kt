package com.lehaine.gdx

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch

interface Renderable {

    fun render(batch: Batch, targetTexture: Texture? = null)
}