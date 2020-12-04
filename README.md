
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
import com.lehaine.ldtk.LDtkProject

// designated class for the LDtk annotation
@LDtkProject(ldtkFileLocation = "sample.ldtk", name = "World")
class _World

fun main(args: Array<String>) {
    // create newly generated object
    val world = World()
    // access the fully typed data
    println(world.allLevels[0].layer_Entities.all_Mob[0].health)
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
