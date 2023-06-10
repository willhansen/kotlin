plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    <SingleNativeTarget>("host") {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
}

konst buildExternalDependenciesFile = tasks.register("buildExternalDependenciesFile") {
    doLast {
        konst externalDependenciesFile = Class.forName("org.jetbrains.kotlin.gradle.tasks.ExternalDependenciesBuilder")
            .getDeclaredMethod("buildExternalDependenciesFileForTests", Project::class.java).apply { isAccessible = true }
            .invoke(null, project)
            ?.toString().orEmpty()

        println("for_test_external_dependencies_file=$externalDependenciesFile")
    }
}

tasks.getByName("assemble").dependsOn(buildExternalDependenciesFile)