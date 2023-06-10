plugins {
    kotlin("multiplatform")
}

kotlin {
    <SingleNativeTarget>("host")

    sourceSets {
        konst commonMain by getting
    }
}
