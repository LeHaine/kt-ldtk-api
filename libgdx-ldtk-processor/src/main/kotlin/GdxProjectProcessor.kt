import com.google.auto.service.AutoService
import com.lehaine.gdx.*
import com.lehaine.ldtk.*
import com.lehaine.ldtk.processor.ProjectProcessor
import javax.annotation.processing.Processor

@AutoService(Processor::class)
class GdxProjectProcessor : ProjectProcessor() {

    override fun baseProjectClass(): Class<out Project> {
        return GdxProject::class.java
    }

    override fun baseLevelClass(): Class<out Level> {
        return GdxLevel::class.java
    }

    override fun baseLayerAutoLayerClass(): Class<out LayerAutoLayer> {
        return GdxLayerAutoLayer::class.java
    }

    override fun baseLayerIntGridClass(): Class<out LayerIntGrid> {
        return GdxLayerIntGrid::class.java
    }

    override fun baseLayerIntGridAutoLayerClass(): Class<out LayerIntGridAutoLayer> {
        return GdxLayerIntGridAutoLayer::class.java
    }

    override fun baseLayerTilesClass(): Class<out LayerTiles> {
        return GdxLayerTiles::class.java
    }

    override fun baseTilesetClass(): Class<out Tileset> {
        return GdxTileset::class.java
    }
}