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
import com.lehaine.ldtk.LayerIntGrid
import com.lehaine.ldtk.Level

//class GdxApp : ApplicationListener {
//
//    private lateinit var spriteBatch: SpriteBatch
//    private lateinit var shapeRenderer: ShapeRenderer
//    private lateinit var worldTiles: Texture
//    private lateinit var unitTestWorldTiles: Texture
//    private lateinit var camera: OrthographicCamera
//    private lateinit var viewport: FitViewport
//    private val world = World()
//    private val unitTestWorld = UnitTestWorld()
//
//    private var currentWorldIdx = 0
//    private var worldBgImage: TextureRegion? = null
//    private lateinit var worldLevel: World.WorldLevel
//
//    private var currentUnitTestWorldIdx = 0
//    private var unitTWorldBgImage: TextureRegion? = null
//    private lateinit var unitTestWorldLevel: UnitTestWorld.UnitTestWorldLevel
//    private var showWorld = true
//
//    private val velocity = Vector2()
//    private val speed = 5f
//
//    override fun create() {
//        spriteBatch = SpriteBatch()
//        shapeRenderer = ShapeRenderer()
//        worldTiles = Texture(Gdx.files.internal("Cavernas_by_Adam_Saltsman.png"))
//        unitTestWorldTiles = Texture(Gdx.files.internal("Minecraft_texture_pack.gif"))
//        camera = OrthographicCamera()
//        viewport = PixelPerfectViewport(480f, 270f, camera)
//        loadLevel(currentWorldIdx)
//    }
//
//    override fun resize(width: Int, height: Int) {
//        viewport.update(width, height, false)
//    }
//
//    override fun render() {
//        val bgColorHex = if (showWorld) world.bgColorHex else unitTestWorld.bgColorHex
//        val bgColor = Color.valueOf(bgColorHex)
//        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a)
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
//        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
//            showWorld = !showWorld
//            if (showWorld) {
//                loadLevel(currentWorldIdx)
//            } else {
//                loadLevel(currentUnitTestWorldIdx)
//            }
//        }
//        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
//            loadLevel(0)
//        }
//        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
//            loadLevel(1)
//        }
//        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
//            loadLevel(2)
//        }
//        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
//            loadLevel(3)
//        }
//
//        velocity.setZero()
//
//        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
//            velocity.y = speed
//        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
//            velocity.y = -speed
//        }
//
//        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
//            velocity.x = speed
//        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
//            velocity.x = -speed
//        }
//        camera.translate(velocity)
//
//        camera.update()
//        spriteBatch.projectionMatrix = camera.combined
//        shapeRenderer.projectionMatrix = camera.combined
//        if (showWorld) {
//            spriteBatch.begin()
//            renderBgImage(spriteBatch, worldLevel)
//            worldLevel.layerCavern_background.render(spriteBatch, worldTiles, worldLevel.pxHeight)
//            worldLevel.layerCollisions.render(spriteBatch, worldTiles, worldLevel.pxHeight)
//            worldLevel.layerCustom_tiles.render(spriteBatch, worldTiles, worldLevel.pxHeight)
//            spriteBatch.end()
//        } else {
////            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
////            renderIntGrid(shapeRenderer, unitTestWorldLevel.layerIntGrid8)
////            renderIntGrid(shapeRenderer, unitTestWorldLevel.layerIntGridTest)
////            shapeRenderer.end()
//            spriteBatch.begin()
//            unitTestWorldLevel.layerTileTest.render(spriteBatch, unitTestWorldTiles, unitTestWorldLevel.pxHeight)
//            unitTestWorldLevel.layerPure_AutoLayer.render(spriteBatch, worldTiles, unitTestWorldLevel.pxHeight)
//            unitTestWorldLevel.layerIntGrid_AutoLayer.render(spriteBatch, worldTiles, unitTestWorldLevel.pxHeight)
//            spriteBatch.end()
//        }
//    }
//
//    private fun loadLevel(levelIdx: Int) {
//        if (showWorld) {
//            if (levelIdx <= world.allLevels.size - 1) {
//                worldLevel = world.allLevels[levelIdx]
//                if (worldLevel.hasBgImage) {
//                    worldBgImage?.texture?.dispose()
//                    val crop = worldLevel.bgImageInfos!!.cropRect
//                    worldBgImage = TextureRegion(
//                        Texture(Gdx.files.internal(worldLevel.bgImageInfos!!.relFilePath))
//                    ).apply {
//                        setRegion(crop.x.toInt(), crop.y.toInt(), crop.w.toInt(), crop.h.toInt())
//                    }
//                }
//            }
//            camera.position.set(worldLevel.pxWidth / 2f, worldLevel.pxHeight / 2f + 20f, camera.position.z)
//        } else {
//            if (levelIdx <= unitTestWorld.allLevels.size - 1) {
//                unitTestWorldLevel = unitTestWorld.allLevels[levelIdx]
//                if (unitTestWorldLevel.hasBgImage) {
//                    unitTWorldBgImage?.texture?.dispose()
//                    val crop = unitTestWorldLevel.bgImageInfos!!.cropRect
//                    unitTWorldBgImage = TextureRegion(
//                        Texture(Gdx.files.internal(unitTestWorldLevel.bgImageInfos!!.relFilePath))
//                    ).apply {
//                        setRegion(crop.x, texture.height - crop.y, crop.w, crop.h)
//                    }
//                }
//            }
//            camera.position.set(
//                unitTestWorldLevel.pxWidth / 2f,
//                unitTestWorldLevel.pxHeight / 2f + 20f,
//                camera.position.z
//            )
//        }
//    }
//
//    private fun renderBgImage(spriteBatch: SpriteBatch, level: Level) {
//        level.bgImageInfos?.let { bgImageInfo ->
//            val bgTexture = if (showWorld) worldBgImage else unitTWorldBgImage
//            bgTexture?.let {
////                spriteBatch.draw(
////                    it,
////                    bgImageInfo.topLeftX.toFloat(),
////                    bgImageInfo.topLeftY.toFloat() - bgImageInfo.cropRect.h, // need to move down due to differing coord systems
////                    0f,
////                    0f,
////                    it.regionWidth.toFloat(),
////                    it.regionHeight.toFloat(),
////                    bgImageInfo.scaleX,
////                    bgImageInfo.scaleY,
////                    0f
//                //       )
//
////                spriteBatch.draw(
////                    it.texture,
////                    bgImageInfo.topLeftX.toFloat(),
////                    bgImageInfo.topLeftY.toFloat() - bgImageInfo.cropRect.h, // need to move down due to differing coord systems
////                    it.regionWidth.toFloat(),
////                    it.regionHeight.toFloat(),
////                    it.u,
////                    it.v,
////                    it.u2,
////                    it.v2)
//
//                spriteBatch.draw(
//                    it.texture,
//                    bgImageInfo.topLeftX.toFloat(),
//                    bgImageInfo.topLeftY.toFloat(),
//                    0f,
//                    0f,
//                    it.regionWidth.toFloat(),
//                    it.regionHeight.toFloat(),
//                    bgImageInfo.scaleX,
//                    bgImageInfo.scaleY,
//                    0f,
//                    it.regionX,
//                    it.regionY,
//                    it.regionWidth,
//                    it.regionHeight,
//                    false,
//                    false
//                )
//            }
//        }
//    }
//
//    private fun renderIntGrid(shapeRenderer: ShapeRenderer, layerIntGrid: LayerIntGrid) {
//        for (cx in 0..layerIntGrid.cWidth) {
//            for (cy in 0..layerIntGrid.cHeight) {
//                if (layerIntGrid.hasValue(cx, cy)) {
//                    val colorHex = layerIntGrid.getColorHex(cx, cy)
//                    val gridSize = layerIntGrid.gridSize.toFloat()
//                    shapeRenderer.color = Color.valueOf(colorHex)
//                    shapeRenderer.rect(cx * gridSize, -cy * gridSize, gridSize, gridSize)
//                }
//            }
//        }
//    }
//
//    override fun pause() {
//    }
//
//    override fun resume() {
//    }
//
//    override fun dispose() {
//    }
//
//}

object GdxTest {
    @JvmStatic
    fun main(arg: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration().apply {
            setWindowedMode(960, 540)
        }
       // Lwjgl3Application(GdxApp(), config)
    }
}

