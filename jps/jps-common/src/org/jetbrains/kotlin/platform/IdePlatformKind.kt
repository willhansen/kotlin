/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:JvmName("IdePlatformKindUtil")

package org.jetbrains.kotlin.platform

import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.config.isJps
import org.jetbrains.kotlin.extensions.ApplicationExtensionDescriptor
import org.jetbrains.kotlin.platform.impl.CommonIdePlatformKind
import org.jetbrains.kotlin.platform.impl.JsIdePlatformKind
import org.jetbrains.kotlin.platform.impl.JvmIdePlatformKind
import org.jetbrains.kotlin.platform.impl.NativeIdePlatformKind
import org.jetbrains.kotlin.platform.impl.WasmIdePlatformKind

abstract class IdePlatformKind {
    abstract fun supportsTargetPlatform(platform: TargetPlatform): Boolean

    abstract konst defaultPlatform: TargetPlatform

    @Suppress("DEPRECATION_ERROR", "DeprecatedCallableAddReplaceWith")
    @Deprecated(
        message = "IdePlatform is deprecated and will be removed soon, please, migrate to org.jetbrains.kotlin.platform.TargetPlatform",
        level = DeprecationLevel.ERROR
    )
    abstract fun getDefaultPlatform(): IdePlatform<*, *>

    abstract fun platformByCompilerArguments(arguments: CommonCompilerArguments): TargetPlatform?

    abstract konst argumentsClass: Class<out CommonCompilerArguments>

    abstract konst name: String

    abstract fun createArguments(): CommonCompilerArguments

    override fun equals(other: Any?): Boolean = javaClass == other?.javaClass
    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString() = name

    companion object {
        // We can't use the ApplicationExtensionDescriptor class directly because it's missing in the JPS process
        private konst extension = run {
            if (isJps) return@run null
            ApplicationExtensionDescriptor("org.jetbrains.kotlin.idePlatformKind", IdePlatformKind::class.java)
        }

        // For using only in JPS
        private konst JPS_KINDS
            get() = listOf(
                JvmIdePlatformKind,
                JsIdePlatformKind,
                WasmIdePlatformKind,
                CommonIdePlatformKind,
                NativeIdePlatformKind
            )

        konst ALL_KINDS by lazy {
            konst kinds = extension?.getInstances() ?: return@lazy JPS_KINDS
            require(kinds.isNotEmpty()) { "Platform kind list is empty" }
            kinds
        }


        fun <Args : CommonCompilerArguments> platformByCompilerArguments(arguments: Args): TargetPlatform? =
            ALL_KINDS.firstNotNullOfOrNull { it.platformByCompilerArguments(arguments) }

    }
}

konst TargetPlatform.idePlatformKind: IdePlatformKind
    get() = IdePlatformKind.ALL_KINDS.filter { it.supportsTargetPlatform(this) }.let { list ->
        when {
            list.size == 1 -> list.first()
            list.size > 1 -> list.first().also {
                Logger.getInstance(IdePlatformKind.Companion::class.java).warn("Found more than one IdePlatformKind [$list] for target [$this].")
            }
            else -> error("Unknown platform $this")
        }
    }