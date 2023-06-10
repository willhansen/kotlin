pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    konst test_fixes_version: String by settings
    konst kotlin_version: String by settings
    plugins {
        id("org.jetbrains.kotlin.test.fixes.android") version test_fixes_version
        id("org.jetbrains.kotlin.jvm") version kotlin_version
    }
}