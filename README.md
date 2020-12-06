
[![Release](https://jitpack.io/v/com.example/Repo.svg)]
(https://jitpack.io/#com.lehaine/gdx-ldtk-api)

# About
**!! This project is currently in development and should not be used yet !!**

This is a Kotlin and Java API to parse and load **LDtk project** files. 

This library can be used for any JVM game engine/framework. It features a separate LibGDX module for easy rendering.

[LDtk official website](https://ldtk.io/)

## Features
- **Annotation processing**: Adding a simple annotation will allow fully generated typesafe code to be used within your game.
- **Compile time code gen**: When using the annotation processor, the project code will be generated at compile time and available right away.
- **LibGDX Module**: Are you using LibGDX as a game framework? Use this module to easily render the loaded LDtk files.
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

    // get a level
    val level: World.WorldLevel = world.allLevels[0]

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

        // get a level
        JavaWorld.JavaWorldLevel level = world.getAllLevels().get(0);

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

You can check out a few samples in the [samples](samples) module.

## Documentation

TODO

## Download

In your gradle build script ensure you have the `kotlin` and `kapt` gradle plugins enabled.

If you are using **Kotlin** then they may already be enabled.

If you are using **Java** then they need to be enabled. **Warning**: This will most likely force you to change any `annontationPrcessing` dependencies to `kapt`.


```Kotlin
plugins {
    kotlin("jvm") version "1.4.20"
    kotlin("kapt") version "1.4.20"
}
```

Add the libraries to your dependencies. 
**The `ldtk-api` and `ldtk-processor` libraries are not yet published to any repositories for download.**

```Kotlin
dependencies {
    implementation("ldtk-api")
    kapt("ldtk-processor")
}
```

## TODO

- [ ] Add LibGDX module
- [ ] Major code clean up
- [ ] Add documentation
