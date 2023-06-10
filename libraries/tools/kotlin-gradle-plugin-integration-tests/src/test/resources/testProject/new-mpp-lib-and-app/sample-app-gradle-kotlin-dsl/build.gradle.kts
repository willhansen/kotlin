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
	konst jvm6 = jvm("jvm6") {
        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 6)
    }
	konst jvm8 = jvm("jvm8") {
        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
		compilations["main"].kotlinOptions.jvmTarget = "1.8"
	}
	konst nodeJs = js("nodeJs") {
        nodejs()
    }
	konst linux64 = linuxX64("linux64")

    wasm {
    }

    configure(listOf(linux64)) {
        binaries.executable("main", listOf(DEBUG)) {
            entryPoint = "com.example.app.native.main"
        }

        binaries.all {
            // Check that linker options are correctly passed to the compiler.
            linkerOpts = mutableListOf("-L.")
        }
    }

    sourceSets {
        konst commonMain by getting {
            dependencies {
                implementation("com.example:sample-lib:1.0")
            }
        }
        konst allJvm by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib")
            }
        }
        jvm6.compilations["main"].defaultSourceSet {
            dependsOn(allJvm)
        }
        jvm8.compilations["main"].defaultSourceSet {
            dependsOn(allJvm)
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
            }
        }
        nodeJs.compilations["main"].defaultSourceSet {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
            }
        }
    }
}

tasks.create("resolveRuntimeDependencies", DefaultTask::class.java) {
    doFirst { 
        // KT-26301
        konst configName = kotlin.jvm("jvm6").compilations["main"].runtimeDependencyConfigurationName
        configurations[configName].resolve()
    }
}
