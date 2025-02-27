import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform").version("<pluginMarkerVersion>")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    konst nativeMain by sourceSets.creating {
        dependsOn(sourceSets["commonMain"])
    }

    targets.withType(KotlinNativeTarget::class.java).all {
        compilations["main"].defaultSourceSet.dependsOn(nativeMain)
    }

    // Targets are added by test methods.
}