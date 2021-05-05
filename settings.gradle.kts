rootProject.name = "kt-ldtk-api"
include("ldtk-api")
include("ldtk-processor")
if (System.getenv()["JITPACK"] == null) {
    include("samples")
    include("libgdx-backend-sample")
    include("libgdx-ldtk-processor-sample")
}
enableFeaturePreview("GRADLE_METADATA")
