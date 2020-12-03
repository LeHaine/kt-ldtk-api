package com.lehaine.ldtk.processor

import com.google.auto.service.AutoService
import com.lehaine.ldtk.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
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
                val className = if (ldtkProject.name.isBlank()) {
                    "${it.simpleName}_"
                } else {
                    ldtkProject.name
                }
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
            val fileContent = resourcePath.resolve(ldtkFileLocation).toFile().readText()

            val json = LDtkApi.parseLDtkFile(fileContent)

            require(json != null) { "LDtk project file is empty or or missing! Please check to ensure it exists." }

            val fileSpec = FileSpec.builder(pkg, className).indent(FILE_INDENT)
            val projectClassSpec = TypeSpec.classBuilder(className).apply {
                superclass(Project::class)
                addSuperclassConstructorParameter("%S", ldtkFileLocation)
                addInitializerBlock(
                    CodeBlock.builder()
                        .addStatement(
                            "val jsonString = javaClass.classLoader.getResource(%S)?.readText() ?: error(\"Unable to load LDtk file content!\")",
                            ldtkFileLocation
                        )
                        .addStatement("parseJson(jsonString)")
                        .build()
                )
                addFunction(
                    FunSpec.builder("instantiateLevel")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("project", Project::class)
                        .addParameter("json", LevelJson::class)
                        .returns(Level::class)
                        .addStatement("return %L_Level(project, json)", className)
                        .build()
                )
            }

            generateEnums(projectClassSpec, json.defs.enums)
            generateEntities(projectClassSpec, json.defs.entities)
            val tilesets = generateTilesets(projectClassSpec, json.defs.tilesets)
            generateLayers(projectClassSpec, pkg, className, tilesets, json.defs.layers)
            generateLevel(projectClassSpec, pkg, className, json.defs.layers)

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

    private fun generateEntities(projectClassSpec: TypeSpec.Builder, entities: List<EntityDefJson>) {
        entities.forEach { entityDef ->
            val entityClassSpec = TypeSpec.classBuilder("Entity_${entityDef.identifier}").apply {
                superclass(Entity::class)
                addSuperclassConstructorParameter("%N", "json")
            }
            val entityConstructor =
                FunSpec.constructorBuilder()
                    .addParameter("json", EntityInstanceJson::class)

            fun addToEntity(id: String, typeName: TypeName, defaultOverride: Any?, lateinit: Boolean = false) {
                entityClassSpec.addProperty(
                    PropertySpec.builder(
                        id, typeName
                    ).mutable(true).also {
                        if (lateinit) {
                            it.addModifiers(KModifier.LATEINIT)
                        } else {
                            it.initializer("%L", defaultOverride)
                        }
                    }.build()
                )
            }
            entityDef.fieldDefs.forEach {
                val canBeNull = it.canBeNull
                when (val typeName = it.__type) {
                    "Int", "Float", "Bool", "String" -> {
                        val name = if (typeName == "Bool") "Boolean" else typeName
                        val className = ClassName("kotlin", name).copy(canBeNull)
                        val defaultValue = if (typeName == "Int") {
                            (it.defaultOverride?.params?.get(0) as? Double)?.toInt()
                        } else {
                            it.defaultOverride?.params?.get(0)
                        }
                        addToEntity(it.identifier, className, defaultValue)
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
                                addToEntity(it.identifier, className, it.defaultOverride?.params?.get(0), !canBeNull)
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


    private data class TilesetInfo(val typeName: String, val json: TilesetDefJson)

    private fun generateTilesets(
        projectClassSpec: TypeSpec.Builder,
        tilesets: List<TilesetDefJson>
    ): MutableMap<Int, TilesetInfo> {
        val allTilesets = mutableMapOf<Int, TilesetInfo>()
        tilesets.forEach {
            val name = "Tileset_${it.identifier}"
            val tilesetClassSpec = TypeSpec.classBuilder(name).apply {
                superclass(Tileset::class)
                addSuperclassConstructorParameter("%N", "json")

            }
            val tilesetConstructor =
                FunSpec.constructorBuilder()
                    .addParameter("json", TilesetDefJson::class)


            tilesetClassSpec.primaryConstructor(tilesetConstructor.build())
            projectClassSpec.addType(tilesetClassSpec.build())

            allTilesets[it.uid] = TilesetInfo(name, it)
        }
        return allTilesets
    }

    private fun generateLayers(
        projectClassSpec: TypeSpec.Builder,
        pkg: String,
        className: String,
        tilesets: MutableMap<Int, TilesetInfo>,
        layers: List<LayerDefJson>
    ) {
        layers.forEach { layerDef ->
            val layerClassSpec = TypeSpec.classBuilder("Layer_${layerDef.identifier}")
            val layerConstructor = FunSpec.constructorBuilder()

            fun extendLayerClass(superClass: KClass<*>) {
                layerClassSpec.run {
                    superclass(superClass)
                    if (superClass == LayerIntGrid::class || superClass == LayerIntGridAutoLayer::class) {
                        if (superClass == LayerIntGridAutoLayer::class) {
                            layerConstructor.addParameter(
                                "tilesetDefJson",
                                TilesetDefJson::class.asTypeName().copy(nullable = true)
                            )
                            addSuperclassConstructorParameter("%N", "tilesetDefJson")
                        }
                        layerConstructor.addParameter(
                            "intGridValues",
                            List::class.asTypeName().parameterizedBy(IntGridValue::class.asTypeName())
                        )
                        addSuperclassConstructorParameter("%N", "intGridValues")
                    }
                    layerConstructor.addParameter("json", LayerInstanceJson::class)
                    addSuperclassConstructorParameter("%N", "json")
                }
            }

            when (layerDef.type) {
                "IntGrid" -> {
                    if (layerDef.autoTilesetDefUid == null) {
                        // IntGrid
                        extendLayerClass(LayerIntGrid::class)

                    } else {
                        // Auto-layer IntGrid
                        extendLayerClass(LayerIntGridAutoLayer::class)
                        val tileset = tilesets[layerDef.autoTilesetDefUid]
                        if (tileset != null) {
                            val tilesetType = ClassName.bestGuess(tileset.typeName).copy(nullable = true)
                            layerClassSpec.addProperty(
                                PropertySpec.builder("tileset", tilesetType)
                                    .initializer("%L(%N!!)", tileset.typeName, "tilesetDefJson")
                                    .build()
                            )
                            layerClassSpec.addFunction(
                                FunSpec.builder("getTileset").returns(Tileset::class.asTypeName().copy(true))
                                    .addModifiers(KModifier.OVERRIDE)
                                    .addStatement("return tileset").build()
                            )
                        }
                    }
                }
                "AutoLayer" -> {
                    extendLayerClass(LayerAutoLayer::class)
                }
                "Entities" -> {
                    extendLayerClass(LayerEntities::class)

                    layerClassSpec.addInitializerBlock(
                        CodeBlock.builder().addStatement("instantiateEntities(\"%L.%L\")", pkg, className).build()
                    )
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


    private fun generateLevel(
        projectClassSpec: TypeSpec.Builder,
        pkg: String,
        className: String,
        layers: List<LayerDefJson>
    ) {
        val levelClassSpec = TypeSpec.classBuilder("${className}_Level").apply {
            superclass(Level::class)
            addSuperclassConstructorParameter("\"%L.%L\"", pkg, className)
            addSuperclassConstructorParameter("%N", "project")
            addSuperclassConstructorParameter("%N", "json")
        }
        val levelConstructor =
            FunSpec.constructorBuilder()
                .addParameter("project", Project::class)
                .addParameter("json", LevelJson::class)

        levelClassSpec.primaryConstructor(levelConstructor.build())

        layers.forEach {
            levelClassSpec.addProperty(
                PropertySpec.builder(
                    "layer_${it.identifier}",
                    ClassName.bestGuess("Layer_${it.identifier}")
                ).initializer("resolveLayer(%S) as Layer_%N", it.identifier, it.identifier).build()
            )
        }
        projectClassSpec.addType(levelClassSpec.build())
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        const val FILE_INDENT = "    "
    }
}