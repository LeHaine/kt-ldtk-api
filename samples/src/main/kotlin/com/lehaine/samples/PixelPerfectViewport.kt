package com.lehaine.samples

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import kotlin.math.max
import kotlin.math.min

// https://gist.github.com/mgsx-dev/ff693b8d83e6d07f88b0aaf653407e5a
class PixelPerfectViewport(worldWidth: Float, worldHeight: Float, camera: Camera?) :
    FitViewport(worldWidth, worldHeight, camera) {

    override fun update(screenWidth: Int, screenHeight: Int, centerCamera: Boolean) {
        val wRatio = screenWidth / worldWidth
        val hRatio = screenHeight / worldHeight
        val ratio = min(wRatio, hRatio)

        val intRatio = max(1, MathUtils.floor(ratio))

        val viewportWidth = worldWidth.toInt() * intRatio
        val viewportHeight = worldHeight.toInt() * intRatio

        setScreenBounds(
            (screenWidth - viewportWidth) / 2,
            (screenHeight - viewportHeight) / 2,
            viewportWidth,
            viewportHeight
        )

        apply(centerCamera)
    }
}