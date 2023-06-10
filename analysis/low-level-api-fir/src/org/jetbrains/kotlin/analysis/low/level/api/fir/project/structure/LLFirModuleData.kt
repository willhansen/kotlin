/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.project.structure

import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.fir.FirElementWithResolveState
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices

konst FirElementWithResolveState.llFirModuleData: LLFirModuleData
    get() {
        return moduleData as LLFirModuleData
    }

konst FirSession.llFirModuleData: LLFirModuleData
    get() {
        return moduleData as LLFirModuleData
    }


konst FirBasedSymbol<*>.llFirModuleData: LLFirModuleData
    get() = fir.llFirModuleData


class LLFirModuleData(
    konst ktModule: KtModule,
) : FirModuleData() {
    override konst name: Name get() = Name.special("<${ktModule.moduleDescription}>")

    override konst dependencies: List<FirModuleData> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        ktModule.directRegularDependencies.map(::LLFirModuleData)
    }

    override konst dependsOnDependencies: List<FirModuleData> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        ktModule.directDependsOnDependencies.map(::LLFirModuleData)
    }

    override konst friendDependencies: List<FirModuleData> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        ktModule.directFriendDependencies.map(::LLFirModuleData)
    }

    override konst platform: TargetPlatform get() = ktModule.platform

    override konst analyzerServices: PlatformDependentAnalyzerServices get() = ktModule.analyzerServices

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LLFirModuleData

        if (ktModule != other.ktModule) return false

        return true
    }

    override fun hashCode(): Int {
        return ktModule.hashCode()
    }
}
