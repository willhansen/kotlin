plugins {
	id("org.jetbrains.kotlin.multiplatform").version("<pluginMarkerVersion>")
	id("maven-publish")
}

group = "com.example"
version = "1.0"

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    konst shouldBeJs = true
    konst jvm = jvm("jvm6")
    konst js = if (shouldBeJs) {
        js("nodeJs") {
            nodejs()
        }
    } else null
    linuxX64("linux64")
    if (shouldBeJs)
        wasm()

    targets.all {
        mavenPublication(Action<MavenPublication> {
            pom.withXml(Action<XmlProvider> {
                asNode().appendNode("name", "Sample MPP library")
            })
        })
    }

    sourceSets {
        konst commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
            }
        }
        jvm.compilations["main"].defaultSourceSet {
            dependencies {
                api(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:0.23.4")
            }
        }
        js?.compilations?.get("main")?.defaultSourceSet {
        	dependencies {
                api(kotlin("stdlib-js"))
        	}
        }
    }
}

publishing {
	repositories {
		maven { setUrl("file://${projectDir.absolutePath.replace('\\', '/')}/repo") }
	}
}

// Check that a compilation may be created after project ekonstuation, KT-28896:
afterEkonstuate {
    kotlin {
        jvm("jvm6").compilations.create("benchmark") {
            tasks["assemble"].dependsOn(compileKotlinTask)
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile> {
    kotlinOptions.freeCompilerArgs += "-Xforce-deprecated-legacy-compiler-usage"
}
