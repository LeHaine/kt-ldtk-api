
[![Release](https://jitpack.io/v/com.lehaine/kt-ldtk-api.svg)](https://jitpack.io/#com.lehaine/kt-ldtk-api)
[![Version](https://img.shields.io/github/v/tag/lehaine/kt-ldtk-api?label=version)](https://github.com/LeHaine/kt-ldtk-api/releases)
[![LDtk](https://img.shields.io/github/v/release/deepnight/ldtk?color=red&label=LDtk)](https://ldtk.io/)

# About
This is a Kotlin and Java API to parse and load **LDtk project** files. 
The `ldtk-api` module is built for Kotlin Multiplatform which mean it *should* work for JVM and JS targets.

This library can be used for any JVM game engine/framework. It features a separate LibGDX module for easy rendering.

![LibGDX Example](/screenshots/example_2.gif "LibGDX rendering example")

[LDtk official website](https://ldtk.io/)

## Features
- **Annotation processing**: An **optional** annotation that will allow fully generated typesafe code to be used within your game.
- **Compile time code gen**: When using the annotation processor, the project code will be generated at compile time and available right away.
- **No runtime reflection**: Reflection is used at compile time which is used to generate code.
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

## Sample code for LibGDX modules

**Kotlin Example**
```Kotlin
class GdxApp : ApplicationListener {

    private lateinit var spriteBatch: SpriteBatch
    private lateinit var tiles: Texture
    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: FitViewport
    private val world = World().apply { load() }
    private val testLevel = world.allLevels[0]

    override fun create() {
        spriteBatch = SpriteBatch()
        tiles = Texture(Gdx.files.internal("Cavernas_by_Adam_Saltsman.png"))
        camera = OrthographicCamera()
        viewport = PixelPerfectViewport(480f, 270f, camera)
        camera.translate(testLevel.pxWidth / 2f, testLevel.pxHeight / -2f)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, false)
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        spriteBatch.projectionMatrix = camera.combined
        spriteBatch.begin()
        testLevel.layerBackground.render(spriteBatch, tiles)
        testLevel.layerCollisions.render(spriteBatch, tiles)
        testLevel.layerCustom_tiles.render(spriteBatch, tiles)
        spriteBatch.end()
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun dispose() {
    }

}
```

**Java Example**

For a full Java example check out the repository [here](https://github.com/LeHaine/gdx-ldtk-api-java-sample).
```Java
@LDtkProject(ldtkFileLocation = "sample.ldtk", name = "World")
public class GdxTest implements ApplicationListener {

    private SpriteBatch spriteBatch;
    private Texture tiles;
    private Camera camera;
    private PixelPerfectViewport viewport;
    private final World world = new World();
    private final World.WorldLevel testLevel;

    @Override
    public void create() {
        world.load();
        testLevel = world.getAllLevels().get(0);
        spriteBatch = new SpriteBatch();
        tiles = new Texture(Gdx.files.internal("Cavernas_by_Adam_Saltsman.png"));
        camera = new OrthographicCamera();
        viewport = new PixelPerfectViewport(480, 270, camera);
        camera.translate(testLevel.getPxWidth() / 2f, testLevel.getPxHeight() / -2f, 0f);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        testLevel.getLayerBackground().render(spriteBatch, tiles);
        testLevel.getLayerCollisions().render(spriteBatch, tiles);
        testLevel.getLayerCustom_tiles().render(spriteBatch, tiles);
        spriteBatch.end();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(960, 540);
        new Lwjgl3Application(new GdxTest(), config);
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

## Documentation

TODO

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
        attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
    }
}

dependencies {
    implementation("com.lehaine.kt-ldtk-api:ldtk-api:$version")
    kapt("com.lehaine.kt-ldtk-api:ldtk-processor:$version")
}
```

### Dependencies for LibGDX modules
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
        attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
    }
}

dependencies {
    implementation("com.lehaine.kt-ldtk-api:ldtk-api:$version")
    implementation("com.lehaine.kt-ldtk-api:libgdx-backend:$version")
    kapt("com.lehaine.kt-ldtk-api:libgdx-ldtk-processor:$version")
}
```

## TODO

- [ ] Optimize rendering
- [ ] Major code clean up
- [ ] Add documentation
