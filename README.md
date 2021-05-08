
[![Release](https://jitpack.io/v/com.lehaine/kt-ldtk-api.svg)](https://jitpack.io/#com.lehaine/kt-ldtk-api)
[![Version](https://img.shields.io/github/v/tag/lehaine/kt-ldtk-api?label=version)](https://github.com/LeHaine/kt-ldtk-api/releases)
[![LDtk](https://img.shields.io/github/v/release/deepnight/ldtk?color=red&label=LDtk)](https://ldtk.io/)

# About
This is a Kotlin and Java API to parse and load **LDtk project** files. 
The `ldtk-api` module is built for Kotlin Multiplatform which means it works for JVM and JS targets. Other targets have not been tested yet.

This library can be used for any Kotlin Multiplatform or JVM game engine/framework/project. It features a sample LibGDX project for both annotation processing and rendering as well as a KorGE example.

**LibGDX Sample Example**
![LibGDX Sample Example](/screenshots/example_3.gif "LibGDX rendering example")

[LDtk official website](https://ldtk.io/)

**Helpful links:**

- [KorGE Kotlin Multiplatform Example](https://github.com/LeHaine/korge-ldtk-example) - **May be outdated but should give a good idea on how to use it**
- [KorGE LDtk View](https://github.com/LeHaine/kiwi/tree/master/src/commonMain/kotlin/com/lehaine/kiwi/korge/view/ldtk) - A view implemented for rendering LDtk levels in KorGE used in my personal library for KorGE called [Kiwi](https://github.com/LeHaine/kiwi).

## Features
- **Annotation processing**: An **optional** annotation that will allow fully generated typesafe code to be used within your game.
- **Compile time code gen**: When using the annotation processor, the project code will be generated at compile time and available right away.
- **No runtime reflection**: Reflection is used at compile time which is used to generate code.
- **Extremely simple**: Parsing and loading a file is extremely easy in just a few lines of code


# Usage
## Sample code 
Create any class and add an `@LDtkProject` annotation to it with the location to your LDtk file on your projects classpath.

Build your project, and it is ready to be used.

**Kotlin example**
```Kotlin
// designate class for loading and attaching LDtk file to
@LDtkProject(ldtkFileLocation = "sample.ldtk", name = "World")
class _World

fun main(args: Array<String>) {
    // create new LDtk world
    val world = World()
    world.load() // loads the file and parses it

    // get a level
    val level: World.WorldLevel = world.allLevels[0]
    level.load() // force load a level
    // levels are loaded automatically when accessing any layer of that level

    // iterate over a layers tiles
    level.layerBackground.autoTiles.forEach {
        // logic for handling the tile
    }

    level.layerEntities.allMob.forEach { mob ->
        // access entity fields
        val type: World.MobType = mob.type // generated enum class
        val patrolPoint: Point? = mob.patrol // points
        val health: Int = mob.health
    }

    level.layerEntities.allCart.forEach { cart ->
        // field arrays
        cart.items.forEach { item ->
            if (item == World.Items.Pickaxe) {
                // spawn pickaxe
            }
        }
    }
}
```

**Java Example**
```Java
// designate class for loading and attaching LDtk file to
@LDtkProject(ldtkFileLocation = "sample.ldtk", name = "JavaWorld")
public class SampleJava {
    public static void main(String[] args) {
        // create new LDtk world
        JavaWorld world = new JavaWorld();
        world.load(); // loads the file and parses it

        // get a level
        JavaWorld.JavaWorldLevel level = world.getAllLevels().get(0);
        level.load(); // force load a level
        // levels are loaded automatically when accessing any layer of that level

        // iterate over a layers tiles
        for (LayerAutoLayer.AutoTile tile : level.getLayerBackground().getAutoTiles()) {
            // logic for handling the tile
            int x = tile.getRenderX();
        }

        // iterate over entities
        for (JavaWorld.EntityMob mob : level.getLayerEntities().getAllMob()) {
            JavaWorld.MobType type = mob.type;
            Point patrolPoint = mob.getPatrol();
            int health = mob.getHealth();
        }

        for (JavaWorld.EntityCart cart : level.getLayerEntities().getAllCart()) {
            // field arrays / lists
            List<JavaWorld.Items> items = cart.getItems();

            for (JavaWorld.Items item : items) {
                if (item == JavaWorld.Items.Pickaxe) {
                    // spawn pickaxe
                }
            }
        }
    }
}
```

## Sample code when NOT using the annotation to generate code

```Kotlin 
val proj = Project("sample.ldtk").apply { load() }
val level = proj.allUntypedLevels[0]
level.load() // this is only needed if levels are saved in separate files!
val gridSize = 16
level.allUntypedEntities?.forEach { entity ->
    val x = entity.cx * gridSize
    val y = entity.cy * gridSize
    entity.json.fieldInstances.forEach {
        if (it.identifier == "Color") {
            val color = it.value!!.content
        }

    }
}
level.allUntypedLayers.forEach { layer ->
    if (layer.type == LayerType.IntGrid) {
        val intGridLayer = layer as LayerIntGrid
        intGridLayer.getInt(0, 5)
    }
}
```

You can check out a few samples in the [samples](samples) module.

## Download

In your gradle build script ensure you have the `kotlin` and `kapt` gradle plugins enabled.

If you are using **Kotlin** then they may already be enabled.

If you are using **Java** then they need to be enabled. **Warning**: This will most likely force you to change any `annontationPrcessing` dependencies to `kapt`.


```Kotlin
plugins {
    kotlin("jvm") version "1.4.31"
    kotlin("kapt") version "1.4.31"
}
```

### Dependencies for any framework
**Note:** Check for latest version at the top.

**build.gradle.kts**
```Kotlin
allprojects {
    repositories {
        maven(url="https://jitpack.io")
    }
}
```

```Kotlin
configurations.all { // kapt has an issue with determining the correct KMM library, so we need to help it
    if (name.contains("kapt")) {
        attributes.attribute(
            KotlinPlatformType.attribute,
            KotlinPlatformType.jvm // pass in the JVM 
        )
    }
}

dependencies {
    implementation("com.lehaine.kt-ldtk-api:ldtk-api:$version")
    kapt("com.lehaine.kt-ldtk-api:ldtk-processor:$version")
}
```
