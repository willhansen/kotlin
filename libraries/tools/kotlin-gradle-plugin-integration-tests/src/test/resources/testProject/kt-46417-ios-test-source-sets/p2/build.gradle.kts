plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    ios()

    konst commonMain by sourceSets.getting
    commonMain.dependencies {
        implementation(project(":p1"))
    }
}