package com.lehaine.ldtk

@Target(AnnotationTarget.CLASS)
annotation class LDtkProject(val ldtkFileLocation: String, val name: String = "")