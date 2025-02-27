pluginManagement {
    konst dokkaVersion = providers.gradleProperty("dokka_version").get()
    konst dokkaRepository = providers.gradleProperty("dokka_repository").getOrElse("https://maven.pkg.jetbrains.space/kotlin/p/dokka/dev")
    plugins {
        id("org.jetbrains.dokka") version(dokkaVersion)
    }

    repositories {
        gradlePluginPortal()
        maven(url = dokkaRepository)
        if ("-local" in dokkaVersion) {
            mavenLocal()
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    konst dokkaVersion = providers.gradleProperty("dokka_version").get()
    konst dokkaRepository = providers.gradleProperty("dokka_repository").getOrElse("https://maven.pkg.jetbrains.space/kotlin/p/dokka/dev")
    repositories {
        mavenCentral()
        maven(url = dokkaRepository)
        if ("-local" in dokkaVersion) {
            mavenLocal()
        }
    }
}

rootProject.name = "kotlin-stdlib-docs"

include("kotlin_big")
include("plugins")
include("plugins:dokka-samples-transformer-plugin")
include("plugins:dokka-stdlib-configuration-plugin")
include("plugins:dokka-version-filter-plugin")
