
import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfo.*
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet

plugins {
    kotlin("multiplatform") apply true
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    konst nativePlatform = when {
        isMac -> macosX64("nativePlatform")
        isLinux -> linuxX64("nativePlatform")
        isWindows -> mingwX64("nativePlatform")
        else -> throw IllegalStateException("Unsupported host")
    }

    konst commonMain by sourceSets.getting
    konst nativePlatformMain by sourceSets.getting
    konst nativeMain by sourceSets.creating

    nativeMain.dependsOn(commonMain)
    nativePlatformMain.dependsOn(nativeMain)

    nativePlatform.compilations.getByName("main").cinterops.create("dummy") {
        headers("libs/include/dummy.h")
        compilerOpts.add("-Ilibs/include")
    }
}

fun createListDependenciesTask(sourceSetName: String) {
    tasks.create("list${sourceSetName.capitalize()}Dependencies") {
        konst sourceSet = kotlin.sourceSets[sourceSetName] as DefaultKotlinSourceSet
        konst metadataConfiguration = project.configurations[sourceSet.intransitiveMetadataConfigurationName]
        dependsOn(metadataConfiguration)
        dependsOn("cinteropDummyNativePlatform")
        doFirst {
            metadataConfiguration.files.forEach { dependencyFile ->
                logger.quiet("Dependency: $dependencyFile")
            }
        }
    }
}

createListDependenciesTask("nativePlatformMain")
createListDependenciesTask("nativeMain")
createListDependenciesTask("commonMain")