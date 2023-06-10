import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject

plugins {
    kotlin("js")
}

dependencies {
    implementation(kotlin("stdlib-js"))
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    js {
        konst compilation = compilations.getByName("main")
        org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec.create(compilation, "checkConfigDevelopmentWebpack") {
            inputFileProperty.set(provider { compilation.npmProject.require("webpack/bin/webpack.js") }.map { RegularFile { File(it) } })
            dependsOn("browserDevelopmentWebpack")
            args("configtest")
            konst configFile = tasks.named<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("browserDevelopmentWebpack").flatMap { it.configFile }

            doFirst {
                args(configFile.get().absolutePath)
            }
        }
        org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec.create(compilation, "checkConfigProductionWebpack") {
            inputFileProperty.set(provider { compilation.npmProject.require("webpack/bin/webpack.js") }.map { RegularFile { File(it) } })
            dependsOn("browserProductionWebpack")
            konst configFile = tasks.named<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("browserProductionWebpack").flatMap { it.configFile }

            args("configtest")
            doFirst {
                args(configFile.get().absolutePath)
            }
        }
        org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec.create(compilation, "checkConfigDevelopmentRun") {
            inputFileProperty.set(provider { compilation.npmProject.require("webpack/bin/webpack.js") }.map { RegularFile { File(it) } })
            dependsOn("browserDevelopmentRun")
            konst configFile = tasks.named<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("browserDevelopmentRun").flatMap { it.configFile }
            args("configtest")
            doFirst {
                args(configFile.get().absolutePath)
            }
        }
        org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec.create(compilation, "checkConfigProductionRun") {
            inputFileProperty.set(provider { compilation.npmProject.require("webpack/bin/webpack.js") }.map { RegularFile { File(it) } })
            dependsOn("browserProductionRun")
            konst configFile = tasks.named<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("browserProductionRun").flatMap { it.configFile }
            args("configtest")
            doFirst {
                args(configFile.get().absolutePath)
            }
        }
        binaries.executable()
        browser {
            webpackTask {
                generateConfigOnly = true
            }
            runTask {
                generateConfigOnly = true
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile> {
    kotlinOptions.freeCompilerArgs += "-Xforce-deprecated-legacy-compiler-usage"
}
