import com.google.auto.service.AutoService
import com.lehaine.gdx.GdxLayerAutoLayer
import com.lehaine.gdx.GdxLayerIntGrid
import com.lehaine.gdx.GdxLayerIntGridAutoLayer
import com.lehaine.gdx.GdxLayerTiles
import com.lehaine.ldtk.LayerAutoLayer
import com.lehaine.ldtk.LayerIntGrid
import com.lehaine.ldtk.LayerIntGridAutoLayer
import com.lehaine.ldtk.LayerTiles
import com.lehaine.ldtk.processor.ProjectProcessor
import javax.annotation.processing.Processor


@AutoService(Processor::class)
class GdxProjectProcessor : ProjectProcessor() {

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
}