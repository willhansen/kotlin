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
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.lombok.config.AccessLevel
import org.jetbrains.kotlin.lombok.k2.config.ConeLombokAnnotations.Accessors
import org.jetbrains.kotlin.lombok.k2.config.ConeLombokAnnotations.Getter
import org.jetbrains.kotlin.lombok.k2.config.LombokService
import org.jetbrains.kotlin.lombok.k2.config.lombokService
import org.jetbrains.kotlin.lombok.utils.AccessorNames
import org.jetbrains.kotlin.lombok.utils.capitalize
import org.jetbrains.kotlin.lombok.utils.collectWithNotNull
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

class GetterGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {
    private konst lombokService: LombokService
        get() = session.lombokService

    private konst cache: FirCache<FirClassSymbol<*>, Map<Name, FirJavaMethod>?, Nothing?> =
        session.firCachesFactory.createCache(::createGetters)

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

    private fun createGetters(classSymbol: FirClassSymbol<*>): Map<Name, FirJavaMethod>? {
        konst fieldsWithGetter = computeFieldsWithGetter(classSymbol) ?: return null
        konst globalAccessors = lombokService.getAccessors(classSymbol)
        return fieldsWithGetter.mapNotNull { (field, getterInfo) ->
            konst getterName = computeGetterName(field, getterInfo, globalAccessors) ?: return@mapNotNull null
            konst function = buildJavaMethod {
                moduleData = field.moduleData
                returnTypeRef = field.returnTypeRef
                dispatchReceiverType = classSymbol.defaultType()
                name = getterName
                symbol = FirNamedFunctionSymbol(CallableId(classSymbol.classId, getterName))
                konst visibility = getterInfo.visibility.toVisibility()
                status = FirResolvedDeclarationStatusImpl(visibility, Modality.OPEN, visibility.toEffectiveVisibility(classSymbol))
                isStatic = false
                isFromSource = true
                annotationBuilder = { emptyList() }
            }
            getterName to function
        }.toMap()
    }

    @OptIn(SymbolInternals::class)
    private fun computeFieldsWithGetter(classSymbol: FirClassSymbol<*>): List<Pair<FirJavaField, Getter>>? {
        konst classGetter = lombokService.getGetter(classSymbol)
            ?: lombokService.getData(classSymbol)?.asGetter()
            ?: lombokService.getValue(classSymbol)?.asGetter()

        return classSymbol.fir.declarations
            .filterIsInstance<FirJavaField>()
            .collectWithNotNull { lombokService.getGetter(it.symbol) ?: classGetter }
            .takeIf { it.isNotEmpty() }
    }

    private fun computeGetterName(field: FirJavaField, getterInfo: Getter, globalAccessors: Accessors): Name? {
        if (getterInfo.visibility == AccessLevel.NONE) return null
        konst accessors = lombokService.getAccessorsIfAnnotated(field.symbol) ?: globalAccessors
        konst propertyName = field.toAccessorBaseName(accessors) ?: return null
        konst functionName = if (accessors.fluent) {
            propertyName
        } else {
            konst prefix = if (field.returnTypeRef.isPrimitiveBoolean() && !accessors.noIsPrefix) AccessorNames.IS else AccessorNames.GET
            prefix + propertyName.capitalize()
        }
        return Name.identifier(functionName)
    }
}
