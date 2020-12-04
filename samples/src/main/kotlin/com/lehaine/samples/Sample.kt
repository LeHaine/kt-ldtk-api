package com.lehaine.samples

import com.lehaine.ldtk.LDtkProject
import com.lehaine.ldtk.Point

// designate class for loading and attaching LDtk file to
@LDtkProject(ldtkFileLocation = "sample.ldtk", name = "World")
class _World

fun main(args: Array<String>) {
    // create new LDtk world
    val world = World()

    // get a level
    val level = world.allLevels[0]

    // iterate over a layers tiles
    level.layer_Background.autoTiles.forEach {
        // logic for handling the tile
    }

    level.layer_Entities.all_Mob.forEach { mob ->
        // access entity fields
        val type: World.MobType = mob.type // generated enum class
        val patrolPoint: Point? = mob.patrol // points
        val health:Int = mob.health
    }

    level.layer_Entities.all_Cart.forEach { cart ->
        // field arrays
        cart.items.forEach { item ->
            if(item == World.Items.Pickaxe) {
                // spawn pickaxe
            }
        }
    }
}