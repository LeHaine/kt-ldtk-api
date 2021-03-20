package com.lehaine.ldtk

import com.soywiz.korio.lang.Charsets
import com.soywiz.korio.lang.toString

open class Level(val project: Project, val definition: LevelDefinition) {
    enum class NeighborDirection {
        North,
        South,
        West,
        East;

        companion object {
            fun fromDir(dir: String): NeighborDirection {
                return when (dir.toLowerCase()) {
                    "n" -> North
                    "e" -> East
                    "s" -> South
                    "w" -> West
                    else -> {
                        println("WARNING: unknown neighbor level direction: $dir")
                        North
                    }
                }
            }
        }
    }

    data class Neighbor(val levelUid: Int, val dir: NeighborDirection)

    data class CropRect(val x: Float, val y: Float, val w: Float, val h: Float);

    data class LevelBgImage(
        val relFilePath: String,
        val topLeftX: Int,
        val topLeftY: Int,
        val scaleX: Float,
        val scaleY: Float,
        val cropRect: CropRect
    )

    var uid = definition.uid
        private set
    var identifier = definition.identifier
        private set
    var pxWidth = definition.pxWid
        private set
    var pxHeight = definition.pxHei
        private set
    var worldX = definition.worldX
        private set
    var worldY = definition.worldY
        private set
    var bgColor = Project.hexToInt(definition.bgColor)
        private set
    val hasBgImage: Boolean
        get() = bgImageInfos != null
    var bgImageInfos: LevelBgImage? = if (definition.bgRelPath.isNullOrEmpty() || definition.bgPos == null) {
        null
    } else {
        LevelBgImage(
            relFilePath = definition.bgRelPath,
            topLeftX = definition.bgPos.topLeftPx[0],
            topLeftY = definition.bgPos.topLeftPx[1],
            scaleX = definition.bgPos.scale[0],
            scaleY = definition.bgPos.scale[1],
            cropRect = CropRect(
                x = definition.bgPos.cropRect[0],
                y = definition.bgPos.cropRect[1],
                w = definition.bgPos.cropRect[2],
                h = definition.bgPos.cropRect[3]
            )
        )
    }
        private set

    private val _allUntypedLayers = mutableListOf<Layer>()
    val allUntypedLayers get() = _allUntypedLayers

    private var entityLayer: LayerEntities? = null

    val allUntypedEntities get() = entityLayer?.entities

    private val _neighbors = mutableListOf<Neighbor>()
    val neighors get() = _neighbors.toList()

    /**
     * Only exists if levels are stored in separate level files.
     */
    private var externalRelPath = definition.externalRelPath

    init {
        definition.layerInstances?.forEach { layerInstanceJson ->
            instantiateLayer(layerInstanceJson)?.also { _allUntypedLayers.add(it) }
        }
        definition.neighbours?.forEach {
            _neighbors.add(Neighbor(it.levelUid, NeighborDirection.fromDir(it.dir)))
        }
    }

    fun isLoaded() = externalRelPath == null || !allUntypedLayers.isNullOrEmpty()

    fun load(): Boolean {
        if (isLoaded()) {
            return true
        }
        val relPath = externalRelPath ?: return false
        val jsonString = project.getAsset(relPath).toString(Charsets.UTF8)

        initJson(jsonString)
        return true
    }

    suspend fun loadAsync(): Boolean {
        if (isLoaded()) {
            return true
        }
        val relPath = externalRelPath ?: return false
        val jsonString = project.getAssetAsync(relPath).toString(Charsets.UTF8)

        initJson(jsonString)
        return true
    }

    private fun initJson(jsonString: String) {
        val json =
            LDtkApi.parseLDtkLevelFile(jsonString) ?: error("Unable to parse Level JSON")

        uid = json.uid
        identifier = json.identifier
        pxWidth = json.pxWid
        pxHeight = json.pxHei
        worldX = json.worldX
        worldY = json.worldY
        bgColor = Project.hexToInt(json.bgColor)

        bgImageInfos = if (json.bgRelPath.isNullOrEmpty() || json.bgPos == null) {
            null
        } else {
            LevelBgImage(
                relFilePath = json.bgRelPath,
                topLeftX = json.bgPos.topLeftPx[0],
                topLeftY = json.bgPos.topLeftPx[1],
                scaleX = json.bgPos.scale[0],
                scaleY = json.bgPos.scale[1],
                cropRect = CropRect(
                    x = json.bgPos.cropRect[0],
                    y = json.bgPos.cropRect[1],
                    w = json.bgPos.cropRect[2],
                    h = json.bgPos.cropRect[3]
                )
            )
        }
        externalRelPath = json.externalRelPath

        json.layerInstances?.forEach { layerInstanceJson ->
            instantiateLayer(layerInstanceJson)?.also { _allUntypedLayers.add(it) }
        }
        json.neighbours?.forEach {
            _neighbors.add(Neighbor(it.levelUid, NeighborDirection.fromDir(it.dir)))
        }
    }

    fun resolveLayer(id: String): Layer {
        load()
        return allUntypedLayers.find { it.identifier == id } ?: error("Unable to find $id layer")
    }

    /**
     * This function will be overridden in the ProjectProcessor if used.
     */
    protected open fun instantiateLayer(json: LayerInstance): Layer? {
        return when (json.type) { //IntGrid, Entities, Tiles or AutoLayer
            "IntGrid" -> {
                val intGridValues = project.getLayerDef(json.layerDefUid)!!.intGridValues
                LayerIntGrid(intGridValues, json)
            }
            "Entities" -> {
                entityLayer = LayerEntities(json).apply {
                    instantiateEntities()
                }
                entityLayer
            }
            "Tiles" -> {
                val tilesetDef = project.getTilesetDef(json.tilesetDefUid)!!
                LayerTiles(tilesetDef, json)
            }
            "AutoLayer" -> {
                val tilesetDef = project.getTilesetDef(json.tilesetDefUid)!!
                LayerAutoLayer(tilesetDef, json)
            }
            else -> error("Unable to instantiate layer for level $identifier")
        }
    }

    override fun toString(): String {
        return "Level(project=$project, uid=$uid, identifier='$identifier', pxWidth=$pxWidth, pxHeight=$pxHeight, worldX=$worldX, worldY=$worldY, bgColor=$bgColor, _allUntypedLayers=$_allUntypedLayers, _neighbors=$_neighbors)"
    }


}