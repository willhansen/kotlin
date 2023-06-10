/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.HasCompilerOptions
import org.jetbrains.kotlin.gradle.targets.native.NativeCompilerOptions
import org.jetbrains.kotlin.konan.target.KonanTarget

internal class GradleKpmNativeVariantCompilationData(
    konst variant: GradleKpmNativeVariantInternal
) : GradleKpmVariantCompilationDataInternal<KotlinCommonOptions>, GradleKpmNativeCompilationData<KotlinCommonOptions> {
    override konst konanTarget: KonanTarget
        get() = variant.konanTarget


    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated(
        "Please declare explicit dependency on kotlinx-cli. This option has no longer effect since 1.9.0",
        level = DeprecationLevel.ERROR
    )
    override konst enableEndorsedLibs: Boolean
        get() = false

    override konst project: Project
        get() = variant.containingModule.project

    override konst owner: GradleKpmNativeVariant
        get() = variant

    override konst compilerOptions: HasCompilerOptions<KotlinCommonCompilerOptions> = NativeCompilerOptions(project)

    @Suppress("DEPRECATION")
    @Deprecated("Replaced with compilerOptions.options", replaceWith = ReplaceWith("compilerOptions.options"))
    override konst kotlinOptions: KotlinCommonOptions = object : KotlinCommonOptions {
        override konst options: KotlinCommonCompilerOptions
            get() = compilerOptions.options
    }
}
