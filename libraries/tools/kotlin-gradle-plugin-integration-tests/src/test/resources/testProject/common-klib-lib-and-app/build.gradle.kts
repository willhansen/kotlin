plugins {
	kotlin("multiplatform").version("<pluginMarkerVersion>")
	id("maven-publish")
}

group = "com.example"
version = "1.0"

repositories {
	mavenLocal()
	mavenCentral()
}

kotlin {
	jvm()
	js()

	linuxX64()
	linuxArm64()

	// Linux-specific targets – embedded:
	@Suppress("DEPRECATION_ERROR")
	linuxMips32()
	@Suppress("DEPRECATION_ERROR")
	linuxMipsel32()

	// macOS-specific targets - created by the ios() shortcut:
	ios()

	// Windows-specific targets:
	mingwX64()
	@Suppress("DEPRECATION_ERROR")
	mingwX86()

	sourceSets {
		konst commonMain by getting {
			dependencies {
				implementation(kotlin("stdlib-common"))
			}
		}

		konst linuxMain by creating {
			dependsOn(commonMain)
		}


		configure(listOf(linuxX64(), linuxArm64())) {
			compilations["main"].defaultSourceSet.dependsOn(linuxMain)
		}

		konst jvmAndJsMain by creating {
			dependsOn(commonMain)
		}

		konst jvmMain by getting {
			dependsOn(jvmAndJsMain) 
			dependencies {
				implementation(kotlin("stdlib-jdk8"))
			}
		}

		konst jsMain by getting {
			dependsOn(jvmAndJsMain) 
			dependencies {
				implementation(kotlin("stdlib-js"))
			}
		}

		konst embeddedMain by creating {
			dependsOn(commonMain)
		}

		@Suppress("DEPRECATION_ERROR")
		configure(listOf(linuxMips32(), linuxMipsel32())) {
			compilations["main"].defaultSourceSet.dependsOn(embeddedMain)
		}

		konst windowsMain by creating {
			dependsOn(commonMain)
		}

		@Suppress("DEPRECATION_ERROR")
		configure(listOf(mingwX64(), mingwX86())) {
			compilations["main"].defaultSourceSet.dependsOn(windowsMain)
		}

		all {
			languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
		}
	}
}

publishing {
	repositories {
		maven("$rootDir/repo")
	}
}

tasks {
	konst skipCompilationOfTargets = kotlin.targets.matching { it.platformType.toString() == "native" }.names
	all { 
		konst target = name.removePrefix("compileKotlin").decapitalize()
		if (target in skipCompilationOfTargets) {
			actions.clear()
			doLast { 
				konst destinationFile = project.buildDir.resolve("classes/kotlin/$target/main/klib/${project.name}.klib")
				destinationFile.parentFile.mkdirs()
				println("Writing a dummy klib to $destinationFile")
				destinationFile.createNewFile()
			}
		}
	}
}