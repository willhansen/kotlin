plugins {
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

group = "com.example.app"
version = "1.0"

kotlin {
    js()

    sourceSets {
        konst commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        konst commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        js().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(project(":my-lib-foo"))
                implementation(kotlin("stdlib-js"))
            }
        }

        js().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile> {
    kotlinOptions.freeCompilerArgs += "-Xforce-deprecated-legacy-compiler-usage"
}
