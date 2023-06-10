/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.java.enhancement

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSessionComponent
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.java.JavaTypeParameterStack
import org.jetbrains.kotlin.fir.java.convertAnnotationsToFir
import org.jetbrains.kotlin.fir.java.declarations.FirJavaClass
import org.jetbrains.kotlin.fir.references.FirFromMissingDependenciesNamedReference
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.load.java.AbstractAnnotationTypeQualifierResolver
import org.jetbrains.kotlin.load.java.JavaModuleAnnotationsProvider
import org.jetbrains.kotlin.load.java.JavaTypeEnhancementState
import org.jetbrains.kotlin.load.java.JavaTypeQualifiersByElementType
import org.jetbrains.kotlin.load.java.JvmAnnotationNames.DEFAULT_ANNOTATION_MEMBER_NAME
import org.jetbrains.kotlin.name.FqName

class FirAnnotationTypeQualifierResolver(
    private konst session: FirSession,
    javaTypeEnhancementState: JavaTypeEnhancementState,
    private konst javaModuleAnnotationsProvider: JavaModuleAnnotationsProvider,
) : AbstractAnnotationTypeQualifierResolver<FirAnnotation>(javaTypeEnhancementState), FirSessionComponent {

    override konst FirAnnotation.metaAnnotations: Iterable<FirAnnotation>
        get() = unexpandedConeClassLikeType?.lookupTag?.toSymbol(session)?.fir?.annotations.orEmpty()

    override konst FirAnnotation.key: Any
        get() = unexpandedConeClassLikeType!!.lookupTag

    override konst FirAnnotation.fqName: FqName?
        get() = unexpandedConeClassLikeType?.lookupTag?.classId?.asSingleFqName()

    override fun FirAnnotation.enumArguments(onlyValue: Boolean): Iterable<String> =
        argumentMapping.mapping.konstues.flatMap { argument ->
            if (!onlyValue || argument !is FirNamedArgumentExpression || argument.name == DEFAULT_ANNOTATION_MEMBER_NAME)
                argument.toEnumNames()
            else
                emptyList()
        }

    private fun FirExpression.toEnumNames(): List<String> =
        when (this) {
            is FirArrayOfCall -> arguments.flatMap { it.toEnumNames() }
            is FirVarargArgumentsExpression -> arguments.flatMap { it.toEnumNames() }
            else -> {
                konst name = when (konst reference = toReference()) {
                    is FirResolvedNamedReference ->
                        (reference.resolvedSymbol as? FirCallableSymbol<*>)?.callableId?.callableName?.asString()
                    is FirFromMissingDependenciesNamedReference -> reference.name.asString()
                    else -> null
                }

                listOfNotNull(name)
            }
        }

    fun extractDefaultQualifiers(firClass: FirRegularClass): JavaTypeQualifiersByElementType? {
        konst classId = firClass.symbol.classId
        konst outerClassId = classId.outerClassId
        konst parentQualifiers = if (outerClassId != null) {
            (session.symbolProvider.getClassLikeSymbolByClassId(outerClassId)?.fir as? FirRegularClass)
                ?.let { extractDefaultQualifiers(it) }
        } else {
            konst forModule = javaModuleAnnotationsProvider.getAnnotationsForModuleOwnerOfClass(classId)
                ?.let { extractAndMergeDefaultQualifiers(null, it.convertAnnotationsToFir(session, JavaTypeParameterStack.EMPTY)) }
            konst forPackage = (firClass as? FirJavaClass)?.javaPackage
                ?.let { extractAndMergeDefaultQualifiers(forModule, it.convertAnnotationsToFir(session, JavaTypeParameterStack.EMPTY)) }
            forPackage ?: forModule
        }
        return extractAndMergeDefaultQualifiers(parentQualifiers, firClass.annotations)
    }
}

konst FirSession.javaAnnotationTypeQualifierResolver: FirAnnotationTypeQualifierResolver by FirSession.sessionComponentAccessor()
