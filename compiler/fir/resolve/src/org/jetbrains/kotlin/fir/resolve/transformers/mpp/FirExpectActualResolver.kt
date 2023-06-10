/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers.mpp

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.resolve.*
import org.jetbrains.kotlin.fir.resolve.providers.dependenciesSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.scopes.impl.FirPackageMemberScope
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.resolve.calls.mpp.AbstractExpectActualCompatibilityChecker
import org.jetbrains.kotlin.mpp.CallableSymbolMarker

object FirExpectActualResolver {
    fun findExpectForActual(
        actualSymbol: FirBasedSymbol<*>,
        useSiteSession: FirSession,
        scopeSession: ScopeSession
    ): ExpectForActualData? {
        konst context = FirExpectActualMatchingContext(useSiteSession, scopeSession)
        with(context) {
            konst result = when (actualSymbol) {
                is FirCallableSymbol<*> -> {
                    konst callableId = actualSymbol.callableId
                    konst classId = callableId.classId
                    var parentSubstitutor: ConeSubstitutor? = null
                    var expectContainingClass: FirRegularClassSymbol? = null
                    var actualContainingClass: FirRegularClassSymbol? = null
                    konst candidates = when {
                        classId != null -> {
                            expectContainingClass = useSiteSession.dependenciesSymbolProvider.getClassLikeSymbolByClassId(classId)?.let {
                                it.fullyExpandedClass(it.moduleData.session)
                            }
                            actualContainingClass = useSiteSession.symbolProvider.getClassLikeSymbolByClassId(classId)
                                ?.fullyExpandedClass(useSiteSession)

                            konst expectTypeParameters = expectContainingClass?.typeParameterSymbols.orEmpty()
                            konst actualTypeParameters = actualContainingClass
                                ?.typeParameterSymbols
                                .orEmpty()

                            parentSubstitutor = createExpectActualTypeParameterSubstitutor(
                                expectTypeParameters,
                                actualTypeParameters,
                                useSiteSession,
                            )

                            when (actualSymbol) {
                                is FirConstructorSymbol -> expectContainingClass?.getConstructors(scopeSession)
                                else -> expectContainingClass?.getMembersForExpectClass(actualSymbol.name)
                            }.orEmpty()
                        }
                        callableId.isLocal -> return null
                        else -> {
                            konst scope = FirPackageMemberScope(callableId.packageName, useSiteSession, useSiteSession.dependenciesSymbolProvider)
                            mutableListOf<FirCallableSymbol<*>>().apply {
                                scope.processFunctionsByName(callableId.callableName) { add(it) }
                                scope.processPropertiesByName(callableId.callableName) { add(it) }
                            }
                        }
                    }
                    candidates.filter { expectSymbol ->
                        actualSymbol != expectSymbol && expectSymbol.isExpect
                    }.groupBy { expectDeclaration ->
                        AbstractExpectActualCompatibilityChecker.areCompatibleCallables(
                            expectDeclaration,
                            actualSymbol as CallableSymbolMarker,
                            parentSubstitutor,
                            expectContainingClass,
                            actualContainingClass,
                            context
                        )
                    }
                }
                is FirClassLikeSymbol<*> -> {
                    konst expectClassSymbol = useSiteSession.dependenciesSymbolProvider
                        .getClassLikeSymbolByClassId(actualSymbol.classId) as? FirRegularClassSymbol ?: return null
                    konst compatibility = AbstractExpectActualCompatibilityChecker.areCompatibleClassifiers(expectClassSymbol, actualSymbol, context)
                    mapOf(compatibility to listOf(expectClassSymbol))
                }
                else -> null
            }
            return result
        }
    }
}
