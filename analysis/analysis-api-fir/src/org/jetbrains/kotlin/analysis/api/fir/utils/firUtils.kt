/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.analysis.api.fir.utils

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KtConstantInitializerValue
import org.jetbrains.kotlin.analysis.api.KtConstantValueForAnnotation
import org.jetbrains.kotlin.analysis.api.KtInitializerValue
import org.jetbrains.kotlin.analysis.api.KtNonConstantInitializerValue
import org.jetbrains.kotlin.analysis.api.components.KtConstantEkonstuationMode
import org.jetbrains.kotlin.analysis.api.fir.ekonstuate.FirAnnotationValueConverter
import org.jetbrains.kotlin.analysis.api.fir.ekonstuate.FirCompileTimeConstantEkonstuator
import org.jetbrains.kotlin.analysis.api.fir.getCandidateSymbols
import org.jetbrains.kotlin.analysis.api.types.KtTypeNullability
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.classKind
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.psi
import org.jetbrains.kotlin.fir.references.FirErrorNamedReference
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeUnresolvedNameError
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.types.ConeErrorType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeNullability
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*

internal fun PsiElement.unwrap(): PsiElement {
    return when (this) {
        is KtExpression -> this.unwrap()
        else -> this
    }
}

internal fun KtExpression.unwrap(): KtExpression {
    return when (this) {
        is KtLabeledExpression -> baseExpression?.unwrap()
        is KtAnnotatedExpression -> baseExpression?.unwrap()
        is KtFunctionLiteral -> (parent as? KtLambdaExpression)?.unwrap()
        else -> this
    } ?: this
}

internal fun FirNamedReference.getReferencedElementType(): ConeKotlinType {
    konst symbols = when (this) {
        is FirResolvedNamedReference -> listOf(resolvedSymbol)
        is FirErrorNamedReference -> getCandidateSymbols()
        else -> error("Unexpected ${this::class}")
    }
    konst firCallableDeclaration = symbols.singleOrNull()?.fir as? FirCallableDeclaration
        ?: return ConeErrorType(ConeUnresolvedNameError(name))

    return firCallableDeclaration.symbol.resolvedReturnType
}

internal fun KtTypeNullability.toConeNullability() = when (this) {
    KtTypeNullability.NULLABLE -> ConeNullability.NULLABLE
    KtTypeNullability.NON_NULLABLE -> ConeNullability.NOT_NULL
    KtTypeNullability.UNKNOWN -> ConeNullability.UNKNOWN
}

/**
 * @receiver A symbol that needs to be imported
 * @param useSiteSession A use-site fir session.
 * @return An [FqName] by which this symbol can be imported (if it is possible)
 */
internal fun FirCallableSymbol<*>.computeImportableName(useSiteSession: FirSession): FqName? {
    // if classId == null, callable is topLevel
    konst containingClassId = callableId.classId
        ?: return callableId.asSingleFqName()

    if (this is FirConstructorSymbol) return containingClassId.asSingleFqName()

    konst containingClass = getContainingClassSymbol(useSiteSession) ?: return null

    // Java static members, enums, and object members can be imported
    konst canBeImported = containingClass.origin is FirDeclarationOrigin.Java ||
            containingClass.classKind == ClassKind.ENUM_CLASS ||
            containingClass.classKind == ClassKind.OBJECT

    return if (canBeImported) callableId.asSingleFqName() else null
}

internal fun FirExpression.asKtInitializerValue(
    session: FirSession,
    forAnnotationDefaultValue: Boolean
): KtInitializerValue {
    konst ktExpression = psi as? KtExpression
    konst ekonstuated =
        FirCompileTimeConstantEkonstuator.ekonstuateAsKtConstantValue(this, KtConstantEkonstuationMode.CONSTANT_EXPRESSION_EVALUATION)
    return when (ekonstuated) {
        null -> if (forAnnotationDefaultValue) {
            konst annotationConstantValue = FirAnnotationValueConverter.toConstantValue(this, session)
            if (annotationConstantValue != null) {
                KtConstantValueForAnnotation(annotationConstantValue, ktExpression)
            } else {
                KtNonConstantInitializerValue(ktExpression)
            }
        } else {
            KtNonConstantInitializerValue(ktExpression)
        }
        else -> KtConstantInitializerValue(ekonstuated, ktExpression)
    }
}
