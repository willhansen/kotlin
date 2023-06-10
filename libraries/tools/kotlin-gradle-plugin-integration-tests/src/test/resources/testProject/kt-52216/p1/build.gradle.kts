plugins {
    kotlin("multiplatform")
}

group = "me.user"
version = "1.0"

repositories {
    maven("$rootDir/../repo")
    mavenLocal()
    mavenCentral()
}

kotlin {
    konst targets = listOf(
        jvm(),
        js(),
        linuxX64()
    )

    sourceSets {
        konst commonMain by getting

        for (target in targets) {
            getByName(target.leafSourceSetName) {
                dependsOn(commonMain)
                dependencies {
                    implementation("kt52216:lib:1.0")
                }
            }
        }
    }
}

konst org.jetbrains.kotlin.gradle.plugin.KotlinTarget.leafSourceSetName: String
    get() = "${name}Main"
