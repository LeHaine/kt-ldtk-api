rootProject.name = "gdx-ldtk-api"
include("ldtk-api")
if (System.getenv()["JITPACK"] == null)
    include("samples")
include("ldtk-processor")
include("libgdx-backend")
include("libgdx-ldtk-processor")
enableFeaturePreview("GRADLE_METADATA")
