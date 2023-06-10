plugins {
    kotlin("multiplatform").version("<pluginMarkerVersion>")
}

repositories {
    mavenLocal()
    mavenCentral()
    mavenCentral()
}

kotlin {
    dependencies {
        commonMainImplementation(kotlin("stdlib-common"))
        commonTestApi(kotlin("test-common"))
    }

    konst jsCommon = js("jsCommon") {
        dependencies {
            commonMainImplementation(kotlin("stdlib-js"))
            commonTestApi(kotlin("test-js"))
        }
    }

    js("server")
    js("client")
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile> {
    kotlinOptions.freeCompilerArgs += "-Xforce-deprecated-legacy-compiler-usage"
}
