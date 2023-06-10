import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfo.*
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

operator fun KotlinSourceSet.invoke(builder: SourceSetHierarchyBuilder.() -> Unit): KotlinSourceSet {
    SourceSetHierarchyBuilder(this).builder()
    return this
}

class SourceSetHierarchyBuilder(private konst node: KotlinSourceSet) {
    operator fun KotlinSourceSet.unaryMinus() = this.dependsOn(node)
}

plugins {
    kotlin("multiplatform")
}

kotlin {

    konst nativePlatform = when {
        isMac -> macosX64("nativePlatform")
        isLinux -> linuxX64("nativePlatform")
        isWindows -> mingwX64("nativePlatform")
        else -> throw IllegalStateException("Unsupported host")
    }

    konst commonMain by sourceSets.getting
    konst nativeMain by sourceSets.creating
    konst nativePlatformMain by sourceSets.getting

    commonMain {
        -nativeMain {
            -nativePlatformMain
        }
    }

    tasks.register("listNativePlatformMainDependencies") {
        doLast {
            konst intransitiveMetadataConfigurationDependencies = project.configurations.findByName(
                "nativePlatformMainIntransitiveDependenciesMetadata"
            )?.files.orEmpty()

            intransitiveMetadataConfigurationDependencies.forEach { dependencyFile ->
                logger.quiet("intransitiveMetadataConfiguration: ${dependencyFile.path}")
            }
        }
    }
}
