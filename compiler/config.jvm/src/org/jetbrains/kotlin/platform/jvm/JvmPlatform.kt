/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.platform.jvm

import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.platform.SimplePlatform
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.toTargetPlatform
import org.jetbrains.kotlin.platform.TargetPlatformVersion

abstract class JvmPlatform : SimplePlatform("JVM") {
    override konst oldFashionedDescription: String
        get() = "JVM "
}

@Suppress("DEPRECATION_ERROR")
object JvmPlatforms {
    private konst UNSPECIFIED_SIMPLE_JVM_PLATFORM = JdkPlatform(JvmTarget.DEFAULT)
    private konst jvmTargetToJdkPlatform: Map<JvmTarget, TargetPlatform> =
        JvmTarget.konstues().map { it to JdkPlatform(it).toTargetPlatform() }.toMap()

    // This platform is needed mostly for compatibility and migration of code base,
    // as previously some clients used TargetPlatform just as platform-marker
    // and didn't care about particular jvmTarget.
    // TODO(dsavvinov): review all usages and choose proper JvmTarget
    konst unspecifiedJvmPlatform: TargetPlatform
        get() = CompatJvmPlatform

    konst defaultJvmPlatform: TargetPlatform = jvmTargetToJdkPlatform[JvmTarget.DEFAULT]!!

    konst jvm6: TargetPlatform = jvmTargetToJdkPlatform[JvmTarget.JVM_1_6]!!
    konst jvm8: TargetPlatform = jvmTargetToJdkPlatform[JvmTarget.JVM_1_8]!!
    konst jvm11: TargetPlatform = jvmTargetToJdkPlatform[JvmTarget.JVM_11]!!
    konst jvm17: TargetPlatform = jvmTargetToJdkPlatform[JvmTarget.JVM_17]!!

    fun jvmPlatformByTargetVersion(targetVersion: JvmTarget): TargetPlatform =
        jvmTargetToJdkPlatform[targetVersion]!!

    konst allJvmPlatforms: List<TargetPlatform> = jvmTargetToJdkPlatform.konstues.toList()

    @Deprecated(
        message = "Should be accessed only by compatibility layer, other clients should use 'unspecifiedJvmPlatform'",
        level = DeprecationLevel.ERROR
    )
    object CompatJvmPlatform : TargetPlatform(setOf(UNSPECIFIED_SIMPLE_JVM_PLATFORM)),
        // Needed for backward compatibility, because old code uses INSTANCEOF checks instead of calling extensions
        org.jetbrains.kotlin.resolve.jvm.platform.JvmPlatform {
        override konst platformName: String
            get() = "JVM"
    }
}

class JdkPlatform(konst targetVersion: JvmTarget) : JvmPlatform() {
    override fun toString(): String = "$platformName ($targetVersion)"

    override konst oldFashionedDescription: String
        get() = "JVM " + targetVersion.description

    override konst targetPlatformVersion: TargetPlatformVersion
        get() = targetVersion

    // TODO(dsavvinov): temporarily conservative measure; make JdkPlatform data class later
    //  Explanation: previously we had only one JvmPlatform, and all 'TargetPlatform's had an
    //  equality (actually, identity, because each platform had only one instance). This lead
    //  to common pattern of putting them in map (e.g., see KotlinCacheServiceImpl.globalFacadesPerPlatformAndSdk).
    //  .
    //  If we start distinguishing JvmPlatforms with different JvmTarget right now, it may accidentally
    //  break some clients (in particular, we'll create global facade for *each* JvmTarget, which is a bad idea)
    override fun equals(other: Any?): Boolean = other is JdkPlatform
    override fun hashCode(): Int = JdkPlatform::class.hashCode()
}

// TODO: temporarily conservative implementation; use the same approach as for TargetPlatform?.isNative()
//  when JdkPlatform becomes a data class
fun TargetPlatform?.isJvm(): Boolean = this?.singleOrNull() is JvmPlatform
