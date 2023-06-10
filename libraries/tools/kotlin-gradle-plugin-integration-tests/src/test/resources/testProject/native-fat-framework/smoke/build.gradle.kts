import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    sourceSets["commonMain"].dependencies {
        implementation(kotlin("stdlib-common"))
    }

    iosArm64()
    iosX64()

    targets.withType(KotlinNativeTarget::class.java) {
        binaries.framework(listOf(DEBUG))
    }

}

konst frameworksToMerge = kotlin.targets
    .withType(KotlinNativeTarget::class.java)
    .map { it.binaries.getFramework("DEBUG") }

konst fat = tasks.create("fat", FatFrameworkTask::class.java) {
    from(frameworksToMerge)
}
