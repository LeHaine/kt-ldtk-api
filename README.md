
# About
**!! This project is currently in development and should not be used yet !!**

This is a Kotlin JVM API (Java API and annontation processing planned) to parse and load **LDtk project** files. 

This library can be used for any JVM game engine/framework. It features a separate LibGDX module for easy rendering.

[LDtk official website](https://ldtk.io/)

## Features
- **Annotation processing**: Adding a simple annontation will allow fully generated typesafe code to be used within your game.
- **Compile time code gen**: When using the annotation processor, the project code will be generated at compile time and available right away.
- **LibGDX Module**: Are you using LibGDX as a game framework? Use this module to easily render the loaded LDtk files.
- **Extremely simple**: Parsing and loading a file is extremely easy in just a few lines of code


# Usage
## Sample code
Create any class and add an `@LDtkProject` annotation to it with the location to your LDtk file on your projects classpath.

Build your project, and it is ready to be used.
```Kotlin
// designate class for loading and attaching LDtk file to
@LDtkProject(ldtkFileLocation = "sample.ldtk", name = "World")
class _World

fun main(args: Array<String>) {
    // create new LDtk world
    val world = World()

    // get a level
    val level = world.allLevels[0]

    // iterate over a layers tiles
    level.layer_Background.autoTiles.forEach {
        // logic for handling the tile
    }

    level.layer_Entities.all_Mob.forEach { mob ->
        // access entity fields
        val type: World.MobType = mob.type // generated enum class
        val patrolPoint: Point? = mob.patrol // points
        val health:Int = mob.health
        // access other fields, etc
    }

    level.layer_Entities.all_Cart.forEach { cart ->
        // field arrays
        cart.items.forEach { item ->
            if(item == World.Items.Pickaxe) {
                // spawn pickaxe
            }
        }
    }
}
```

You can check out a few samples in the [samples](samples) module.

## Documentation

TODO

## Download

TODO

## TODO

- [ ] Add LibGDX module
- [ ] Add Java friendly API
- [ ] Add module for generating Java code instead of Kotlin
- [ ] Major code clean up
- [ ] Add documentation
