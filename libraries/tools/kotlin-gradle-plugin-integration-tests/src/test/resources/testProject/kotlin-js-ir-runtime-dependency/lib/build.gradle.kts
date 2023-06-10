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

        useCommonJs()
        browser {
        }
    }

    sourceSets {
        konst main by getting {
            kotlin.exclude("**/other/**")
            dependencies {
                runtimeOnly(files(tasks.named("otherKlib")))
            }
        }
        konst other by getting {
            kotlin.srcDirs("src/main/kotlin/other")
            dependencies {
                implementation(project(path = project.path))
            }
        }
    }
}