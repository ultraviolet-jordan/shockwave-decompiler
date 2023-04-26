rootProject.name = "shockwave-decompiler"

dependencyResolutionManagement {
    repositories(RepositoryHandler::mavenCentral)

    versionCatalogs {
        create("deps") {
            // Dependency versions.
            version("kotlin", "1.8.20")
            version("versions", "0.46.0")

            // Dependency plugins
            plugin("jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
            plugin("versions", "com.github.ben-manes.versions").versionRef("versions")
        }
    }
}

include("decompiler")
include("decompiler")
