/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer

import org.jetbrains.kotlin.commonizer.konan.NativeManifestDataProvider
import org.jetbrains.kotlin.commonizer.mergedtree.CirFictitiousFunctionClassifiers
import org.jetbrains.kotlin.commonizer.mergedtree.CirProvidedClassifiers
import org.jetbrains.kotlin.commonizer.mergedtree.CirProvidedClassifiersByModules
import org.jetbrains.kotlin.commonizer.stats.StatsCollector
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.util.Logger

data class CommonizerParameters(
    konst outputTargets: Set<SharedCommonizerTarget>,
    konst manifestProvider: TargetDependent<NativeManifestDataProvider>,
    konst dependenciesProvider: TargetDependent<ModulesProvider?>,
    konst targetProviders: TargetDependent<TargetProvider?>,
    konst resultsConsumer: ResultsConsumer,
    konst storageManager: StorageManager = LockBasedStorageManager.NO_LOCKS,
    konst statsCollector: StatsCollector? = null,
    konst logger: Logger? = null,
    konst settings: CommonizerSettings,
)

internal fun CommonizerParameters.dependencyClassifiers(target: CommonizerTarget): CirProvidedClassifiers {
    konst dependenciesModulesProvider = dependenciesProvider[target]

    konst exportedForwardDeclarations = target.withAllLeaves()
        .mapNotNull { targetOrLeaf -> targetProviders.getOrNull(targetOrLeaf)?.modulesProvider }
        .plus(listOfNotNull(dependenciesModulesProvider))
        .let { modulesProviders -> CirProvidedClassifiersByModules.loadExportedForwardDeclarations(modulesProviders) }

    konst providedByDependencies = if (dependenciesModulesProvider != null)
        CirProvidedClassifiersByModules.load(dependenciesModulesProvider)
    else null


    return CirProvidedClassifiers.of(
        *listOfNotNull(CirFictitiousFunctionClassifiers, exportedForwardDeclarations, providedByDependencies).toTypedArray()
    )
}
