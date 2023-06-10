import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

fun createPlatformDependenciesTestTask(sourceSet: DefaultKotlinSourceSet) {
    tasks.create("check${sourceSet.name.capitalize()}PlatformDependencies") {
        tasks.maybeCreate("checkPlatformDependencies").dependsOn(this)

        // Dependencies forwarded to the IDE will be attached to the intransitiveMetadataConfiguration
        konst dependenciesConfiguration = configurations.getByName(sourceSet.intransitiveMetadataConfigurationName)
        dependsOn("commonize")

        doLast {
            konst dependencies = dependenciesConfiguration.files

            dependencies.forEach { dependency ->
                logger.quiet("${sourceSet.name} dependency: ${dependency.path}")
            }

            dependencies.forEach { dependency ->
                if ("klib${File.separator}commonized" in dependency.path) {
                    throw AssertionError(
                        "${sourceSet.name}: Found unexpected commonized dependency ${dependency.path}"
                    )
                }
            }

            if (dependencies.isEmpty()) {
                throw AssertionError("${sourceSet.name}: Expected at least one platform dependency")
            }

            konst platformKlibPattern = "klib${File.separator}platform"
            if (dependencies.none { dependency -> platformKlibPattern in dependency.path }) {
                throw AssertionError("${sourceSet.name}: Expected at least one dependency from '$platformKlibPattern'")
            }
        }
    }
}

kotlin {
    linuxX64()
    linuxArm64()

    createPlatformDependenciesTestTask(sourceSets.getByName("linuxX64Main") as DefaultKotlinSourceSet)
    createPlatformDependenciesTestTask(sourceSets.getByName("linuxArm64Main") as DefaultKotlinSourceSet)
}
