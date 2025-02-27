/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes.impl

import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirImport
import org.jetbrains.kotlin.fir.declarations.FirResolvedImport
import org.jetbrains.kotlin.fir.declarations.builder.buildImport
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.transformers.FirImportResolveTransformer

class FirDefaultSimpleImportingScope(
    session: FirSession,
    scopeSession: ScopeSession,
    priority: DefaultImportPriority
) : FirAbstractSimpleImportingScope(session, scopeSession) {

    private fun FirImport.resolve(importResolveTransformer: FirImportResolveTransformer) =
        importResolveTransformer.transformImport(this, null) as FirResolvedImport

    override konst simpleImports = run {
        konst importResolveTransformer = FirImportResolveTransformer(session)
        konst analyzerServices = session.moduleData.analyzerServices
        konst allDefaultImports = priority.getAllDefaultImports(analyzerServices, LanguageVersionSettingsImpl.DEFAULT)
        allDefaultImports
            ?.filter { !it.isAllUnder }
            ?.map {
                buildImport {
                    importedFqName = it.fqName
                    isAllUnder = false
                }.resolve(importResolveTransformer)
            }?.groupBy { it.importedName!! } ?: emptyMap()
    }
}
