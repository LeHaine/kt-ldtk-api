package com.lehaine.ldtk.processor

import com.google.auto.service.AutoService
import com.lehaine.ldtk.*
import com.lehaine.ldtk.LDtkApi.ENTITY_PREFIX
import com.lehaine.ldtk.LDtkApi.LAYER_PREFIX
import com.lehaine.ldtk.LDtkApi.LEVEL_SUFFIX
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import kotlin.reflect.KClass

@AutoService(Processor::class)
open class ProjectProcessor : AbstractProcessor() {

    open fun baseLayerAutoLayerClass(): Class<out LayerAutoLayer> {
        return LayerAutoLayer::class.java
    }

    open fun baseLayerIntGridClass(): Class<out LayerIntGrid> {
        return LayerIntGrid::class.java
    }

    open fun baseLayerIntGridAutoLayerClass(): Class<out LayerIntGridAutoLayer> {
        return LayerIntGridAutoLayer::class.java
    }

    open fun baseLayerTilesClass(): Class<out LayerTiles> {
        return LayerTiles::class.java
    }

    open fun baseTilesetClass(): Class<out Tileset> {
        return Tileset::class.java
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(LDtkProject::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    @KotlinPoetMetadataPreview
    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(LDtkProject::class.java)
            .forEach {
                val ldtkProject = it.getAnnotation(LDtkProject::class.java)
                var extendClass: TypeName? = null
                try {
                    ldtkProject.extendClass
                } catch (ex: MirroredTypeException) {
                    extendClass = ex.typeMirror.asTypeName()
                }

                val ldtkFileLocation = ldtkProject.ldtkFileLocation
                val className = if (ldtkProject.name.isBlank()) {
                    "${it.simpleName}_"
                } else {
                    ldtkProject.name
                }

                val pkg = processingEnv.elementUtils.getPackageOf(it).toString()
                generateProject(className, pkg, ldtkFileLocation, extendClass!!)
            }
        return true
    }

    private fun generateProject(className: String, pkg: String, ldtkFileLocation: String, extendClass: TypeName) {
        try {
            val resource =
                processingEnv.options["kapt.kotlin.generated"] ?: error("Unable to find kotlin generated path")
            val resourcePath = Paths.get(resource)
            val filePath = findResourcePath(resourcePath, resourcePath, ldtkFileLocation)
            val fileContent = filePath.toFile().readText()

            val json = LDtkApi.parseLDtkFile(fileContent)

            require(json != null) { "LDtk project file is empty or or missing! Please check to ensure it exists." }

            val fileSpec = FileSpec.builder(pkg, className).indent(FILE_INDENT)
            val projectClassSpec = TypeSpec.classBuilder(className).apply {
                superclass(extendClass)
                addSuperclassConstructorParameter("%S", ldtkFileLocation)
                addFunction(
                    FunSpec.builder("instantiateLevel")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("json", LevelDefinition::class)
                        .returns(Level::class)
                        .addStatement("return %L$LEVEL_SUFFIX(this, json)", className)
                        .build()
                )
            }

            generateEnums(projectClassSpec, json.defs.enums)
            val instantiateEntityFun = generateEntities(projectClassSpec, pkg, className, json.defs.entities)
            val tilesets = generateTilesets(projectClassSpec, json.defs.tilesets)
            val instantiateLayerFun = generateLayers(
                projectClassSpec,
                pkg,
                className,
                tilesets,
                json.defs.entities,
                json.defs.layers,
                instantiateEntityFun
            )
            generateLevel(projectClassSpec, pkg, className, json.defs.layers, instantiateLayerFun)

            val levelClassType = ClassName.bestGuess("${className}$LEVEL_SUFFIX")
            // all levels list property
            projectClassSpec.addProperty(
                PropertySpec.builder(
                    "allLevels",
                    List::class.asTypeName().parameterizedBy(levelClassType)
                ).getter(
                    FunSpec.getterBuilder().addCode(
                        CodeBlock.builder()
//                            .beginControlFlow("allUntypedLevels.map")
//                            .addStatement("it as %T", levelClassType)
//                            .endControlFlow()
                            .addStatement("return allUntypedLevels as List<%T>", levelClassType)
                            .build()
                    ).build()
                ).build()
            )

            fileSpec.addType(projectClassSpec.build())
            val file = fileSpec.build()

            val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
            require(kaptKotlinGeneratedDir != null) { "Unable to find kapt kotlin generated directory." }
            file.writeTo(File(kaptKotlinGeneratedDir))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val resourcePaths = listOf("resources/main/", "processedResources/jvm/main/", "processedResources/js/main/")

    private fun findResourcePath(
        currenPath: Path,
        originalPath: Path,
        ldtkFileLocation: String,
        maxPath: Int = 15,
        currentPathIdx: Int = 0,
        pathCheckIdx: Int = 0
    ): Path {
        if (pathCheckIdx >= resourcePaths.size) {
            val fileName = ldtkFileLocation.split("/").last()
            error("Unable to find resource file $fileName in any of these locations: $resourcePaths. Aborting...")
        }
        val newPath = currenPath.resolve("./${resourcePaths[pathCheckIdx]}${ldtkFileLocation}")
        if (!Files.exists(newPath)) {
            newPath.resolve("./$ldtkFileLocation")
        }
        return if (Files.exists(newPath)) {
            newPath
        } else {
            var newPathIdx = pathCheckIdx
            var newCurrentPathIdx = currentPathIdx + 1
            var newCurrentPath = currenPath.resolve("../")
            if (currentPathIdx >= maxPath) {
                newPathIdx += 1
                newCurrentPathIdx = 0
                newCurrentPath = originalPath
            }
            findResourcePath(
                newCurrentPath,
                originalPath,
                ldtkFileLocation,
                currentPathIdx = newCurrentPathIdx,
                pathCheckIdx = newPathIdx
            )
        }
    }


    private fun generateEnums(projectClassSpec: TypeSpec.Builder, enums: List<EnumDefinition>) {
        enums.forEach { enumDef ->
            val typeSpec = TypeSpec.enumBuilder(enumDef.identifier)
            enumDef.values.forEach {
                typeSpec.addEnumConstant(it.id)
            }
            projectClassSpec.addType(typeSpec.build())
        }
    }

    private fun generateEntities(
        projectClassSpec: TypeSpec.Builder, pkg: String, projClassName: String,
        entities: List<EntityDefinition>
    ): FunSpec.Builder {
        val instantiateLayerFun = FunSpec.builder("instantiateEntity")
            .addParameter("json", EntityInstance::class)
            .addModifiers(KModifier.OVERRIDE, KModifier.PROTECTED)
            .returns(Entity::class.asTypeName().copy(nullable = true))

        entities.forEach { entityDef ->
            val entityClassName = "$ENTITY_PREFIX${entityDef.identifier}"
            val entityClassSpec = TypeSpec.classBuilder(entityClassName).apply {
                superclass(Entity::class.java)
                addSuperclassConstructorParameter("%N", "json")
            }
            val entityConstructor =
                FunSpec.constructorBuilder()
                    .addParameter("json", EntityInstance::class)

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

            val fields = mutableListOf<String>()
            val arrayReg = "Array<(.*)>".toRegex()
            instantiateLayerFun
                .beginControlFlow("if (\"${entityDef.identifier}\" == json.identifier)")
                .addStatement("val entity = %L.%L.${entityClassName}(json)", pkg, projClassName)
                .beginControlFlow("json.fieldInstances.forEach")
            entityDef.fieldDefs.forEach { fieldDefJson ->
                instantiateLayerFun.beginControlFlow("if(\"${fieldDefJson.identifier}\" == it.identifier)")
                val canBeNull = fieldDefJson.canBeNull
                val isArray = arrayReg.matches(fieldDefJson.type)
                val typeName =
                    if (isArray) arrayReg.find(fieldDefJson.type)!!.groupValues[1] else fieldDefJson.type
                val name = if (typeName == "Bool") "Boolean" else if (typeName == "Float") "Double" else typeName
                val entityFieldName = "entity.${fieldDefJson.identifier}"
                val privateEntityFieldName = "entity._${fieldDefJson.identifier}"
                if (isArray) {
                    val fieldClassType = when (name) {
                        "Int", "Double", "Boolean", "String" -> {

                            instantiateLayerFun.addStatement("val arrList = it.value!!.stringList!!")
                            if (name == "String") {
                                instantiateLayerFun.addStatement("$privateEntityFieldName.addAll(arrList)")
                            } else {
                                instantiateLayerFun.addStatement("$privateEntityFieldName.addAll(arrList.map { it.to$name() })")
                            }
                            ClassName("kotlin", name)
                        }
                        "Point", "Color" -> {
                            if (name == "Color") {
                                instantiateLayerFun.addStatement("val arrList = it.value!!.stringList!!")
                                    .beginControlFlow("val result = arrList.map")
                                    .addStatement("val value = Project.hexToInt(it)")
                                    .addStatement("Color(value, it)")
                                    .endControlFlow()
                                    .addStatement("$privateEntityFieldName.addAll(result)")
                            } else {
                                instantiateLayerFun.addStatement("val arrList = it.value!!.stringMapList!!")
                                    .beginControlFlow("val result = arrList.map")
                                    .addStatement("val cx = it[\"cx\"]!!.toInt()")
                                    .addStatement("val cy = it[\"cy\"]!!.toInt()")
                                    .addStatement("Point(cx, cy)")
                                    .endControlFlow()
                                    .addStatement("$privateEntityFieldName.addAll(result)")
                            }
                            ClassName("com.lehaine.ldtk", name)
                        }
                        else -> {
                            when {
                                "LocalEnum." in name -> {
                                    val enumName = typeName.substring(typeName.indexOf(".") + 1)
                                    instantiateLayerFun.addStatement("val arrList = it.value!!.stringList!!")
                                        .beginControlFlow("val result = arrList.map")
                                        .addStatement("$enumName.valueOf(it)")
                                        .endControlFlow()
                                        .addStatement("$privateEntityFieldName.addAll(result)")
                                    ClassName.bestGuess(enumName)
                                }
                                "ExternEnum." in name -> {
                                    error("ExternEnums are not supported!")
                                }
                                else -> {
                                    error("Unsupported Array type ${fieldDefJson.type}")
                                }
                            }
                        }
                    }
                    entityClassSpec.addProperty(
                        PropertySpec.builder(
                            "_${fieldDefJson.identifier}",
                            ClassName("kotlin.collections", "MutableList").parameterizedBy(fieldClassType)
                        ).initializer(
                            CodeBlock.builder()
                                .addStatement("mutableListOf()")
                                .build()
                        ).build()
                    )
                    entityClassSpec.addProperty(
                        PropertySpec.builder(
                            fieldDefJson.identifier,
                            List::class.asTypeName().parameterizedBy(fieldClassType)
                        ).getter(
                            FunSpec.getterBuilder()
                                .addStatement("return _${fieldDefJson.identifier}.toList()")
                                .build()
                        ).build()
                    )
                    fields.add(fieldDefJson.identifier)
                } else {
                    when (name) {
                        "Int", "Double", "Boolean", "String" -> {
                            if (name == "String") {
                                instantiateLayerFun.addStatement("$entityFieldName = it.value!!.content!!")
                            } else {
                                instantiateLayerFun.addStatement("$entityFieldName = it.value!!.content!!.to$name()")
                            }
                            val className = ClassName("kotlin", name).copy(canBeNull)
                            val defaultOverride =
                                fieldDefJson.defaultOverride?.params?.get(0)?.content
                            val defaultValue = when {
                                defaultOverride != null -> {
                                    when (name) {
                                        "Int" -> defaultOverride.toInt()
                                        "Double" -> defaultOverride.toDouble()
                                        "Boolean" -> defaultOverride.toBoolean()
                                        "String" -> defaultOverride
                                        else -> error("Unsupported primitive field $typeName!")
                                    }
                                }
                                canBeNull -> {
                                    null
                                }
                                !canBeNull -> {
                                    when (name) {
                                        "Int" -> 0
                                        "Double" -> 0.0
                                        "Boolean" -> false
                                        "String" -> "\"\""
                                        else -> error("Unsupported primitive field $typeName!")
                                    }
                                }
                                else -> {
                                    error("Unable to set default value for $typeName")
                                }
                            }
                            fields.add(fieldDefJson.identifier)
                            addToEntity(fieldDefJson.identifier, className, defaultValue)
                        }
                        "FilePath" -> {
                            // TODO impl filepath?
                        }
                        "Color" -> {
                            val defaultValue =
                                fieldDefJson.defaultOverride?.params?.get(0)?.content?.toIntOrNull()
                            val intDefaultValue = defaultValue ?: 0
                            val hexDefaultValue = defaultValue?.let { "\"${Project.intToHex(it)}\"" }
                                ?: "\"#000000\""
                            val colorDefault = Color(intDefaultValue, hexDefaultValue)
                            val colorClass = ClassName("com.lehaine.ldtk", "Color").copy(nullable = canBeNull)

                            addToEntity(fieldDefJson.identifier, colorClass, colorDefault)
                            fields.add(fieldDefJson.identifier)

                            instantiateLayerFun.addStatement("val value = Project.hexToInt(it.value!!.content!!)")
                                .addStatement("val hexValue = it.value!!.content!!")
                                .addStatement("$entityFieldName = Color(value, hexValue)")

                        }
                        "Point" -> {
                            val className = ClassName("com.lehaine.ldtk", "Point").copy(canBeNull)
                            val defaultValue = fieldDefJson.defaultOverride?.params?.get(0) ?: "Point(0, 0)"
                            addToEntity(fieldDefJson.identifier, className, defaultValue)
                            fields.add(fieldDefJson.identifier)

                            instantiateLayerFun
                                .beginControlFlow("if (it.value != null)")
                                .addStatement("val map = it.value!!.stringMap!!")
                                .addStatement("val cx = map[\"cx\"]!!.toInt()")
                                .addStatement("val cy = map[\"cy\"]!!.toInt()")
                                .addStatement("$entityFieldName = Point(cx, cy)")
                                .endControlFlow()
                        }
                        else -> { // field type is an enum
                            when {
                                "LocalEnum." in typeName -> {
                                    val type = typeName.substring(typeName.indexOf(".") + 1)
                                    val className = ClassName.bestGuess(type).copy(canBeNull)
                                    fields.add(fieldDefJson.identifier)
                                    addToEntity(
                                        fieldDefJson.identifier,
                                        className,
                                        fieldDefJson.defaultOverride?.params?.get(0),
                                        !canBeNull
                                    )

                                    instantiateLayerFun.addStatement("$entityFieldName = $type.valueOf(it.value!!.content!!)")
                                }
                                "ExternEnum." in typeName -> {
                                    error("ExternEnums are not supported!")
                                }
                                else -> {
                                    error("Unsupported field type $typeName")
                                }
                            }
                        }
                    }
                }
                instantiateLayerFun
                    .endControlFlow()
            }

            instantiateLayerFun
                .endControlFlow()
                .addStatement("_all${entityDef.identifier}.add(entity)")
                .addStatement("return entity")
                .endControlFlow()
            var stringStatment = ""
            fields.forEachIndexed { index, s ->
                stringStatment += "$s=\${$s}"
                if (index != fields.size - 1) {
                    stringStatment += ",·"
                }
            }
            entityClassSpec.addFunction(
                FunSpec.builder("toString").addModifiers(KModifier.OVERRIDE)
                    .returns(String::class)
                    .addStatement("return \"%L(%L,·\${entityInfoString()})\"", entityClassName, stringStatment).build()
            )
            entityClassSpec.primaryConstructor(entityConstructor.build())
            projectClassSpec.addType(entityClassSpec.build())
        }
        instantiateLayerFun.addStatement("return null")
        return instantiateLayerFun
    }

    private data class TilesetInfo(val typeName: String, val json: TilesetDefinition)

    private fun generateTilesets(
        projectClassSpec: TypeSpec.Builder,
        tilesets: List<TilesetDefinition>
    ): MutableMap<Int, TilesetInfo> {
        val allTilesets = mutableMapOf<Int, TilesetInfo>()
        tilesets.forEach {
            val name = "Tileset_${it.identifier}"
            val tilesetClassSpec = TypeSpec.classBuilder(name).apply {
                superclass(baseTilesetClass())
                addSuperclassConstructorParameter("%N", "json")

            }
            val tilesetConstructor =
                FunSpec.constructorBuilder()
                    .addParameter("json", TilesetDefinition::class)


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
        entities: List<EntityDefinition>,
        layers: List<LayerDefinition>,
        instantiateEntityFun: FunSpec.Builder
    ): FunSpec.Builder {
        val instantiateLayerFun = FunSpec.builder("instantiateLayer")
            .addParameter("json", LayerInstance::class)
            .addModifiers(KModifier.OVERRIDE, KModifier.PROTECTED)
            .returns(Layer::class.asTypeName().copy(nullable = true))

        layers.forEach { layerDef ->
            val layerName = "$LAYER_PREFIX${layerDef.identifier}"
            val layerClassSpec = TypeSpec.classBuilder(layerName)
            val layerConstructor = FunSpec.constructorBuilder()

            instantiateLayerFun
                .beginControlFlow("if (\"${layerDef.identifier}\" == json.identifier)")


            fun extendLayerClass(superClass: KClass<*>) {
                layerClassSpec.run {
                    superclass(superClass)

                    when (superClass) {
                        baseLayerIntGridClass().kotlin, baseLayerIntGridAutoLayerClass().kotlin -> {
                            if (superClass == baseLayerIntGridAutoLayerClass().kotlin) {
                                layerConstructor.addParameter(
                                    "tilesetDefJson",
                                    TilesetDefinition::class.asTypeName().copy(nullable = true)
                                )
                                addSuperclassConstructorParameter("%N", "tilesetDefJson")
                            }
                            layerConstructor.addParameter(
                                "intGridValues",
                                List::class.asTypeName().parameterizedBy(IntGridValueDefinition::class.asTypeName())
                            )
                            addSuperclassConstructorParameter("%N", "intGridValues")
                        }
                        baseLayerAutoLayerClass().kotlin -> {
                            layerConstructor.addParameter(
                                "tilesetDefJson",
                                TilesetDefinition::class.asTypeName().copy(nullable = true)
                            )
                            addSuperclassConstructorParameter("%N", "tilesetDefJson")
                        }
                        baseLayerTilesClass().kotlin -> {
                            layerConstructor.addParameter(
                                "tilesetDefJson",
                                TilesetDefinition::class.asTypeName()
                            )
                            addSuperclassConstructorParameter("%N", "tilesetDefJson")
                        }
                    }
                    layerConstructor.addParameter("json", LayerInstance::class)
                    addSuperclassConstructorParameter("%N", "json")
                }
            }

            when (layerDef.type) {
                "IntGrid" -> {
                    if (layerDef.autoTilesetDefUid == null) {
                        // IntGrid
                        extendLayerClass(baseLayerIntGridClass().kotlin)
                        instantiateLayerFun
                            .addStatement("val intGridValues = project.getLayerDef(json.layerDefUid)!!.intGridValues")
                            .addStatement("val layer = %L.%L.$layerName(intGridValues, json)", pkg, className)


                    } else {
                        // Auto-layer IntGrid
                        extendLayerClass(baseLayerIntGridAutoLayerClass().kotlin)
                        val tileset = tilesets[layerDef.autoTilesetDefUid]
                        if (tileset != null) {
                            val tilesetType = ClassName.bestGuess(tileset.typeName).copy(nullable = true)
                            layerClassSpec.addProperty(
                                PropertySpec.builder("tileset", tilesetType)
                                    .initializer("%L(%N!!)", tileset.typeName, "tilesetDefJson")
                                    .build()
                            )
                            layerClassSpec.addFunction(
                                FunSpec.builder("getTileset").returns(baseTilesetClass().asTypeName().copy(true))
                                    .addModifiers(KModifier.OVERRIDE)
                                    .addStatement("return tileset").build()
                            )
                        }
                        instantiateLayerFun
                            .addStatement("val intGridValues = project.getLayerDef(json.layerDefUid)!!.intGridValues")
                            .addStatement("val tilesetDef = project.getTilesetDef(json.tilesetDefUid)!!")
                            .addStatement(
                                "val layer = %L.%L.$layerName(tilesetDef, intGridValues, json)",
                                pkg,
                                className
                            )
                    }
                }
                "AutoLayer" -> {
                    extendLayerClass(baseLayerAutoLayerClass().kotlin)
                    val tileset = tilesets[layerDef.autoTilesetDefUid]
                    if (tileset != null) {
                        val tilesetType = ClassName.bestGuess(tileset.typeName).copy(nullable = true)
                        layerClassSpec.addProperty(
                            PropertySpec.builder("tileset", tilesetType)
                                .initializer("%L(%N!!)", tileset.typeName, "tilesetDefJson")
                                .build()
                        )
                        layerClassSpec.addFunction(
                            FunSpec.builder("getTileset").returns(baseTilesetClass().asTypeName().copy(true))
                                .addModifiers(KModifier.OVERRIDE)
                                .addStatement("return tileset").build()
                        )
                    }

                    instantiateLayerFun
                        .addStatement("val tilesetDef = project.getTilesetDef(json.tilesetDefUid)!!")
                        .addStatement("val layer = %L.%L.$layerName(tilesetDef, json)", pkg, className)
                }
                "Entities" -> {
                    extendLayerClass(LayerEntities::class)
                    layerClassSpec.addFunction(instantiateEntityFun.build())


                    entities.forEach {
                        val levelClassType = ClassName.bestGuess("$ENTITY_PREFIX${it.identifier}")
                        // all levels list property
                        layerClassSpec.addProperty(
                            PropertySpec.builder(
                                "_all${it.identifier.capitalize()}",
                                ClassName("kotlin.collections", "MutableList").parameterizedBy(levelClassType)
                            ).initializer(
                                CodeBlock.builder()
                                    .addStatement("mutableListOf()")
                                    .build()
                            ).addModifiers(KModifier.PRIVATE).build()
                        )

                        layerClassSpec.addProperty(
                            PropertySpec.builder(
                                "all${it.identifier.capitalize()}",
                                List::class.asTypeName().parameterizedBy(levelClassType)
                            ).getter(
                                FunSpec.getterBuilder()
                                    .addStatement("return _all${it.identifier.capitalize()}.toList()")
                                    .build()
                            ).build()
                        )

                    }
                    layerClassSpec.addInitializerBlock(
                        CodeBlock.builder().addStatement("instantiateEntities()").build()
                    )

                    instantiateLayerFun
                        .addStatement("val layer = %L.%L.$layerName(json)", pkg, className)
                }
                "Tiles" -> {
                    extendLayerClass(baseLayerTilesClass().kotlin)

                    val tileset = tilesets[layerDef.tilesetDefUid]
                        ?: error("Tiles layer ${layerDef.identifier} doesn't have a tileset!")
                    val tilesetType = ClassName.bestGuess(tileset.typeName)
                    layerClassSpec.addProperty(
                        PropertySpec.builder("tileset", tilesetType)
                            .initializer("%L(%N)", tileset.typeName, "tilesetDefJson")
                            .build()
                    )
                    layerClassSpec.addFunction(
                        FunSpec.builder("getTileset").returns(baseTilesetClass().asTypeName().copy(true))
                            .addModifiers(KModifier.OVERRIDE)
                            .addStatement("return tileset").build()
                    )

                    instantiateLayerFun
                        .addStatement("val tilesetDef = project.getTilesetDef(json.tilesetDefUid)!!")
                        .addStatement("val layer = %L.%L.$layerName(tilesetDef, json)", pkg, className)
                }
                else -> {
                    error("Unknown layer type ${layerDef.type}")
                }

            }

            layerClassSpec.primaryConstructor(layerConstructor.build())
            projectClassSpec.addType(layerClassSpec.build())

            instantiateLayerFun
                .addStatement("return layer")
                .endControlFlow()
        }

        instantiateLayerFun.addStatement("return null")

        return instantiateLayerFun
    }


    private fun generateLevel(
        projectClassSpec: TypeSpec.Builder,
        pkg: String,
        className: String,
        layers: List<LayerDefinition>,
        instantiateLayerFun: FunSpec.Builder
    ) {
        val levelClassSpec = TypeSpec.classBuilder("${className}$LEVEL_SUFFIX").apply {
            superclass(Level::class)
            addSuperclassConstructorParameter("%N", "project")
            addSuperclassConstructorParameter("%N", "json")
        }
        val levelConstructor =
            FunSpec.constructorBuilder()
                .addParameter("project", Project::class)
                .addParameter("json", LevelDefinition::class)

        levelClassSpec.primaryConstructor(levelConstructor.build())
            .addFunction(instantiateLayerFun.build())

        layers.forEach {
            levelClassSpec.addProperty(
                PropertySpec.builder(
                    "layer${it.identifier.capitalize()}",
                    ClassName.bestGuess("$LAYER_PREFIX${it.identifier}")
                ).getter(
                    FunSpec.getterBuilder()
                        .addStatement("return resolveLayer(%S) as $LAYER_PREFIX%N", it.identifier, it.identifier)
                        .build()
                ).build()
            )
        }
        projectClassSpec.addType(levelClassSpec.build())
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        const val FILE_INDENT = "    "
    }
}