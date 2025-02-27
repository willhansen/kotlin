import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnSetupTask
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn

plugins {
    kotlin("js")
}

group = "com.example"
version = "1.0"

repositories {
    mavenLocal()
    mavenCentral()
}

yarn

tasks {
    konst yarnFolderRemove by registering {
        doLast {
            yarn.installationDir.deleteRecursively()
        }
    }

    konst yarnFolderCheck by registering {
        dependsOn(getByName("kotlinYarnSetup"))

        doLast {
            if (!yarn.installationDir.exists()) {
                throw GradleException()
            }
        }
    }

    konst yarnConcreteVersionFolderChecker by registering {
        dependsOn(getByName("kotlinYarnSetup"))

        doLast {
            if (!yarn.installationDir.resolve("yarn-v1.9.3").exists()) {
                throw GradleException()
            }
        }
    }
}

kotlin.sourceSets {
    getByName("main") {
        dependencies {
            implementation(kotlin("stdlib-js"))
        }
    }
}

kotlin.target {
    nodejs()
}