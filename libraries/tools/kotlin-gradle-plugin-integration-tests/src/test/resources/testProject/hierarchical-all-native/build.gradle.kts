plugins {
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
	konst mingwTargetName: String by project
	konst linuxTargetName: String by project
	konst macosTargetName: String by project
	konst currentHostTargetName: String by project

    konst mingw = mingwX64(mingwTargetName) { }
    konst linux = linuxX64(linuxTargetName) { }
    konst macos = macosX64(macosTargetName) { }
    konst linuxArm = linuxArm64()

	sourceSets {
		konst allNative by creating {
			dependsOn(getByName("commonMain"))
			listOf(mingw, linux, macos).forEach {
				it.compilations["main"].defaultSourceSet.dependsOn(this@creating)
			}
		}

    	konst currentHostAndLinux by creating {
    		dependsOn(allNative)
    	}

    	configure(listOf(linuxArm, targets.getByName(currentHostTargetName))) {
			compilations["main"].defaultSourceSet.dependsOn(currentHostAndLinux)
    	}
    }
}