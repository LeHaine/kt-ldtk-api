package com.lehaine.samples

import com.lehaine.ldtk.LDtkProject

@LDtkProject(ldtkFileLocation = "sample.ldtk", name = "World")
class _World

fun main(args: Array<String>) {

    val world = World()
    println(world.allUntypedLevels)
}