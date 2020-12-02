package com.lehaine.ldtk.processor

import com.google.auto.service.AutoService
import com.lehaine.ldtk.Entity
import com.lehaine.ldtk.EntityInstanceJson
import com.lehaine.ldtk.LDtkApi
import com.lehaine.ldtk.LDtkProject
import com.squareup.kotlinpoet.*
import java.io.File
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation

@AutoService(Processor::class)
class ProjectProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(LDtkProject::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(LDtkProject::class.java)
            .forEach {
                val ldtkProject = it.getAnnotation(LDtkProject::class.java)
                val ldtkFileLocation = ldtkProject.ldtkFileLocation
                val className = it.simpleName.toString()
                println("Processing $className")
                val pkg = processingEnv.elementUtils.getPackageOf(it).toString()
                generateProject(className, pkg, ldtkFileLocation)
            }
        return true
    }

    private fun generateProject(className: String, pkg: String, ldtkFileLocation: String) {
        try {
            val resource = processingEnv.filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "tmp", null)
            val path = Paths.get(resource.toUri()).resolve("../../../../../") // major hack
            resource.delete()
            val resourcePath = path.resolve("resources/main")
            val fileText = resourcePath.resolve(ldtkFileLocation).toFile().readText()

            val json = LDtkApi.parseLDtkFile(fileText)

            require(json != null) { "LDtk project file is empty or or missing! Please check to ensure it exists." }

            val fileName = "${className}_"

            val fileSpec = FileSpec.builder(pkg, fileName)
            val projectClassSpec = TypeSpec.classBuilder(fileName)
            json.defs.enums.forEach { enumDef ->
                val typeSpec = TypeSpec.enumBuilder(enumDef.identifier)
                enumDef.values.forEach {
                    typeSpec.addEnumConstant(it.id)
                }
                projectClassSpec.addType(typeSpec.build())
            }

            json.defs.layers.forEach { layerDef ->
                when (layerDef.type) {
                    "IntGrid" -> {
                    }
                    "AutoLayer" -> {
                    }
                    "Entities" -> {
                        json.defs.entities.forEach { entityDef ->
                            val entityClassSpec = TypeSpec.classBuilder("Entity_${entityDef.identifier}").apply {
                                superclass(Entity::class)
                                addSuperclassConstructorParameter("%N", "json")
                            }
                            val entityConstructor =
                                FunSpec.constructorBuilder()
                                    .addParameter("json", EntityInstanceJson::class)
                            entityDef.fieldDefs.forEach {
                                when (val typeName = it.__type) {
                                    "Int", "Float", "Bool", "String" -> {
                                        entityConstructor.addParameter(it.identifier, ClassName.bestGuess(typeName))
                                        entityClassSpec.addProperty(
                                            PropertySpec.builder(
                                                it.identifier,
                                                ClassName.bestGuess(typeName)
                                            ).initializer(it.identifier)
                                                .build()
                                        )
                                    }
                                    "Color" -> {
                                    }
                                    "Point" -> {
                                    }
                                    else -> { // field type is an enum
                                        when {
                                            "LocalEnum." in typeName -> {
                                                val type = typeName.substring(typeName.indexOf(".") + 1)
                                                entityConstructor.addParameter(it.identifier, ClassName.bestGuess(type))
                                                entityClassSpec.addProperty(
                                                    PropertySpec.builder(
                                                        it.identifier,
                                                        ClassName.bestGuess(type)
                                                    ).initializer(it.identifier).build()
                                                )
                                            }
                                            "ExternEnum." in typeName -> {

                                            }
                                            else -> {
                                                error("Unknown field type $typeName")
                                            }
                                        }
                                    }
                                }
                            }
                            entityClassSpec.primaryConstructor(entityConstructor.build())
                            projectClassSpec.addType(entityClassSpec.build())

                        }
                    }
                    "Tiles" -> {
                    }
                    else -> {
                        error("Unknown layer type ${layerDef.type}")
                    }

                }

            }


            fileSpec.addType(projectClassSpec.build())
            val file = fileSpec.build()

            val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
            require(kaptKotlinGeneratedDir != null) { "Unable to find kapt kotlin generated directory." }
            file.writeTo(File(kaptKotlinGeneratedDir))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        val TYPE_TO_CLASS = mapOf(
            "Int" to Int::class,
            "Float" to Float::class,
            "Bool" to Boolean::class,
            "String" to String::class
        )
    }
}