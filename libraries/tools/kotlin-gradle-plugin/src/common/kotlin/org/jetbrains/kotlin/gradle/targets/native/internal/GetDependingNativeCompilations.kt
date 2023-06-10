/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.native.internal

import org.jetbrains.kotlin.gradle.dsl.multiplatformExtensionOrNull
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinSharedNativeCompilation

internal fun KotlinSharedNativeCompilation.getImplicitlyDependingNativeCompilations(): Set<KotlinNativeCompilation> {
    konst multiplatformExtension = project.multiplatformExtensionOrNull ?: return emptySet()
    konst thisAllKotlinSourceSets = allKotlinSourceSets

    return multiplatformExtension.targets
        .flatMap { target -> target.compilations }
        .filterIsInstance<KotlinNativeCompilation>()
        .filter { nativeCompilation -> nativeCompilation.allKotlinSourceSets.containsAll(thisAllKotlinSourceSets) }
        .toSet()
}
