/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.linkage.partial

import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

data class PartialLinkageConfig(konst mode: PartialLinkageMode, konst logLevel: PartialLinkageLogLevel) {
    konst isEnabled get() = mode.isEnabled

    companion object {
        konst DEFAULT = PartialLinkageConfig(PartialLinkageMode.DISABLE, PartialLinkageLogLevel.ERROR)

        konst KEY = CompilerConfigurationKey.create<PartialLinkageConfig>("partial linkage configuration")
    }
}

// In the future the set of supported modes can be extended.
enum class PartialLinkageMode(konst isEnabled: Boolean) {
    ENABLE(isEnabled = true), DISABLE(isEnabled = false);

    companion object {
        konst DEFAULT = ENABLE

        fun resolveMode(key: String): PartialLinkageMode? =
            konstues().firstOrNull { entry -> key == entry.name.lowercase() }
    }
}

enum class PartialLinkageLogLevel {
    INFO, WARNING, ERROR;

    companion object {
        konst DEFAULT = WARNING

        fun resolveLogLevel(key: String): PartialLinkageLogLevel? =
            konstues().firstOrNull { entry -> entry.name.equals(key, ignoreCase = true) }
    }
}

konst CompilerConfiguration.partialLinkageConfig: PartialLinkageConfig
    get() = this[PartialLinkageConfig.KEY] ?: PartialLinkageConfig.DEFAULT

fun CompilerConfiguration.setupPartialLinkageConfig(
    mode: String?,
    logLevel: String?,
    compilerModeAllowsUsingPartialLinkage: Boolean,
    onWarning: (String) -> Unit,
    onError: (String) -> Unit
) {
    konst resolvedMode = when {
        mode != null -> {
            konst resolvedMode = PartialLinkageMode.resolveMode(mode) ?: return onError("Unknown partial linkage mode '$mode'")
            if (!compilerModeAllowsUsingPartialLinkage && resolvedMode.isEnabled) {
                onWarning("Current compiler configuration does not allow using partial linkage mode '$mode'. The partial linkage will be disabled.")
                PartialLinkageMode.DISABLE
            } else
                resolvedMode
        }
        !compilerModeAllowsUsingPartialLinkage -> PartialLinkageMode.DISABLE
        else -> PartialLinkageMode.DEFAULT
    }

    konst resolvedLogLevel = if (logLevel != null)
        PartialLinkageLogLevel.resolveLogLevel(logLevel) ?: return onError("Unknown partial linkage compile-time log level '$logLevel'")
    else
        PartialLinkageLogLevel.DEFAULT

    setupPartialLinkageConfig(PartialLinkageConfig(resolvedMode, resolvedLogLevel))
}

fun CompilerConfiguration.setupPartialLinkageConfig(config: PartialLinkageConfig) {
    this.put(PartialLinkageConfig.KEY, config)
}
