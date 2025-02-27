/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.sessions

import com.intellij.openapi.util.ModificationTracker
import org.jetbrains.kotlin.analysis.low.level.api.fir.LLFirModuleResolveComponents
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.fir.BuiltinTypes
import org.jetbrains.kotlin.fir.FirElementWithResolveState
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol

abstract class LLFirResolvableModuleSession(
    ktModule: KtModule,
    dependencyTracker: ModificationTracker,
    builtinTypes: BuiltinTypes
) : LLFirModuleSession(ktModule, dependencyTracker, builtinTypes, Kind.Source) {
    internal abstract konst moduleComponents: LLFirModuleResolveComponents

    final override fun getScopeSession(): ScopeSession {
        return moduleComponents.scopeSessionProvider.getScopeSession()
    }
}

internal konst FirElementWithResolveState.llFirResolvableSession: LLFirResolvableModuleSession?
    get() = llFirSession as? LLFirResolvableModuleSession

internal konst FirBasedSymbol<*>.llFirResolvableSession: LLFirResolvableModuleSession?
    get() = fir.llFirResolvableSession