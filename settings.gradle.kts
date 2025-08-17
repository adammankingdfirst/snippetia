rootProject.name = "snippetia"

include(":backend")
include(":frontend")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}