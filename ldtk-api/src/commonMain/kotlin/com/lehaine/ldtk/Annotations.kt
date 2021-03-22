package com.lehaine.ldtk

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class LDtkProject(
    val ldtkFileLocation: String,
    val name: String = "",
)