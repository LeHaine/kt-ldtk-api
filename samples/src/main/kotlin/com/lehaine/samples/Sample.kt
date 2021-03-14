package com.lehaine.samples

import com.lehaine.ldtk.LDtkProject
import com.lehaine.ldtk.Point
import com.lehaine.ldtk.Project
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

// designate class for loading and attaching LDtk file to
@LDtkProject(ldtkFileLocation = "sample.ldtk", name = "World")
open class _World(projectFilePath: String)

@LDtkProject(ldtkFileLocation = "unitTest.ldtk", name = "UnitTestWorld")
class _UnitTestWorld

fun main(args: Array<String>) {
    // create new LDtk world
    val world = World()
    world.load()


    // get a level
    val level: World.WorldLevel = world.allLevels[0]

    // iterate over a layers tiles
    level.layerCavern_background.autoTiles.forEach {
        // logic for handling the tile
    }

    // iterate over entities
    level.layerEntities.allMob.forEach { mob ->
        // access entity fields
        val type: World.MobType = mob.type // generated enum class
        // field arrays / lists
        val patrolPoints: List<Point> = mob.patrol // points
        val health: Int = mob.health
    }

    level.layerEntities.allItem.forEach { item ->
        if (item.type == World.Items.Pickaxe) {
            // spawn pickaxe
        }

    }
}