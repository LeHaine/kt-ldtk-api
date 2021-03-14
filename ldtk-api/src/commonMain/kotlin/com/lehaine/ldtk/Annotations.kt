package com.lehaine.ldtk

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class LDtkProject(
    val ldtkFileLocation: String,
    val name: String = "",
    val extendClass: KClass<out Project> = Project::class
)