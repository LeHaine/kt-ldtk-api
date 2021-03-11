package com.lehaine.samples

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.FitViewport
import com.lehaine.ldtk.LayerIntGrid

class GdxApp : ApplicationListener {

    private lateinit var spriteBatch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var worldTiles: Texture
    private lateinit var unitTestWorldTiles: Texture
    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: FitViewport
    private val world = World()
    private val unitTestWorld = UnitTestWorld()
    private val worldLevel = world.allLevels[0]
    private val unitTestWorldLevel = unitTestWorld.allLevels[0]
    private var showWorld = true

    override fun create() {
        spriteBatch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        worldTiles = Texture(Gdx.files.internal("Cavernas_by_Adam_Saltsman.png"))
        unitTestWorldTiles = Texture(Gdx.files.internal("Minecraft_texture_pack.gif"))
        camera = OrthographicCamera()
        viewport = PixelPerfectViewport(480f, 270f, camera)
        camera.translate(worldLevel.pxWidth / 2f, worldLevel.pxHeight / -2f + 20f)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, false)
    }

    override fun render() {
        val bgColorHex = if (showWorld) world.bgColorHex else unitTestWorld.bgColorHex
        val bgColor = Color.valueOf(bgColorHex)
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            showWorld = !showWorld
        }
        camera.update()
        spriteBatch.projectionMatrix = camera.combined
        shapeRenderer.projectionMatrix = camera.combined
        if (showWorld) {
            spriteBatch.begin()
            worldLevel.layerCavern_background.render(spriteBatch, worldTiles)
            worldLevel.layerCollisions.render(spriteBatch, worldTiles)
            worldLevel.layerCustom_tiles.render(spriteBatch, worldTiles)
            spriteBatch.end()
        } else {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            renderIntGrid(shapeRenderer, unitTestWorldLevel.layerIntGrid8)
            renderIntGrid(shapeRenderer, unitTestWorldLevel.layerIntGridTest)
            shapeRenderer.end()
            spriteBatch.begin()
            unitTestWorldLevel.layerTileTest.render(spriteBatch, unitTestWorldTiles)
            unitTestWorldLevel.layerPure_AutoLayer.render(spriteBatch, worldTiles)
            unitTestWorldLevel.layerIntGrid_AutoLayer.render(spriteBatch, worldTiles)
            spriteBatch.end()
        }
    }

    private fun renderIntGrid(shapeRenderer: ShapeRenderer, layerIntGrid: LayerIntGrid) {
        for (cx in 0..layerIntGrid.cWidth) {
            for (cy in 0..layerIntGrid.cHeight) {
                if (layerIntGrid.hasValue(cx, cy)) {
                    val colorHex = layerIntGrid.getColorHex(cx, cy)
                    val gridSize = layerIntGrid.gridSize.toFloat()
                    shapeRenderer.color = Color.valueOf(colorHex)
                    shapeRenderer.rect(cx * gridSize, -cy * gridSize, gridSize, gridSize)
                }
            }
        }
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

