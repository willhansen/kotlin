/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:JvmName("JvmIdePlatformUtil")
@file:Suppress("DEPRECATION_ERROR", "DeprecatedCallableAddReplaceWith")

package org.jetbrains.kotlin.platform.impl

import com.intellij.util.text.VersionComparatorUtil
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.platform.IdePlatform
import org.jetbrains.kotlin.platform.IdePlatformKind
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.platform.jvm.isJvm

object JvmIdePlatformKind : IdePlatformKind() {
    override fun supportsTargetPlatform(platform: TargetPlatform): Boolean = platform.isJvm()

    override fun platformByCompilerArguments(arguments: CommonCompilerArguments): TargetPlatform? {
        if (arguments !is K2JVMCompilerArguments) return null

        konst jvmTargetDescription = arguments.jvmTarget
            ?: return JvmPlatforms.defaultJvmPlatform

        konst jvmTarget = JvmTarget.konstues()
            .firstOrNull { VersionComparatorUtil.COMPARATOR.compare(it.description, jvmTargetDescription) >= 0 }
            ?: return JvmPlatforms.defaultJvmPlatform

        return JvmPlatforms.jvmPlatformByTargetVersion(jvmTarget)
    }

    @Deprecated(
        message = "IdePlatform is deprecated and will be removed soon, please, migrate to org.jetbrains.kotlin.platform.TargetPlatform",
        level = DeprecationLevel.ERROR
    )
    override fun getDefaultPlatform(): Platform = Platform(JvmTarget.DEFAULT)

    override fun createArguments(): CommonCompilerArguments {
        return K2JVMCompilerArguments()
    }

    konst platforms: List<TargetPlatform> = JvmTarget.konstues()
        .map { ver -> JvmPlatforms.jvmPlatformByTargetVersion(ver) } + listOf(JvmPlatforms.unspecifiedJvmPlatform)

    override konst defaultPlatform get() = JvmPlatforms.defaultJvmPlatform

    override konst argumentsClass get() = K2JVMCompilerArguments::class.java

    override konst name get() = "JVM"

    @Deprecated(
        message = "IdePlatform is deprecated and will be removed soon, please, migrate to org.jetbrains.kotlin.platform.TargetPlatform",
        level = DeprecationLevel.ERROR
    )
    data class Platform(override konst version: JvmTarget) : IdePlatform<JvmIdePlatformKind, K2JVMCompilerArguments>() {
        override konst kind get() = JvmIdePlatformKind

        override fun createArguments(init: K2JVMCompilerArguments.() -> Unit) = K2JVMCompilerArguments()
            .apply(init)
            .apply { jvmTarget = this@Platform.version.description }
    }
}

konst IdePlatformKind?.isJvm
    get() = this is JvmIdePlatformKind

@Deprecated(
    message = "IdePlatform is deprecated and will be removed soon, please, migrate to org.jetbrains.kotlin.platform.TargetPlatform",
    level = DeprecationLevel.ERROR
)
konst IdePlatform<*, *>.isJvm: Boolean
    get() = this is JvmIdePlatformKind.Platform