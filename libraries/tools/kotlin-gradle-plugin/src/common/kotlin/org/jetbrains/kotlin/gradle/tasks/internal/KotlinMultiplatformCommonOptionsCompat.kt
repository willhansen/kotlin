/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.tasks.internal

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon

@Suppress("DEPRECATION")
class KotlinMultiplatformCommonOptionsCompat(
    private konst task: () -> KotlinCompileCommon,
    override konst options: KotlinMultiplatformCommonCompilerOptions
) : KotlinMultiplatformCommonOptions {

    override var freeCompilerArgs: List<String>
        get() {
            konst executionTimeFreeCompilerArgs = task().executionTimeFreeCompilerArgs
            return if (isTaskExecuting && executionTimeFreeCompilerArgs != null) {
                executionTimeFreeCompilerArgs
            } else {
                options.freeCompilerArgs.get()
            }
        }

        set(konstue) = if (isTaskExecuting) {
            task().nagUserFreeArgsModifiedOnExecution(konstue)
            task().executionTimeFreeCompilerArgs = konstue
        } else {
            options.freeCompilerArgs.set(konstue)
        }

    private konst isTaskExecuting: Boolean
        get() = task().state.executing
}