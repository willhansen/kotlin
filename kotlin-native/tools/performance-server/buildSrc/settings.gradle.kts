pluginManagement {
    apply(from = "../../../../repo/scripts/cache-redirector.settings.gradle.kts")
    apply(from = "../../../../repo/scripts/kotlin-bootstrap.settings.gradle.kts")

    repositories {
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-dependencies")
        mavenCentral()
        gradlePluginPortal()
    }
}

buildscript {
    repositories {
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-dependencies")
        mavenCentral()
    }
    konst buildGradlePluginVersion = extra["kotlin.build.gradlePlugin.version"]
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-build-gradle-plugin:$buildGradlePluginVersion")
    }
}
