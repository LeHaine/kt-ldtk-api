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
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.FitViewport
import com.lehaine.gdx.convertToGdxPos
import com.lehaine.ldtk.Entity
import com.lehaine.ldtk.LayerIntGrid
import com.lehaine.ldtk.Level

class GdxApp : ApplicationListener {

    private lateinit var spriteBatch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var worldTiles: Texture
    private lateinit var unitTestWorldTiles: Texture
    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: FitViewport
    private val world = World()

    private var currentWorldIdx = 0
    private var worldBgImage: TextureRegion? = null
    private lateinit var worldLevel: World.WorldLevel

    private val velocity = Vector2()
    private val speed = 5f

    private val gridSize = 8;
    private val temp = Vector2()

    override fun create() {
        spriteBatch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        worldTiles = Texture(Gdx.files.internal("Cavernas_by_Adam_Saltsman.png"))
        unitTestWorldTiles = Texture(Gdx.files.internal("Minecraft_texture_pack.gif"))
        camera = OrthographicCamera()
        viewport = PixelPerfectViewport(480f, 270f, camera)
        loadLevel(currentWorldIdx)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, false)
    }

    override fun render() {
        val bgColorHex = world.bgColorHex
        val bgColor = Color.valueOf(bgColorHex)
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            loadLevel(0)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            loadLevel(1)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            loadLevel(2)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            loadLevel(3)
        }

        velocity.setZero()

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            velocity.y = speed
        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            velocity.y = -speed
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            velocity.x = speed
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            velocity.x = -speed
        }
        camera.translate(velocity)

        camera.update()
        spriteBatch.projectionMatrix = camera.combined
        shapeRenderer.projectionMatrix = camera.combined
        spriteBatch.begin()
        renderBgImage(spriteBatch, worldLevel)
        worldLevel.layerCavern_background.render(spriteBatch, worldTiles, worldLevel.pxHeight)
        worldLevel.layerCollisions.render(spriteBatch, worldTiles, worldLevel.pxHeight)
        worldLevel.layerCustom_tiles.render(spriteBatch, worldTiles, worldLevel.pxHeight)
        spriteBatch.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        worldLevel.layerEntities.allPlayer.forEach {
            renderEntity(shapeRenderer, it, Color.GREEN, gridSize, worldLevel.pxHeight)
        }

        worldLevel.layerEntities.allMob.forEach {
            renderEntity(shapeRenderer, it, Color.RED, gridSize, worldLevel.pxHeight)
        }

        worldLevel.layerEntities.allItem.forEach {
            renderEntity(shapeRenderer, it, Color.GOLD, gridSize, worldLevel.pxHeight)
        }
        shapeRenderer.end()

        temp.setZero()
    }

    private fun loadLevel(levelIdx: Int) {
        if (levelIdx <= world.allLevels.size - 1) {
            worldLevel = world.allLevels[levelIdx]
            if (worldLevel.hasBgImage) {
                worldBgImage?.texture?.dispose()
                val crop = worldLevel.bgImageInfos!!.cropRect
                worldBgImage = TextureRegion(
                    Texture(Gdx.files.internal(worldLevel.bgImageInfos!!.relFilePath))
                ).apply {
                    setRegion(crop.x.toInt(), crop.y.toInt(), crop.w.toInt(), crop.h.toInt())
                }
            }
        }
        camera.position.set(worldLevel.pxWidth / 2f, worldLevel.pxHeight / 2f + 20f, camera.position.z)
    }

    private fun renderBgImage(spriteBatch: SpriteBatch, level: Level) {
        level.bgImageInfos?.let { bgImageInfo ->
            worldBgImage?.let {
                spriteBatch.draw(
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

    private fun renderEntity(
        shapeRenderer: ShapeRenderer,
        entity: Entity,
        color: Color,
        entitySize: Int,
        pxHeight: Int
    ) {
        temp.convertToGdxPos(entity.cx, entity.cy, gridSize, pxHeight)
        shapeRenderer.color = color
        shapeRenderer.rect(
            temp.x,
            temp.y,
            entitySize.toFloat(),
            entitySize.toFloat()
        )
    }

    private fun renderIntGrid(shapeRenderer: ShapeRenderer, layerIntGrid: LayerIntGrid, pxHeight: Int) {
        for (cx in 0..layerIntGrid.cWidth) {
            for (cy in 0..layerIntGrid.cHeight) {
                if (layerIntGrid.hasValue(cx, cy)) {
                    val colorHex = layerIntGrid.getColorHex(cx, cy)
                    val gridSize = layerIntGrid.gridSize
                    temp.convertToGdxPos(cx, cy, gridSize, pxHeight)
                    shapeRenderer.color = Color.valueOf(colorHex)
                    shapeRenderer.rect(temp.x, temp.y, gridSize.toFloat(), gridSize.toFloat())
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

