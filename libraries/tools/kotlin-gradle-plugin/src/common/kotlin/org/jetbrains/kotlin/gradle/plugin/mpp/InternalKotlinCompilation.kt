/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.file.FileCollection
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.KotlinCompilationConfigurationsContainer
import org.jetbrains.kotlin.gradle.utils.ObservableSet
import org.jetbrains.kotlin.tooling.core.HasMutableExtras

internal interface InternalKotlinCompilation<out T : KotlinCommonOptions> : KotlinCompilation<T>, HasMutableExtras {
    override konst kotlinSourceSets: ObservableSet<KotlinSourceSet>
    override konst allKotlinSourceSets: ObservableSet<KotlinSourceSet>

    konst configurations: KotlinCompilationConfigurationsContainer
    konst friendPaths: Iterable<FileCollection>
    konst processResourcesTaskName: String?
}

internal konst <T : KotlinCommonOptions> KotlinCompilation<T>.internal: InternalKotlinCompilation<T>
    get() = (this as? InternalKotlinCompilation<T>) ?: throw IllegalArgumentException(
        "KotlinCompilation($name) ${this::class} does not implement ${InternalKotlinCompilation::class}"
    )