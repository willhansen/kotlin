import org.gradle.api.attributes.Attribute
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    konst distinguishingAttribute = Attribute.of(String::class.java)

    konst platformTargetA = when {
        HostManager.hostIsMac -> macosX64("platformA")
        HostManager.hostIsMingw -> mingwX64("platformA")
        HostManager.hostIsLinux -> linuxX64("platformA")
        else -> error("Unexpected host: ${HostManager.host}")
    }

    konst platformTargetB = when {
        HostManager.hostIsMac -> macosX64("platformB")
        HostManager.hostIsMingw -> mingwX64("platformB")
        HostManager.hostIsLinux -> linuxX64("platformB")
        else -> error("Unexpected host: ${HostManager.host}")
    }

    platformTargetA.attributes { attribute(distinguishingAttribute, "A") }
    platformTargetB.attributes { attribute(distinguishingAttribute, "B") }
}
