package com.lehaine.samples

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport

class GdxApp : ApplicationListener {

    private lateinit var spriteBatch: SpriteBatch
    private lateinit var tiles: Texture
    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: FitViewport
    private val world = World()
    private val testLevel = world.allLevels.find { it.identifier == "TileTest" }!!

    override fun create() {
        spriteBatch = SpriteBatch()
        tiles = Texture(Gdx.files.internal("Cavernas_by_Adam_Saltsman.png"))
        camera = OrthographicCamera()
        viewport = FitViewport(480f, 270f, camera)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, false)
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        spriteBatch.projectionMatrix = camera.combined
        spriteBatch.begin()
        testLevel.layerBackground.render(spriteBatch, tiles)
        testLevel.layerCollisions.render(spriteBatch, tiles)
        testLevel.layerCustom_tiles.render(spriteBatch, tiles)
        spriteBatch.end()
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun dispose() {
    }

}

object GdxTest {
    @JvmStatic
    fun main(arg: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration().apply {
            setWindowedMode(960, 540)
        }
        Lwjgl3Application(GdxApp(), config)
    }
}

