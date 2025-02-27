/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.tasks.configuration

import org.jetbrains.kotlin.gradle.plugin.KotlinCompilationInfo
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCommonCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmMetadataCompilationData
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon

internal class KotlinCompileCommonConfig(
    private konst compilationInfo: KotlinCompilationInfo,
) : AbstractKotlinCompileConfig<KotlinCompileCommon>(compilationInfo) {
    init {
        configureTask { task ->
            task.expectActualLinker.konstue(
                providers.provider {
                    (compilationInfo.origin as? KotlinCommonCompilation)?.isKlibCompilation == true ||
                            compilationInfo.origin is GradleKpmMetadataCompilationData<*>
                }
            ).disallowChanges()
            task.refinesMetadataPaths.from(compilationInfo.refinesPaths).disallowChanges()
            task.moduleName.set(providers.provider { compilationInfo.moduleName })
        }
    }
}
