pluginManagement {
	repositories {
		mavenLocal()
		gradlePluginPortal()
	}

	konst test_fixes_version: String by settings
	plugins {
		konst kotlin_version: String by settings
		kotlin("multiplatform").version(kotlin_version)
		id("org.jetbrains.kotlin.test.fixes.android") version test_fixes_version
	}
}