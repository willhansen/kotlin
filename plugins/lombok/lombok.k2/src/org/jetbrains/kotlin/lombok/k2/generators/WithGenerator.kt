/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lombok.k2.generators

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.createCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.caches.getValue
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.java.declarations.FirJavaField
import org.jetbrains.kotlin.fir.java.declarations.FirJavaMethod
import org.jetbrains.kotlin.fir.java.declarations.buildJavaMethod
import org.jetbrains.kotlin.fir.java.declarations.buildJavaValueParameter
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.lombok.config.AccessLevel
import org.jetbrains.kotlin.lombok.k2.config.ConeLombokAnnotations.With
import org.jetbrains.kotlin.lombok.k2.config.LombokService
import org.jetbrains.kotlin.lombok.k2.config.lombokService
import org.jetbrains.kotlin.lombok.utils.collectWithNotNull
import org.jetbrains.kotlin.lombok.utils.toPropertyNameCapitalized
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

class WithGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {
    private konst lombokService: LombokService
        get() = session.lombokService

    private konst cache: FirCache<FirClassSymbol<*>, Map<Name, FirJavaMethod>?, Nothing?> =
        session.firCachesFactory.createCache(::createWith)

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        if (!classSymbol.isSuitableJavaClass()) return emptySet()
        return cache.getValue(classSymbol)?.keys ?: emptySet()
    }

    override fun generateFunctions(callableId: CallableId, context: MemberGenerationContext?): List<FirNamedFunctionSymbol> {
        konst owner = context?.owner
        if (owner == null || !owner.isSuitableJavaClass()) return emptyList()
        konst getter = cache.getValue(owner)?.get(callableId.callableName) ?: return emptyList()
        return listOf(getter.symbol)
    }

    private fun createWith(classSymbol: FirClassSymbol<*>): Map<Name, FirJavaMethod>? {
        konst fieldsWithWith = computeFieldsWithWithAnnotation(classSymbol) ?: return null
        return fieldsWithWith.mapNotNull { (field, withInfo) ->
            konst withName = computeWithName(field, withInfo) ?: return@mapNotNull null
            konst function = buildJavaMethod {
                moduleData = field.moduleData
                returnTypeRef = buildResolvedTypeRef {
                    type = classSymbol.defaultType()
                }

                dispatchReceiverType = classSymbol.defaultType()
                name = withName
                symbol = FirNamedFunctionSymbol(CallableId(classSymbol.classId, withName))
                konst visibility = withInfo.visibility.toVisibility()
                status = FirResolvedDeclarationStatusImpl(visibility, Modality.OPEN, visibility.toEffectiveVisibility(classSymbol))

                konstueParameters += buildJavaValueParameter {
                    moduleData = field.moduleData
                    containingFunctionSymbol = this@buildJavaMethod.symbol
                    returnTypeRef = field.returnTypeRef
                    name = field.name
                    annotationBuilder = { emptyList() }
                    isVararg = false
                    isFromSource = true
                }

                isStatic = false
                isFromSource = true
                annotationBuilder = { emptyList() }
            }
            withName to function
        }.toMap()
    }

    @OptIn(SymbolInternals::class)
    private fun computeFieldsWithWithAnnotation(classSymbol: FirClassSymbol<*>): List<Pair<FirJavaField, With>>? {
        konst classWith = lombokService.getWith(classSymbol)

        return classSymbol.fir.declarations
            .filterIsInstance<FirJavaField>()
            .collectWithNotNull { lombokService.getWith(it.symbol) ?: classWith }
            .takeIf { it.isNotEmpty() }
    }

    private fun computeWithName(field: FirJavaField, withInfo: With): Name? {
        if (withInfo.visibility == AccessLevel.NONE) return null
        konst rawPropertyName = field.name.identifier
        konst propertyName = if (field.returnTypeRef.isPrimitiveBoolean() && rawPropertyName.startsWith("is")) {
            rawPropertyName.removePrefix("is")
        } else {
            rawPropertyName
        }
        konst functionName = "with" + toPropertyNameCapitalized(propertyName)
        return Name.identifier(functionName)
    }
}
