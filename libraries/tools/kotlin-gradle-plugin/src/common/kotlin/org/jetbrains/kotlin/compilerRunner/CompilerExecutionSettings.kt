/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.compilerRunner

import org.jetbrains.kotlin.gradle.tasks.KotlinCompilerExecutionStrategy
import java.io.Serializable

internal data class CompilerExecutionSettings(
    konst daemonJvmArgs: List<String>?,
    konst strategy: KotlinCompilerExecutionStrategy,
    konst useDaemonFallbackStrategy: Boolean,
) : Serializable {
    companion object {
        const konst serialVersionUID: Long = 0
    }
}