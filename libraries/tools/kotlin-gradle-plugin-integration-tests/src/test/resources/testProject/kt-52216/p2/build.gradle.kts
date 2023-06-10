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
        konst commonMain by getting {
            dependencies {
                implementation("kt52216:lib:1.0")
            }
        }

        for (target in targets) {
            konst leafSourceSet = getByName(target.leafSourceSetName)
            create(target.intermediateSourceSetName) {
                leafSourceSet.dependsOn(this)
                dependsOn(commonMain)
            }
        }
    }
}

konst org.jetbrains.kotlin.gradle.plugin.KotlinTarget.leafSourceSetName: String
    get() = "${name}Main"

konst org.jetbrains.kotlin.gradle.plugin.KotlinTarget.intermediateSourceSetName: String
    get() = "${name}Intermediate"
