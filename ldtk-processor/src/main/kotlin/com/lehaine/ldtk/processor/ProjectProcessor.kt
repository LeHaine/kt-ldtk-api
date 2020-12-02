package com.lehaine.ldtk.processor

import com.google.auto.service.AutoService
import com.lehaine.ldtk.*
import com.squareup.kotlinpoet.*
import java.io.File
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation
import kotlin.reflect.KClass

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

            generateEnums(projectClassSpec, json.defs.enums)
            generateEntities(projectClassSpec, json.defs.entities)
            generateLayers(projectClassSpec, json.defs.layers)

            fileSpec.addType(projectClassSpec.build())
            val file = fileSpec.build()

            val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
            require(kaptKotlinGeneratedDir != null) { "Unable to find kapt kotlin generated directory." }
            file.writeTo(File(kaptKotlinGeneratedDir))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateEnums(projectClassSpec: TypeSpec.Builder, enums: List<EnumDefJson>) {
        enums.forEach { enumDef ->
            val typeSpec = TypeSpec.enumBuilder(enumDef.identifier)
            enumDef.values.forEach {
                typeSpec.addEnumConstant(it.id)
            }
            projectClassSpec.addType(typeSpec.build())
        }
    }

    private fun generateLayers(projectClassSpec: TypeSpec.Builder, layers: List<LayerDefJson>) {
        layers.forEach { layerDef ->
            val layerClassSpec = TypeSpec.classBuilder("Layer_${layerDef.identifier}")
            val layerConstructor =
                FunSpec.constructorBuilder()
                    .addParameter("json", LayerInstanceJson::class)

            fun extendLayerClass(superClass: KClass<*>) {
                layerClassSpec.run {
                    superclass(superClass)
                    addSuperclassConstructorParameter("%N", "json")
                }
            }

            when (layerDef.type) {
                "IntGrid" -> {
                    extendLayerClass(LayerIntGrid::class)
                }
                "AutoLayer" -> {
                    extendLayerClass(LayerIntGridAutoLayer::class)
                }
                "Entities" -> {
                    extendLayerClass(LayerEntities::class)
                }
                "Tiles" -> {
                    extendLayerClass(LayerTiles::class)
                }
                else -> {
                    error("Unknown layer type ${layerDef.type}")
                }

            }

            layerClassSpec.primaryConstructor(layerConstructor.build())
            projectClassSpec.addType(layerClassSpec.build())
        }
    }

    private fun generateEntities(projectClassSpec: TypeSpec.Builder, entities: List<EntityDefJson>) {
        entities.forEach { entityDef ->
            val entityClassSpec = TypeSpec.classBuilder("Entity_${entityDef.identifier}").apply {
                superclass(Entity::class)
                addSuperclassConstructorParameter("%N", "json")
            }
            val entityConstructor =
                FunSpec.constructorBuilder()
                    .addParameter("json", EntityInstanceJson::class)

            fun addToEntity(id: String, typeName: TypeName) {
                entityConstructor.addParameter(id, typeName)
                entityClassSpec.addProperty(
                    PropertySpec.builder(
                        id, typeName
                    ).initializer(id)
                        .build()
                )
            }
            entityDef.fieldDefs.forEach {
                val canBeNull = it.canBeNull
                when (val typeName = it.__type) {
                    "Int", "Float", "Bool", "String" -> {
                        val name = if (typeName == "Bool") "Boolean" else typeName
                        val className = ClassName("kotlin", name).copy(canBeNull)
                        addToEntity(it.identifier, className)
                    }
                    "Color" -> {
                    }
                    "Point" -> {
                    }
                    else -> { // field type is an enum
                        when {
                            "LocalEnum." in typeName -> {
                                val type = typeName.substring(typeName.indexOf(".") + 1)
                                val className = ClassName.bestGuess(type).copy(canBeNull)
                                addToEntity(it.identifier, className)
                            }
                            "ExternEnum." in typeName -> {
                                error("ExternEnums are not supported!")
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

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}