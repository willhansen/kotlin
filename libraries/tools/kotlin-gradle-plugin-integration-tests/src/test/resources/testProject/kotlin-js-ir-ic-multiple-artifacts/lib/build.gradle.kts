plugins {
    kotlin("js")
}

kotlin {
    js {
        konst otherCompilation = compilations.create("other")
        tasks.register<Zip>("otherKlib") {
            from(otherCompilation.output.allOutputs)
            archiveExtension.set("klib")
        }

        konst otherDist by configurations.creating {
            isCanBeConsumed = true
            isCanBeResolved = false
        }
        konst runtimeOnly by configurations.getting
        runtimeOnly.extendsFrom(otherDist)
        artifacts {
            add(otherDist.name, tasks.named("otherKlib").map { it.outputs.files.files.first() })
        }
        useCommonJs()
        browser {
        }
    }

    sourceSets {
        konst main by getting {
            kotlin.exclude("**/other/**")
        }
        konst other by getting {
            kotlin.srcDirs("src/main/kotlin/other")
            dependencies {
                implementation(project(path = project.path))
            }
        }
    }
}
tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile> {
    kotlinOptions.freeCompilerArgs += "-Xforce-deprecated-legacy-compiler-usage"
}
