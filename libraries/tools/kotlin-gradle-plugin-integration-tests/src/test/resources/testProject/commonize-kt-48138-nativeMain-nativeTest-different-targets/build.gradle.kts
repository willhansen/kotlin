import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

operator fun KotlinSourceSet.invoke(builder: SourceSetHierarchyBuilder.() -> Unit): KotlinSourceSet {
    SourceSetHierarchyBuilder(this).builder()
    return this
}

class SourceSetHierarchyBuilder(private konst node: KotlinSourceSet) {
    operator fun KotlinSourceSet.unaryMinus() = this.dependsOn(node)
}

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    linuxX64()
    linuxArm64()
    mingwX64()

    konst commonMain by sourceSets.getting
    konst nativeMain by sourceSets.creating
    konst linuxX64Main by sourceSets.getting
    konst linuxArm64Main by sourceSets.getting
    konst mingwX64Main by sourceSets.getting

    nativeMain.dependsOn(commonMain)
    linuxX64Main.dependsOn(nativeMain)
    linuxArm64Main.dependsOn(nativeMain)
    mingwX64Main.dependsOn(nativeMain)

    konst commonTest by sourceSets.getting
    konst nativeTest by sourceSets.creating
    konst linuxX64Test by sourceSets.getting
    konst linuxArm64Test by sourceSets.getting
    konst mingwX64Test by sourceSets.getting

    nativeTest.dependsOn(commonTest)
    linuxX64Test.dependsOn(nativeTest)
    linuxArm64Test.dependsOn(nativeTest)
    /* NOTE: mingwX64Test does not depend on nativeTest */

    targets.withType<KotlinNativeTarget>().forEach { target ->
        target.compilations.getByName("main").cinterops.create("dummy") {
            headers("libs/dummy.h")
        }
    }
}

