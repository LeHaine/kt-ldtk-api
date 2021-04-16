package com.lehaine.samples

import com.lehaine.ldtk.LDtkProject


@LDtkProject(ldtkFileLocation = "unitTest.ldtk", name = "UnitTestWorld")
class _UnitTestWorld

fun main(args: Array<String>) {
    // create new LDtk world
    val world = UnitTestWorld()
    world.load()


    // get a level
    val level: UnitTestWorld.UnitTestWorldLevel = world.allLevels[0]

    // iterate over a layers tiles
    level.layerIntGrid_AutoLayer.autoTiles.forEach {
        // logic for handling the tile
    }

    level.layerEntityTest.allMob.forEach { item ->
        if (item.type == UnitTestWorld.Mobs.Shielder) {
            // spawn shielder
        }
    }

    val isGrassTile = level.layerTileTest.tileset.hasTag(UnitTestWorld.CollisionType.Grass.name, 0)
    if (isGrassTile) {
        // play grass sound
    }
}