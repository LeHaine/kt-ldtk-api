package com.lehaine.samples

import com.lehaine.ldtk.Project

/**
 * A sample not using the ldtk-processor.
 */
fun main(args: Array<String>) {
    val proj = Project("sample.ldtk")
    val level = proj.allUntypedLevels[0]
    level.load() // this is only needed if levels are saved in separate files!
    level.allUntypedEntities?.forEach {
        it.json
        println(it)
    }
    level.definition.fieldInstances.forEach {
        println(it.value)
    }
}