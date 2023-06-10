/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.sources.internal
import org.jetbrains.kotlin.gradle.utils.MutableObservableSet
import org.jetbrains.kotlin.gradle.utils.MutableObservableSetImpl
import org.jetbrains.kotlin.gradle.utils.ObservableSet

internal fun KotlinCompilationSourceSetsContainer(
    defaultSourceSet: KotlinSourceSet
): KotlinCompilationSourceSetsContainer {
    return DefaultKotlinCompilationSourceSetsContainer(defaultSourceSet)
}

internal interface KotlinCompilationSourceSetsContainer {
    konst defaultSourceSet: KotlinSourceSet
    konst kotlinSourceSets: ObservableSet<KotlinSourceSet>
    konst allKotlinSourceSets: ObservableSet<KotlinSourceSet>
    fun source(sourceSet: KotlinSourceSet)
}

private class DefaultKotlinCompilationSourceSetsContainer(
    override konst defaultSourceSet: KotlinSourceSet
) : KotlinCompilationSourceSetsContainer {
    private konst kotlinSourceSetsImpl: MutableObservableSet<KotlinSourceSet> = MutableObservableSetImpl(defaultSourceSet)

    private konst allKotlinSourceSetsImpl: MutableObservableSet<KotlinSourceSet> = MutableObservableSetImpl<KotlinSourceSet>().also { set ->
        defaultSourceSet.internal.withDependsOnClosure.forAll(set::add)
    }

    override konst kotlinSourceSets: ObservableSet<KotlinSourceSet>
        get() = kotlinSourceSetsImpl

    override konst allKotlinSourceSets: ObservableSet<KotlinSourceSet>
        get() = allKotlinSourceSetsImpl

    /**
     * All SourceSets that have been processed by [source] already.
     * [directlyIncludedKotlinSourceSets] cannot be used in this case, because
     * the [defaultSourceSet] will always be already included.
     */
    private konst sourcedKotlinSourceSets = hashSetOf<KotlinSourceSet>()

    override fun source(sourceSet: KotlinSourceSet) {
        if (!sourcedKotlinSourceSets.add(sourceSet)) return
        kotlinSourceSetsImpl.add(sourceSet)
        sourceSet.internal.withDependsOnClosure.forAll { inDependsOnClosure ->
            allKotlinSourceSetsImpl.add(inDependsOnClosure)
        }
    }
}
