plugins {
    kotlin("multiplatform")
    `maven-publish`
}

repositories {
    mavenLocal()
    mavenCentral()
}

group = "com.example.thirdparty"
version = "1.0"

kotlin {
    jvm() 
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
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib"))
            }
        }
        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        js().compilations["main"].defaultSourceSet {
            dependencies {
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

publishing {
    repositories {
        maven("<localRepo>")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile> {
    kotlinOptions.freeCompilerArgs += "-Xforce-deprecated-legacy-compiler-usage"
}
