package com.lehaine.ldtk.processor

import com.google.auto.service.AutoService
import com.lehaine.ldtk.*
import com.lehaine.ldtk.LDtkApi.ENTITY_PREFIX
import com.lehaine.ldtk.LDtkApi.LAYER_PREFIX
import com.lehaine.ldtk.LDtkApi.LEVEL_SUFFIX
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation
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
                val pkg = processingEnv.elementUtils.getPackageOf(it).toString()
                generateProject(className, pkg, ldtkFileLocation)
            }
        return true
    }

    private fun generateProject(className: String, pkg: String, ldtkFileLocation: String) {
        try {
            val resource =
                processingEnv.filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "tmp_${className}", null)
            val resourcePath = findResourcePath(Paths.get(resource.toUri()), ldtkFileLocation)
            resource.delete()
            val fileContent = resourcePath.toFile().readText()

            val json = LDtkApi.parseLDtkFile(fileContent)

            require(json != null) { "LDtk project file is empty or or missing! Please check to ensure it exists." }

            val fileSpec = FileSpec.builder(pkg, className).indent(FILE_INDENT)
            val projectClassSpec = TypeSpec.classBuilder(className).apply {
                superclass(Project::class)
                addSuperclassConstructorParameter("%S", ldtkFileLocation)
                addFunction(
                    FunSpec.builder("instantiateLevel")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("project", Project::class)
                        .addParameter("json", LevelJson::class)
                        .returns(Level::class)
                        .addStatement("return %L$LEVEL_SUFFIX(project, json)", className)
                        .build()
                )
            }

            generateEnums(projectClassSpec, json.defs.enums)
            val instantiateLayerFun = generateEntities(projectClassSpec, pkg, className, json.defs.entities)
            val tilesets = generateTilesets(projectClassSpec, json.defs.tilesets)
            generateLayers(
                projectClassSpec,
                pkg,
                className,
                tilesets,
                json.defs.entities,
                json.defs.layers,
                instantiateLayerFun
            )
            generateLevel(projectClassSpec, pkg, className, json.defs.layers)

            val levelClassType = ClassName.bestGuess("${className}$LEVEL_SUFFIX")
            // all levels list property
            projectClassSpec.addProperty(
                PropertySpec.builder(
                    "allLevels",
                    List::class.asTypeName().parameterizedBy(levelClassType)
                ).initializer(
                    CodeBlock.builder()
                        .beginControlFlow("allUntypedLevels.map")
                        .addStatement("it as %T", levelClassType)
                        .endControlFlow()
                        .build()
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

    private fun findResourcePath(path: Path, ldtkFileLocation: String, maxPath: Int = 15, currentPath: Int = 0): Path {
        val newPath = path.resolve("./resources/main/${ldtkFileLocation}")
        if (!Files.exists(newPath)) {
            newPath.resolve("./$ldtkFileLocation")
        }
        return if (Files.exists(newPath)) {
            newPath
        } else {
            if (currentPath >= maxPath) {
                error("Unable to find resource file $ldtkFileLocation. Aborting...")
            }
            findResourcePath(path.resolve("../"), ldtkFileLocation, currentPath = currentPath + 1)
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

    private fun generateEntities(
        projectClassSpec: TypeSpec.Builder, pkg: String, projClassName: String,
        entities: List<EntityDefJson>
    ): FunSpec.Builder {
        val instantiateLayerFun = FunSpec.builder("instantiateEntity")
            .addParameter("json", EntityInstanceJson::class)
            .addModifiers(KModifier.OVERRIDE)
            .addModifiers(KModifier.PROTECTED)
            .returns(Entity::class.asTypeName().copy(nullable = true))

        entities.forEach { entityDef ->
            val entityClassName = "$ENTITY_PREFIX${entityDef.identifier}"
            val entityClassSpec = TypeSpec.classBuilder(entityClassName).apply {
                superclass(Entity::class.java)
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

            val fields = mutableListOf<String>()
            val arrayReg = "Array<(.*)>".toRegex()
            instantiateLayerFun
                .beginControlFlow("if (\"${entityDef.identifier}\" == json.__identifier)")
                .addStatement("val entity = %L.%L.${entityClassName}(json)", pkg, projClassName)
                .beginControlFlow("json.fieldInstances.forEach")
            entityDef.fieldDefs.forEach { fieldDefJson ->
                instantiateLayerFun.beginControlFlow("if(\"${fieldDefJson.identifier}\" == it.__identifier)")
                val canBeNull = fieldDefJson.canBeNull
                val isArray = arrayReg.matches(fieldDefJson.__type)
                val typeName =
                    if (isArray) arrayReg.find(fieldDefJson.__type)!!.groupValues[1] else fieldDefJson.__type
                val name = if (typeName == "Bool") "Boolean" else if (typeName == "Float") "Double" else typeName
                val entityFieldName = "entity.${fieldDefJson.identifier}"
                val privateEntityFieldName = "entity._${fieldDefJson.identifier}"
                if (isArray) {
                    val fieldClassType = when (name) {
                        "Int", "Double", "Boolean", "String" -> {
                            if (name == "Int") {
                                instantiateLayerFun.addStatement("val arrList = it.__value as MutableList<Double>")
                                instantiateLayerFun.addStatement("$privateEntityFieldName.addAll(arrList.map { it.toInt() })")
                            } else {
                                instantiateLayerFun.addStatement("val arrList = it.__value as MutableList<$name>")
                                instantiateLayerFun.addStatement("$privateEntityFieldName.addAll(arrList)")
                            }
                            ClassName("kotlin", name)
                        }
                        "Point", "Color" -> {
                            if (name == "Color") {
                                instantiateLayerFun.addStatement("val arrList = it.__value as MutableList<String>")
                                    .beginControlFlow("val result = arrList.map")
                                    .addStatement("val value = Project.hexToInt(it)")
                                    .addStatement("Color(value, it)")
                                    .endControlFlow()
                                    .addStatement("$privateEntityFieldName.addAll(result)")
                            } else {
                                instantiateLayerFun.addStatement("val arrList = it.__value as MutableList<Map<String, Double>>")
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
                                    instantiateLayerFun.addStatement("val arrList = it.__value as MutableList<String>")
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
                                    error("Unsupported Array type ${fieldDefJson.__type}")
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

                            instantiateLayerFun.addStatement("$entityFieldName = it.__value as $name")
                            val className = ClassName("kotlin", name).copy(canBeNull)
                            val defaultOverride = fieldDefJson.defaultOverride?.params?.get(0)
                            val hasOverride = defaultOverride != null
                            val defaultValue = when {
                                hasOverride -> {
                                    when (name) {
                                        "Int" -> (defaultOverride as? Double)?.toInt()
                                        "Double", "Boolean", "String" -> defaultOverride
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
                            val defaultValue = (fieldDefJson.defaultOverride?.params?.get(0) as? Double)?.toInt()
                            val intDefaultValue = defaultValue ?: 0
                            val hexDefaultValue = defaultValue?.let { "\"${Project.intToHex(it)}\"" }
                                ?: "\"#000000\""
                            val colorDefault = Color(intDefaultValue, hexDefaultValue)
                            val colorClass = ClassName("com.lehaine.ldtk", "Color").copy(nullable = canBeNull)

                            addToEntity(fieldDefJson.identifier, colorClass, colorDefault)
                            fields.add(fieldDefJson.identifier)

                            instantiateLayerFun.addStatement("val value = Project.hexToInt(it.__value as String)")
                                .addStatement("val hexValue = it.__value as String")
                                .addStatement("$entityFieldName = Color(value, hexValue)")

                        }
                        "Point" -> {
                            val className = ClassName("com.lehaine.ldtk", "Point").copy(canBeNull)
                            val defaultValue = fieldDefJson.defaultOverride?.params?.get(0) ?: "Point(0, 0)"
                            addToEntity(fieldDefJson.identifier, className, defaultValue)
                            fields.add(fieldDefJson.identifier)

                            instantiateLayerFun
                                .beginControlFlow("if (it.__value != null)")
                                .addStatement("val map = it.__value as Map<String, Double>")
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

                                    instantiateLayerFun.addStatement("$entityFieldName = $type.valueOf(it.__value as String)")
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

    private data class TilesetInfo(val typeName: String, val json: TilesetDefJson)

    private fun generateTilesets(
        projectClassSpec: TypeSpec.Builder,
        tilesets: List<TilesetDefJson>
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
        entities: List<EntityDefJson>,
        layers: List<LayerDefJson>,
        instantiateLayerFun: FunSpec.Builder
    ) {
        layers.forEach { layerDef ->
            val layerClassSpec = TypeSpec.classBuilder("$LAYER_PREFIX${layerDef.identifier}")
            val layerConstructor = FunSpec.constructorBuilder()

            fun extendLayerClass(superClass: KClass<*>) {
                layerClassSpec.run {
                    superclass(superClass)

                    when (superClass) {
                        baseLayerIntGridClass().kotlin, baseLayerIntGridAutoLayerClass().kotlin -> {
                            if (superClass == baseLayerIntGridAutoLayerClass().kotlin) {
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
                        baseLayerAutoLayerClass().kotlin -> {
                            layerConstructor.addParameter(
                                "tilesetDefJson",
                                TilesetDefJson::class.asTypeName().copy(nullable = true)
                            )
                            addSuperclassConstructorParameter("%N", "tilesetDefJson")
                        }
                        baseLayerTilesClass().kotlin -> {
                            layerConstructor.addParameter(
                                "tilesetDefJson",
                                TilesetDefJson::class.asTypeName()
                            )
                            addSuperclassConstructorParameter("%N", "tilesetDefJson")
                        }
                    }
                    layerConstructor.addParameter("json", LayerInstanceJson::class)
                    addSuperclassConstructorParameter("%N", "json")
                }
            }

            when (layerDef.type) {
                "IntGrid" -> {
                    if (layerDef.autoTilesetDefUid == null) {
                        // IntGrid
                        extendLayerClass(baseLayerIntGridClass().kotlin)

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
                }
                "Entities" -> {
                    extendLayerClass(LayerEntities::class)
                    layerClassSpec.addFunction(instantiateLayerFun.build())
                    layerClassSpec.addInitializerBlock(
                        CodeBlock.builder().addStatement("instantiateEntities()").build()
                    )


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
        val levelClassSpec = TypeSpec.classBuilder("${className}$LEVEL_SUFFIX").apply {
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