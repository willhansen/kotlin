/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.diagnostics

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.symbols.KtCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtClassLikeSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtFunctionLikeSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtTypeParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtVariableLikeSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtVariableSymbol
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.contracts.description.EventOccurrencesRange
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.diagnostics.KtPsiDiagnostic
import org.jetbrains.kotlin.diagnostics.WhenMissingCase
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.lexer.KtKeywordToken
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtAnonymousInitializer
import org.jetbrains.kotlin.psi.KtArrayAccessExpression
import org.jetbrains.kotlin.psi.KtBackingField
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBinaryExpressionWithTypeRHS
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtConstructorDelegationCall
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtDeclarationWithBody
import org.jetbrains.kotlin.psi.KtDelegatedSuperTypeEntry
import org.jetbrains.kotlin.psi.KtDestructuringDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtExpressionWithLabel
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtLabelReferenceExpression
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtSafeQualifiedExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtSuperExpression
import org.jetbrains.kotlin.psi.KtTypeAlias
import org.jetbrains.kotlin.psi.KtTypeParameter
import org.jetbrains.kotlin.psi.KtTypeProjection
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtVariableDeclaration
import org.jetbrains.kotlin.psi.KtWhenCondition
import org.jetbrains.kotlin.psi.KtWhenEntry
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.resolve.ForbiddenNamedArgumentsTarget
import org.jetbrains.kotlin.resolve.deprecation.DeprecationInfo
import org.jetbrains.kotlin.resolve.multiplatform.ExpectActualCompatibility
import org.jetbrains.kotlin.resolve.multiplatform.ExpectActualCompatibility.Incompatible
import org.jetbrains.kotlin.types.Variance

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

internal class UnsupportedImpl(
    override konst unsupported: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.Unsupported

internal class UnsupportedFeatureImpl(
    override konst unsupportedFeature: Pair<LanguageFeature, LanguageVersionSettings>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.UnsupportedFeature

internal class NewInferenceErrorImpl(
    override konst error: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NewInferenceError

internal class OtherErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.OtherError

internal class IllegalConstExpressionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.IllegalConstExpression

internal class IllegalUnderscoreImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.IllegalUnderscore

internal class ExpressionExpectedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ExpressionExpected

internal class AssignmentInExpressionContextImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBinaryExpression>(firDiagnostic, token), KtFirDiagnostic.AssignmentInExpressionContext

internal class BreakOrContinueOutsideALoopImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.BreakOrContinueOutsideALoop

internal class NotALoopLabelImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NotALoopLabel

internal class BreakOrContinueJumpsAcrossFunctionBoundaryImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpressionWithLabel>(firDiagnostic, token), KtFirDiagnostic.BreakOrContinueJumpsAcrossFunctionBoundary

internal class VariableExpectedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.VariableExpected

internal class DelegationInInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DelegationInInterface

internal class DelegationNotToInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DelegationNotToInterface

internal class NestedClassNotAllowedImpl(
    override konst declaration: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.NestedClassNotAllowed

internal class IncorrectCharacterLiteralImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.IncorrectCharacterLiteral

internal class EmptyCharacterLiteralImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.EmptyCharacterLiteral

internal class TooManyCharactersInCharacterLiteralImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.TooManyCharactersInCharacterLiteral

internal class IllegalEscapeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.IllegalEscape

internal class IntLiteralOutOfRangeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.IntLiteralOutOfRange

internal class FloatLiteralOutOfRangeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.FloatLiteralOutOfRange

internal class WrongLongSuffixImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.WrongLongSuffix

internal class UnsignedLiteralWithoutDeclarationsOnClasspathImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.UnsignedLiteralWithoutDeclarationsOnClasspath

internal class DivisionByZeroImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.DivisionByZero

internal class ValOrVarOnLoopParameterImpl(
    override konst konstOrVar: KtKeywordToken,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.ValOrVarOnLoopParameter

internal class ValOrVarOnFunParameterImpl(
    override konst konstOrVar: KtKeywordToken,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.ValOrVarOnFunParameter

internal class ValOrVarOnCatchParameterImpl(
    override konst konstOrVar: KtKeywordToken,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.ValOrVarOnCatchParameter

internal class ValOrVarOnSecondaryConstructorParameterImpl(
    override konst konstOrVar: KtKeywordToken,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.ValOrVarOnSecondaryConstructorParameter

internal class InvisibleSetterImpl(
    override konst property: KtVariableSymbol,
    override konst visibility: Visibility,
    override konst callableId: CallableId,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.InvisibleSetter

internal class InvisibleReferenceImpl(
    override konst reference: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.InvisibleReference

internal class UnresolvedReferenceImpl(
    override konst reference: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.UnresolvedReference

internal class UnresolvedLabelImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.UnresolvedLabel

internal class DeserializationErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DeserializationError

internal class ErrorFromJavaResolutionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ErrorFromJavaResolution

internal class MissingStdlibClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.MissingStdlibClass

internal class NoThisImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NoThis

internal class DeprecationErrorImpl(
    override konst reference: KtSymbol,
    override konst message: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DeprecationError

internal class DeprecationImpl(
    override konst reference: KtSymbol,
    override konst message: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.Deprecation

internal class TypealiasExpansionDeprecationErrorImpl(
    override konst alias: KtSymbol,
    override konst reference: KtSymbol,
    override konst message: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.TypealiasExpansionDeprecationError

internal class TypealiasExpansionDeprecationImpl(
    override konst alias: KtSymbol,
    override konst reference: KtSymbol,
    override konst message: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.TypealiasExpansionDeprecation

internal class ApiNotAvailableImpl(
    override konst sinceKotlinVersion: ApiVersion,
    override konst currentVersion: ApiVersion,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ApiNotAvailable

internal class UnresolvedReferenceWrongReceiverImpl(
    override konst candidates: List<KtSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.UnresolvedReferenceWrongReceiver

internal class UnresolvedImportImpl(
    override konst reference: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.UnresolvedImport

internal class CreatingAnInstanceOfAbstractClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.CreatingAnInstanceOfAbstractClass

internal class FunctionCallExpectedImpl(
    override konst functionName: String,
    override konst hasValueParameters: Boolean,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.FunctionCallExpected

internal class IllegalSelectorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.IllegalSelector

internal class NoReceiverAllowedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NoReceiverAllowed

internal class FunctionExpectedImpl(
    override konst expression: String,
    override konst type: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.FunctionExpected

internal class ResolutionToClassifierImpl(
    override konst classSymbol: KtClassLikeSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ResolutionToClassifier

internal class AmbiguousAlteredAssignImpl(
    override konst altererNames: List<String?>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.AmbiguousAlteredAssign

internal class ForbiddenBinaryModImpl(
    override konst forbiddenFunction: KtSymbol,
    override konst suggestedFunction: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ForbiddenBinaryMod

internal class DeprecatedBinaryModImpl(
    override konst forbiddenFunction: KtSymbol,
    override konst suggestedFunction: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DeprecatedBinaryMod

internal class SuperIsNotAnExpressionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.SuperIsNotAnExpression

internal class SuperNotAvailableImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.SuperNotAvailable

internal class AbstractSuperCallImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.AbstractSuperCall

internal class AbstractSuperCallWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.AbstractSuperCallWarning

internal class InstanceAccessBeforeSuperCallImpl(
    override konst target: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.InstanceAccessBeforeSuperCall

internal class SuperCallWithDefaultParametersImpl(
    override konst name: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.SuperCallWithDefaultParameters

internal class InterfaceCantCallDefaultMethodViaSuperImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.InterfaceCantCallDefaultMethodViaSuper

internal class NotASupertypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NotASupertype

internal class TypeArgumentsRedundantInSuperQualifierImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.TypeArgumentsRedundantInSuperQualifier

internal class SuperclassNotAccessibleFromInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.SuperclassNotAccessibleFromInterface

internal class QualifiedSupertypeExtendedByOtherSupertypeImpl(
    override konst otherSuperType: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.QualifiedSupertypeExtendedByOtherSupertype

internal class SupertypeInitializedInInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.SupertypeInitializedInInterface

internal class InterfaceWithSuperclassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.InterfaceWithSuperclass

internal class FinalSupertypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.FinalSupertype

internal class ClassCannotBeExtendedDirectlyImpl(
    override konst classSymbol: KtClassLikeSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.ClassCannotBeExtendedDirectly

internal class SupertypeIsExtensionFunctionTypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.SupertypeIsExtensionFunctionType

internal class SingletonInSupertypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.SingletonInSupertype

internal class NullableSupertypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.NullableSupertype

internal class ManyClassesInSupertypeListImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.ManyClassesInSupertypeList

internal class SupertypeAppearsTwiceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.SupertypeAppearsTwice

internal class ClassInSupertypeForEnumImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.ClassInSupertypeForEnum

internal class SealedSupertypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.SealedSupertype

internal class SealedSupertypeInLocalClassImpl(
    override konst declarationType: String,
    override konst sealedClassKind: ClassKind,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.SealedSupertypeInLocalClass

internal class SealedInheritorInDifferentPackageImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.SealedInheritorInDifferentPackage

internal class SealedInheritorInDifferentModuleImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.SealedInheritorInDifferentModule

internal class ClassInheritsJavaSealedClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.ClassInheritsJavaSealedClass

internal class SupertypeNotAClassOrInterfaceImpl(
    override konst reason: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.SupertypeNotAClassOrInterface

internal class CyclicInheritanceHierarchyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.CyclicInheritanceHierarchy

internal class ExpandedTypeCannotBeInheritedImpl(
    override konst type: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.ExpandedTypeCannotBeInherited

internal class ProjectionInImmediateArgumentToSupertypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.ProjectionInImmediateArgumentToSupertype

internal class InconsistentTypeParameterValuesImpl(
    override konst typeParameter: KtTypeParameterSymbol,
    override konst type: KtClassLikeSymbol,
    override konst bounds: List<KtType>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClass>(firDiagnostic, token), KtFirDiagnostic.InconsistentTypeParameterValues

internal class InconsistentTypeParameterBoundsImpl(
    override konst typeParameter: KtTypeParameterSymbol,
    override konst type: KtClassLikeSymbol,
    override konst bounds: List<KtType>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.InconsistentTypeParameterBounds

internal class AmbiguousSuperImpl(
    override konst candidates: List<KtType>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtSuperExpression>(firDiagnostic, token), KtFirDiagnostic.AmbiguousSuper

internal class WrongMultipleInheritanceImpl(
    override konst symbol: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.WrongMultipleInheritance

internal class ConstructorInObjectImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.ConstructorInObject

internal class ConstructorInInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.ConstructorInInterface

internal class NonPrivateConstructorInEnumImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NonPrivateConstructorInEnum

internal class NonPrivateOrProtectedConstructorInSealedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NonPrivateOrProtectedConstructorInSealed

internal class CyclicConstructorDelegationCallImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.CyclicConstructorDelegationCall

internal class PrimaryConstructorDelegationCallExpectedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.PrimaryConstructorDelegationCallExpected

internal class SupertypeNotInitializedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.SupertypeNotInitialized

internal class SupertypeInitializedWithoutPrimaryConstructorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.SupertypeInitializedWithoutPrimaryConstructor

internal class DelegationSuperCallInEnumConstructorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DelegationSuperCallInEnumConstructor

internal class PrimaryConstructorRequiredForDataClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.PrimaryConstructorRequiredForDataClass

internal class ExplicitDelegationCallRequiredImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ExplicitDelegationCallRequired

internal class SealedClassConstructorCallImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.SealedClassConstructorCall

internal class DataClassWithoutParametersImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtPrimaryConstructor>(firDiagnostic, token), KtFirDiagnostic.DataClassWithoutParameters

internal class DataClassVarargParameterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.DataClassVarargParameter

internal class DataClassNotPropertyParameterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.DataClassNotPropertyParameter

internal class AnnotationArgumentKclassLiteralOfTypeParameterErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.AnnotationArgumentKclassLiteralOfTypeParameterError

internal class AnnotationArgumentMustBeConstImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.AnnotationArgumentMustBeConst

internal class AnnotationArgumentMustBeEnumConstImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.AnnotationArgumentMustBeEnumConst

internal class AnnotationArgumentMustBeKclassLiteralImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.AnnotationArgumentMustBeKclassLiteral

internal class AnnotationClassMemberImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.AnnotationClassMember

internal class AnnotationParameterDefaultValueMustBeConstantImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.AnnotationParameterDefaultValueMustBeConstant

internal class InkonstidTypeOfAnnotationMemberImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.InkonstidTypeOfAnnotationMember

internal class LocalAnnotationClassErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.LocalAnnotationClassError

internal class MissingValOnAnnotationParameterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.MissingValOnAnnotationParameter

internal class NonConstValUsedInConstantExpressionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.NonConstValUsedInConstantExpression

internal class CycleInAnnotationParameterErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.CycleInAnnotationParameterError

internal class CycleInAnnotationParameterWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.CycleInAnnotationParameterWarning

internal class AnnotationClassConstructorCallImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtCallExpression>(firDiagnostic, token), KtFirDiagnostic.AnnotationClassConstructorCall

internal class NotAnAnnotationClassImpl(
    override konst annotationName: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NotAnAnnotationClass

internal class NullableTypeOfAnnotationMemberImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.NullableTypeOfAnnotationMember

internal class VarAnnotationParameterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.VarAnnotationParameter

internal class SupertypesForAnnotationClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClass>(firDiagnostic, token), KtFirDiagnostic.SupertypesForAnnotationClass

internal class AnnotationUsedAsAnnotationArgumentImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.AnnotationUsedAsAnnotationArgument

internal class IllegalKotlinVersionStringValueImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.IllegalKotlinVersionStringValue

internal class NewerVersionInSinceKotlinImpl(
    override konst specifiedVersion: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.NewerVersionInSinceKotlin

internal class DeprecatedSinceKotlinWithUnorderedVersionsImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DeprecatedSinceKotlinWithUnorderedVersions

internal class DeprecatedSinceKotlinWithoutArgumentsImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DeprecatedSinceKotlinWithoutArguments

internal class DeprecatedSinceKotlinWithoutDeprecatedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DeprecatedSinceKotlinWithoutDeprecated

internal class DeprecatedSinceKotlinWithDeprecatedLevelImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DeprecatedSinceKotlinWithDeprecatedLevel

internal class DeprecatedSinceKotlinOutsideKotlinSubpackageImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DeprecatedSinceKotlinOutsideKotlinSubpackage

internal class OverrideDeprecationImpl(
    override konst overridenSymbol: KtSymbol,
    override konst deprecationInfo: DeprecationInfo,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.OverrideDeprecation

internal class AnnotationOnSuperclassErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.AnnotationOnSuperclassError

internal class AnnotationOnSuperclassWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.AnnotationOnSuperclassWarning

internal class RestrictedRetentionForExpressionAnnotationErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.RestrictedRetentionForExpressionAnnotationError

internal class RestrictedRetentionForExpressionAnnotationWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.RestrictedRetentionForExpressionAnnotationWarning

internal class WrongAnnotationTargetImpl(
    override konst actualTarget: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.WrongAnnotationTarget

internal class WrongAnnotationTargetWithUseSiteTargetImpl(
    override konst actualTarget: String,
    override konst useSiteTarget: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.WrongAnnotationTargetWithUseSiteTarget

internal class InapplicableTargetOnPropertyImpl(
    override konst useSiteDescription: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.InapplicableTargetOnProperty

internal class InapplicableTargetOnPropertyWarningImpl(
    override konst useSiteDescription: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.InapplicableTargetOnPropertyWarning

internal class InapplicableTargetPropertyImmutableImpl(
    override konst useSiteDescription: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.InapplicableTargetPropertyImmutable

internal class InapplicableTargetPropertyHasNoDelegateImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.InapplicableTargetPropertyHasNoDelegate

internal class InapplicableTargetPropertyHasNoBackingFieldImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.InapplicableTargetPropertyHasNoBackingField

internal class InapplicableParamTargetImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.InapplicableParamTarget

internal class RedundantAnnotationTargetImpl(
    override konst useSiteDescription: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.RedundantAnnotationTarget

internal class InapplicableFileTargetImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.InapplicableFileTarget

internal class RepeatedAnnotationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.RepeatedAnnotation

internal class RepeatedAnnotationWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.RepeatedAnnotationWarning

internal class NotAClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NotAClass

internal class WrongExtensionFunctionTypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.WrongExtensionFunctionType

internal class WrongExtensionFunctionTypeWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.WrongExtensionFunctionTypeWarning

internal class AnnotationInWhereClauseErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.AnnotationInWhereClauseError

internal class PluginAnnotationAmbiguityImpl(
    override konst typeFromCompilerPhase: KtType,
    override konst typeFromTypesPhase: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.PluginAnnotationAmbiguity

internal class AmbiguousAnnotationArgumentImpl(
    override konst symbols: List<KtSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.AmbiguousAnnotationArgument

internal class VolatileOnValueImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.VolatileOnValue

internal class VolatileOnDelegateImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.VolatileOnDelegate

internal class WrongJsQualifierImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.WrongJsQualifier

internal class JsModuleProhibitedOnVarImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.JsModuleProhibitedOnVar

internal class JsModuleProhibitedOnNonNativeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.JsModuleProhibitedOnNonNative

internal class NestedJsModuleProhibitedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.NestedJsModuleProhibited

internal class RuntimeAnnotationNotSupportedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.RuntimeAnnotationNotSupported

internal class RuntimeAnnotationOnExternalDeclarationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.RuntimeAnnotationOnExternalDeclaration

internal class NativeAnnotationsAllowedOnlyOnMemberOrExtensionFunImpl(
    override konst type: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.NativeAnnotationsAllowedOnlyOnMemberOrExtensionFun

internal class NativeIndexerKeyShouldBeStringOrNumberImpl(
    override konst kind: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.NativeIndexerKeyShouldBeStringOrNumber

internal class NativeIndexerWrongParameterCountImpl(
    override konst parametersCount: Int,
    override konst kind: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.NativeIndexerWrongParameterCount

internal class NativeIndexerCanNotHaveDefaultArgumentsImpl(
    override konst kind: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.NativeIndexerCanNotHaveDefaultArguments

internal class NativeGetterReturnTypeShouldBeNullableImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.NativeGetterReturnTypeShouldBeNullable

internal class NativeSetterWrongReturnTypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.NativeSetterWrongReturnType

internal class JsNameIsNotOnAllAccessorsImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.JsNameIsNotOnAllAccessors

internal class JsNameProhibitedForNamedNativeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.JsNameProhibitedForNamedNative

internal class JsNameProhibitedForOverrideImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.JsNameProhibitedForOverride

internal class JsNameOnPrimaryConstructorProhibitedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.JsNameOnPrimaryConstructorProhibited

internal class JsNameOnAccessorAndPropertyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.JsNameOnAccessorAndProperty

internal class JsNameProhibitedForExtensionPropertyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.JsNameProhibitedForExtensionProperty

internal class OptInUsageImpl(
    override konst optInMarkerFqName: FqName,
    override konst message: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.OptInUsage

internal class OptInUsageErrorImpl(
    override konst optInMarkerFqName: FqName,
    override konst message: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.OptInUsageError

internal class OptInOverrideImpl(
    override konst optInMarkerFqName: FqName,
    override konst message: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.OptInOverride

internal class OptInOverrideErrorImpl(
    override konst optInMarkerFqName: FqName,
    override konst message: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.OptInOverrideError

internal class OptInIsNotEnabledImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.OptInIsNotEnabled

internal class OptInCanOnlyBeUsedAsAnnotationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.OptInCanOnlyBeUsedAsAnnotation

internal class OptInMarkerCanOnlyBeUsedAsAnnotationOrArgumentInOptInImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.OptInMarkerCanOnlyBeUsedAsAnnotationOrArgumentInOptIn

internal class OptInWithoutArgumentsImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.OptInWithoutArguments

internal class OptInArgumentIsNotMarkerImpl(
    override konst notMarkerFqName: FqName,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.OptInArgumentIsNotMarker

internal class OptInMarkerWithWrongTargetImpl(
    override konst target: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.OptInMarkerWithWrongTarget

internal class OptInMarkerWithWrongRetentionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.OptInMarkerWithWrongRetention

internal class OptInMarkerOnWrongTargetImpl(
    override konst target: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.OptInMarkerOnWrongTarget

internal class OptInMarkerOnOverrideImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.OptInMarkerOnOverride

internal class OptInMarkerOnOverrideWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.OptInMarkerOnOverrideWarning

internal class SubclassOptInInapplicableImpl(
    override konst target: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.SubclassOptInInapplicable

internal class ExposedTypealiasExpandedTypeImpl(
    override konst elementVisibility: EffectiveVisibility,
    override konst restrictingDeclaration: KtSymbol,
    override konst restrictingVisibility: EffectiveVisibility,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.ExposedTypealiasExpandedType

internal class ExposedFunctionReturnTypeImpl(
    override konst elementVisibility: EffectiveVisibility,
    override konst restrictingDeclaration: KtSymbol,
    override konst restrictingVisibility: EffectiveVisibility,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.ExposedFunctionReturnType

internal class ExposedReceiverTypeImpl(
    override konst elementVisibility: EffectiveVisibility,
    override konst restrictingDeclaration: KtSymbol,
    override konst restrictingVisibility: EffectiveVisibility,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.ExposedReceiverType

internal class ExposedPropertyTypeImpl(
    override konst elementVisibility: EffectiveVisibility,
    override konst restrictingDeclaration: KtSymbol,
    override konst restrictingVisibility: EffectiveVisibility,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.ExposedPropertyType

internal class ExposedPropertyTypeInConstructorErrorImpl(
    override konst elementVisibility: EffectiveVisibility,
    override konst restrictingDeclaration: KtSymbol,
    override konst restrictingVisibility: EffectiveVisibility,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.ExposedPropertyTypeInConstructorError

internal class ExposedPropertyTypeInConstructorWarningImpl(
    override konst elementVisibility: EffectiveVisibility,
    override konst restrictingDeclaration: KtSymbol,
    override konst restrictingVisibility: EffectiveVisibility,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.ExposedPropertyTypeInConstructorWarning

internal class ExposedParameterTypeImpl(
    override konst elementVisibility: EffectiveVisibility,
    override konst restrictingDeclaration: KtSymbol,
    override konst restrictingVisibility: EffectiveVisibility,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.ExposedParameterType

internal class ExposedSuperInterfaceImpl(
    override konst elementVisibility: EffectiveVisibility,
    override konst restrictingDeclaration: KtSymbol,
    override konst restrictingVisibility: EffectiveVisibility,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.ExposedSuperInterface

internal class ExposedSuperClassImpl(
    override konst elementVisibility: EffectiveVisibility,
    override konst restrictingDeclaration: KtSymbol,
    override konst restrictingVisibility: EffectiveVisibility,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.ExposedSuperClass

internal class ExposedTypeParameterBoundImpl(
    override konst elementVisibility: EffectiveVisibility,
    override konst restrictingDeclaration: KtSymbol,
    override konst restrictingVisibility: EffectiveVisibility,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.ExposedTypeParameterBound

internal class InapplicableInfixModifierImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.InapplicableInfixModifier

internal class RepeatedModifierImpl(
    override konst modifier: KtModifierKeywordToken,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.RepeatedModifier

internal class RedundantModifierImpl(
    override konst redundantModifier: KtModifierKeywordToken,
    override konst conflictingModifier: KtModifierKeywordToken,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.RedundantModifier

internal class DeprecatedModifierImpl(
    override konst deprecatedModifier: KtModifierKeywordToken,
    override konst actualModifier: KtModifierKeywordToken,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DeprecatedModifier

internal class DeprecatedModifierPairImpl(
    override konst deprecatedModifier: KtModifierKeywordToken,
    override konst conflictingModifier: KtModifierKeywordToken,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DeprecatedModifierPair

internal class DeprecatedModifierForTargetImpl(
    override konst deprecatedModifier: KtModifierKeywordToken,
    override konst target: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DeprecatedModifierForTarget

internal class RedundantModifierForTargetImpl(
    override konst redundantModifier: KtModifierKeywordToken,
    override konst target: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.RedundantModifierForTarget

internal class IncompatibleModifiersImpl(
    override konst modifier1: KtModifierKeywordToken,
    override konst modifier2: KtModifierKeywordToken,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.IncompatibleModifiers

internal class RedundantOpenInInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.RedundantOpenInInterface

internal class WrongModifierTargetImpl(
    override konst modifier: KtModifierKeywordToken,
    override konst target: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.WrongModifierTarget

internal class OperatorModifierRequiredImpl(
    override konst functionSymbol: KtFunctionLikeSymbol,
    override konst name: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.OperatorModifierRequired

internal class InfixModifierRequiredImpl(
    override konst functionSymbol: KtFunctionLikeSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.InfixModifierRequired

internal class WrongModifierContainingDeclarationImpl(
    override konst modifier: KtModifierKeywordToken,
    override konst target: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.WrongModifierContainingDeclaration

internal class DeprecatedModifierContainingDeclarationImpl(
    override konst modifier: KtModifierKeywordToken,
    override konst target: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DeprecatedModifierContainingDeclaration

internal class InapplicableOperatorModifierImpl(
    override konst message: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.InapplicableOperatorModifier

internal class NoExplicitVisibilityInApiModeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.NoExplicitVisibilityInApiMode

internal class NoExplicitVisibilityInApiModeWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.NoExplicitVisibilityInApiModeWarning

internal class NoExplicitReturnTypeInApiModeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.NoExplicitReturnTypeInApiMode

internal class NoExplicitReturnTypeInApiModeWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.NoExplicitReturnTypeInApiModeWarning

internal class ValueClassNotTopLevelImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.ValueClassNotTopLevel

internal class ValueClassNotFinalImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.ValueClassNotFinal

internal class AbsenceOfPrimaryConstructorForValueClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.AbsenceOfPrimaryConstructorForValueClass

internal class InlineClassConstructorWrongParametersSizeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.InlineClassConstructorWrongParametersSize

internal class ValueClassEmptyConstructorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ValueClassEmptyConstructor

internal class ValueClassConstructorNotFinalReadOnlyParameterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.ValueClassConstructorNotFinalReadOnlyParameter

internal class PropertyWithBackingFieldInsideValueClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.PropertyWithBackingFieldInsideValueClass

internal class DelegatedPropertyInsideValueClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DelegatedPropertyInsideValueClass

internal class ValueClassHasInapplicableParameterTypeImpl(
    override konst type: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.ValueClassHasInapplicableParameterType

internal class ValueClassCannotImplementInterfaceByDelegationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ValueClassCannotImplementInterfaceByDelegation

internal class ValueClassCannotExtendClassesImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.ValueClassCannotExtendClasses

internal class ValueClassCannotBeRecursiveImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.ValueClassCannotBeRecursive

internal class MultiFieldValueClassPrimaryConstructorDefaultParameterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.MultiFieldValueClassPrimaryConstructorDefaultParameter

internal class SecondaryConstructorWithBodyInsideValueClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.SecondaryConstructorWithBodyInsideValueClass

internal class ReservedMemberInsideValueClassImpl(
    override konst name: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtFunction>(firDiagnostic, token), KtFirDiagnostic.ReservedMemberInsideValueClass

internal class TypeArgumentOnTypedValueClassEqualsImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.TypeArgumentOnTypedValueClassEquals

internal class InnerClassInsideValueClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.InnerClassInsideValueClass

internal class ValueClassCannotBeCloneableImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.ValueClassCannotBeCloneable

internal class AnnotationOnIllegalMultiFieldValueClassTypedTargetImpl(
    override konst name: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.AnnotationOnIllegalMultiFieldValueClassTypedTarget

internal class NoneApplicableImpl(
    override konst candidates: List<KtSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NoneApplicable

internal class InapplicableCandidateImpl(
    override konst candidate: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.InapplicableCandidate

internal class TypeMismatchImpl(
    override konst expectedType: KtType,
    override konst actualType: KtType,
    override konst isMismatchDueToNullability: Boolean,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.TypeMismatch

internal class TypeInferenceOnlyInputTypesErrorImpl(
    override konst typeParameter: KtTypeParameterSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.TypeInferenceOnlyInputTypesError

internal class ThrowableTypeMismatchImpl(
    override konst actualType: KtType,
    override konst isMismatchDueToNullability: Boolean,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ThrowableTypeMismatch

internal class ConditionTypeMismatchImpl(
    override konst actualType: KtType,
    override konst isMismatchDueToNullability: Boolean,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ConditionTypeMismatch

internal class ArgumentTypeMismatchImpl(
    override konst expectedType: KtType,
    override konst actualType: KtType,
    override konst isMismatchDueToNullability: Boolean,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ArgumentTypeMismatch

internal class NullForNonnullTypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NullForNonnullType

internal class InapplicableLateinitModifierImpl(
    override konst reason: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.InapplicableLateinitModifier

internal class VarargOutsideParenthesesImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.VarargOutsideParentheses

internal class NamedArgumentsNotAllowedImpl(
    override konst forbiddenNamedArgumentsTarget: ForbiddenNamedArgumentsTarget,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtValueArgument>(firDiagnostic, token), KtFirDiagnostic.NamedArgumentsNotAllowed

internal class NonVarargSpreadImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<LeafPsiElement>(firDiagnostic, token), KtFirDiagnostic.NonVarargSpread

internal class ArgumentPassedTwiceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtValueArgument>(firDiagnostic, token), KtFirDiagnostic.ArgumentPassedTwice

internal class TooManyArgumentsImpl(
    override konst function: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.TooManyArguments

internal class NoValueForParameterImpl(
    override konst violatedParameter: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.NoValueForParameter

internal class NamedParameterNotFoundImpl(
    override konst name: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtValueArgument>(firDiagnostic, token), KtFirDiagnostic.NamedParameterNotFound

internal class NameForAmbiguousParameterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtValueArgument>(firDiagnostic, token), KtFirDiagnostic.NameForAmbiguousParameter

internal class AssignmentTypeMismatchImpl(
    override konst expectedType: KtType,
    override konst actualType: KtType,
    override konst isMismatchDueToNullability: Boolean,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.AssignmentTypeMismatch

internal class ResultTypeMismatchImpl(
    override konst expectedType: KtType,
    override konst actualType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.ResultTypeMismatch

internal class ManyLambdaExpressionArgumentsImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtValueArgument>(firDiagnostic, token), KtFirDiagnostic.ManyLambdaExpressionArguments

internal class NewInferenceNoInformationForParameterImpl(
    override konst name: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.NewInferenceNoInformationForParameter

internal class SpreadOfNullableImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.SpreadOfNullable

internal class AssigningSingleElementToVarargInNamedFormFunctionErrorImpl(
    override konst expectedArrayType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.AssigningSingleElementToVarargInNamedFormFunctionError

internal class AssigningSingleElementToVarargInNamedFormFunctionWarningImpl(
    override konst expectedArrayType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.AssigningSingleElementToVarargInNamedFormFunctionWarning

internal class AssigningSingleElementToVarargInNamedFormAnnotationErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.AssigningSingleElementToVarargInNamedFormAnnotationError

internal class AssigningSingleElementToVarargInNamedFormAnnotationWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.AssigningSingleElementToVarargInNamedFormAnnotationWarning

internal class RedundantSpreadOperatorInNamedFormInAnnotationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.RedundantSpreadOperatorInNamedFormInAnnotation

internal class RedundantSpreadOperatorInNamedFormInFunctionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.RedundantSpreadOperatorInNamedFormInFunction

internal class InferenceUnsuccessfulForkImpl(
    override konst message: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.InferenceUnsuccessfulFork

internal class OverloadResolutionAmbiguityImpl(
    override konst candidates: List<KtSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.OverloadResolutionAmbiguity

internal class AssignOperatorAmbiguityImpl(
    override konst candidates: List<KtSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.AssignOperatorAmbiguity

internal class IteratorAmbiguityImpl(
    override konst candidates: List<KtSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.IteratorAmbiguity

internal class HasNextFunctionAmbiguityImpl(
    override konst candidates: List<KtSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.HasNextFunctionAmbiguity

internal class NextAmbiguityImpl(
    override konst candidates: List<KtSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NextAmbiguity

internal class AmbiguousFunctionTypeKindImpl(
    override konst kinds: List<FunctionTypeKind>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.AmbiguousFunctionTypeKind

internal class NoContextReceiverImpl(
    override konst contextReceiverRepresentation: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.NoContextReceiver

internal class MultipleArgumentsApplicableForContextReceiverImpl(
    override konst contextReceiverRepresentation: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.MultipleArgumentsApplicableForContextReceiver

internal class AmbiguousCallWithImplicitContextReceiverImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.AmbiguousCallWithImplicitContextReceiver

internal class UnsupportedContextualDeclarationCallImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.UnsupportedContextualDeclarationCall

internal class RecursionInImplicitTypesImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.RecursionInImplicitTypes

internal class InferenceErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.InferenceError

internal class ProjectionOnNonClassTypeArgumentImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ProjectionOnNonClassTypeArgument

internal class UpperBoundViolatedImpl(
    override konst expectedUpperBound: KtType,
    override konst actualUpperBound: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.UpperBoundViolated

internal class UpperBoundViolatedInTypealiasExpansionImpl(
    override konst expectedUpperBound: KtType,
    override konst actualUpperBound: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.UpperBoundViolatedInTypealiasExpansion

internal class TypeArgumentsNotAllowedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.TypeArgumentsNotAllowed

internal class WrongNumberOfTypeArgumentsImpl(
    override konst expectedCount: Int,
    override konst classifier: KtClassLikeSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.WrongNumberOfTypeArguments

internal class NoTypeArgumentsOnRhsImpl(
    override konst expectedCount: Int,
    override konst classifier: KtClassLikeSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NoTypeArgumentsOnRhs

internal class OuterClassArgumentsRequiredImpl(
    override konst outer: KtClassLikeSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.OuterClassArgumentsRequired

internal class TypeParametersInObjectImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.TypeParametersInObject

internal class TypeParametersInAnonymousObjectImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.TypeParametersInAnonymousObject

internal class IllegalProjectionUsageImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.IllegalProjectionUsage

internal class TypeParametersInEnumImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.TypeParametersInEnum

internal class ConflictingProjectionImpl(
    override konst type: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeProjection>(firDiagnostic, token), KtFirDiagnostic.ConflictingProjection

internal class ConflictingProjectionInTypealiasExpansionImpl(
    override konst type: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ConflictingProjectionInTypealiasExpansion

internal class RedundantProjectionImpl(
    override konst type: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeProjection>(firDiagnostic, token), KtFirDiagnostic.RedundantProjection

internal class VarianceOnTypeParameterNotAllowedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeParameter>(firDiagnostic, token), KtFirDiagnostic.VarianceOnTypeParameterNotAllowed

internal class CatchParameterWithDefaultValueImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.CatchParameterWithDefaultValue

internal class ReifiedTypeInCatchClauseImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ReifiedTypeInCatchClause

internal class TypeParameterInCatchClauseImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.TypeParameterInCatchClause

internal class GenericThrowableSubclassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeParameter>(firDiagnostic, token), KtFirDiagnostic.GenericThrowableSubclass

internal class InnerClassOfGenericThrowableSubclassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.InnerClassOfGenericThrowableSubclass

internal class KclassWithNullableTypeParameterInSignatureImpl(
    override konst typeParameter: KtTypeParameterSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.KclassWithNullableTypeParameterInSignature

internal class TypeParameterAsReifiedImpl(
    override konst typeParameter: KtTypeParameterSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.TypeParameterAsReified

internal class TypeParameterAsReifiedArrayErrorImpl(
    override konst typeParameter: KtTypeParameterSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.TypeParameterAsReifiedArrayError

internal class TypeParameterAsReifiedArrayWarningImpl(
    override konst typeParameter: KtTypeParameterSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.TypeParameterAsReifiedArrayWarning

internal class ReifiedTypeForbiddenSubstitutionImpl(
    override konst type: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ReifiedTypeForbiddenSubstitution

internal class DefinitelyNonNullableAsReifiedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DefinitelyNonNullableAsReified

internal class FinalUpperBoundImpl(
    override konst type: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.FinalUpperBound

internal class UpperBoundIsExtensionFunctionTypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.UpperBoundIsExtensionFunctionType

internal class BoundsNotAllowedIfBoundedByTypeParameterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.BoundsNotAllowedIfBoundedByTypeParameter

internal class OnlyOneClassBoundAllowedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.OnlyOneClassBoundAllowed

internal class RepeatedBoundImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.RepeatedBound

internal class ConflictingUpperBoundsImpl(
    override konst typeParameter: KtTypeParameterSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.ConflictingUpperBounds

internal class NameInConstraintIsNotATypeParameterImpl(
    override konst typeParameterName: Name,
    override konst typeParametersOwner: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtSimpleNameExpression>(firDiagnostic, token), KtFirDiagnostic.NameInConstraintIsNotATypeParameter

internal class BoundOnTypeAliasParameterNotAllowedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.BoundOnTypeAliasParameterNotAllowed

internal class ReifiedTypeParameterNoInlineImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeParameter>(firDiagnostic, token), KtFirDiagnostic.ReifiedTypeParameterNoInline

internal class TypeParametersNotAllowedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.TypeParametersNotAllowed

internal class TypeParameterOfPropertyNotUsedInReceiverImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeParameter>(firDiagnostic, token), KtFirDiagnostic.TypeParameterOfPropertyNotUsedInReceiver

internal class ReturnTypeMismatchImpl(
    override konst expectedType: KtType,
    override konst actualType: KtType,
    override konst targetFunction: KtSymbol,
    override konst isMismatchDueToNullability: Boolean,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.ReturnTypeMismatch

internal class ImplicitNothingReturnTypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ImplicitNothingReturnType

internal class ImplicitNothingPropertyTypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ImplicitNothingPropertyType

internal class CyclicGenericUpperBoundImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.CyclicGenericUpperBound

internal class DeprecatedTypeParameterSyntaxImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.DeprecatedTypeParameterSyntax

internal class MisplacedTypeParameterConstraintsImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeParameter>(firDiagnostic, token), KtFirDiagnostic.MisplacedTypeParameterConstraints

internal class DynamicSupertypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.DynamicSupertype

internal class DynamicUpperBoundImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.DynamicUpperBound

internal class DynamicReceiverNotAllowedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.DynamicReceiverNotAllowed

internal class IncompatibleTypesImpl(
    override konst typeA: KtType,
    override konst typeB: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.IncompatibleTypes

internal class IncompatibleTypesWarningImpl(
    override konst typeA: KtType,
    override konst typeB: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.IncompatibleTypesWarning

internal class TypeVarianceConflictErrorImpl(
    override konst typeParameter: KtTypeParameterSymbol,
    override konst typeParameterVariance: Variance,
    override konst variance: Variance,
    override konst containingType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.TypeVarianceConflictError

internal class TypeVarianceConflictInExpandedTypeImpl(
    override konst typeParameter: KtTypeParameterSymbol,
    override konst typeParameterVariance: Variance,
    override konst variance: Variance,
    override konst containingType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.TypeVarianceConflictInExpandedType

internal class SmartcastImpossibleImpl(
    override konst desiredType: KtType,
    override konst subject: KtExpression,
    override konst description: String,
    override konst isCastToNotNull: Boolean,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.SmartcastImpossible

internal class RedundantNullableImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.RedundantNullable

internal class PlatformClassMappedToKotlinImpl(
    override konst kotlinClass: FqName,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.PlatformClassMappedToKotlin

internal class InferredTypeVariableIntoEmptyIntersectionErrorImpl(
    override konst typeVariableDescription: String,
    override konst incompatibleTypes: List<KtType>,
    override konst description: String,
    override konst causingTypes: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.InferredTypeVariableIntoEmptyIntersectionError

internal class InferredTypeVariableIntoEmptyIntersectionWarningImpl(
    override konst typeVariableDescription: String,
    override konst incompatibleTypes: List<KtType>,
    override konst description: String,
    override konst causingTypes: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.InferredTypeVariableIntoEmptyIntersectionWarning

internal class InferredTypeVariableIntoPossibleEmptyIntersectionImpl(
    override konst typeVariableDescription: String,
    override konst incompatibleTypes: List<KtType>,
    override konst description: String,
    override konst causingTypes: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.InferredTypeVariableIntoPossibleEmptyIntersection

internal class IncorrectLeftComponentOfIntersectionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.IncorrectLeftComponentOfIntersection

internal class IncorrectRightComponentOfIntersectionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.IncorrectRightComponentOfIntersection

internal class NullableOnDefinitelyNotNullableImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.NullableOnDefinitelyNotNullable

internal class ExtensionInClassReferenceNotAllowedImpl(
    override konst referencedDeclaration: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.ExtensionInClassReferenceNotAllowed

internal class CallableReferenceLhsNotAClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.CallableReferenceLhsNotAClass

internal class CallableReferenceToAnnotationConstructorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.CallableReferenceToAnnotationConstructor

internal class ClassLiteralLhsNotAClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.ClassLiteralLhsNotAClass

internal class NullableTypeInClassLiteralLhsImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.NullableTypeInClassLiteralLhs

internal class ExpressionOfNullableTypeInClassLiteralLhsImpl(
    override konst lhsType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ExpressionOfNullableTypeInClassLiteralLhs

internal class NothingToOverrideImpl(
    override konst declaration: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.NothingToOverride

internal class CannotOverrideInvisibleMemberImpl(
    override konst overridingMember: KtCallableSymbol,
    override konst baseMember: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.CannotOverrideInvisibleMember

internal class DataClassOverrideConflictImpl(
    override konst overridingMember: KtCallableSymbol,
    override konst baseMember: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.DataClassOverrideConflict

internal class CannotWeakenAccessPrivilegeImpl(
    override konst overridingVisibility: Visibility,
    override konst overridden: KtCallableSymbol,
    override konst containingClassName: Name,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.CannotWeakenAccessPrivilege

internal class CannotChangeAccessPrivilegeImpl(
    override konst overridingVisibility: Visibility,
    override konst overridden: KtCallableSymbol,
    override konst containingClassName: Name,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.CannotChangeAccessPrivilege

internal class OverridingFinalMemberImpl(
    override konst overriddenDeclaration: KtCallableSymbol,
    override konst containingClassName: Name,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.OverridingFinalMember

internal class ReturnTypeMismatchOnInheritanceImpl(
    override konst conflictingDeclaration1: KtCallableSymbol,
    override konst conflictingDeclaration2: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.ReturnTypeMismatchOnInheritance

internal class PropertyTypeMismatchOnInheritanceImpl(
    override konst conflictingDeclaration1: KtCallableSymbol,
    override konst conflictingDeclaration2: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.PropertyTypeMismatchOnInheritance

internal class VarTypeMismatchOnInheritanceImpl(
    override konst conflictingDeclaration1: KtCallableSymbol,
    override konst conflictingDeclaration2: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.VarTypeMismatchOnInheritance

internal class ReturnTypeMismatchByDelegationImpl(
    override konst delegateDeclaration: KtCallableSymbol,
    override konst baseDeclaration: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.ReturnTypeMismatchByDelegation

internal class PropertyTypeMismatchByDelegationImpl(
    override konst delegateDeclaration: KtCallableSymbol,
    override konst baseDeclaration: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.PropertyTypeMismatchByDelegation

internal class VarOverriddenByValByDelegationImpl(
    override konst delegateDeclaration: KtCallableSymbol,
    override konst baseDeclaration: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.VarOverriddenByValByDelegation

internal class ConflictingInheritedMembersImpl(
    override konst owner: KtClassLikeSymbol,
    override konst conflictingDeclarations: List<KtCallableSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.ConflictingInheritedMembers

internal class AbstractMemberNotImplementedImpl(
    override konst classOrObject: KtClassLikeSymbol,
    override konst missingDeclaration: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.AbstractMemberNotImplemented

internal class AbstractMemberNotImplementedByEnumEntryImpl(
    override konst enumEntry: KtSymbol,
    override konst missingDeclarations: List<KtCallableSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtEnumEntry>(firDiagnostic, token), KtFirDiagnostic.AbstractMemberNotImplementedByEnumEntry

internal class AbstractClassMemberNotImplementedImpl(
    override konst classOrObject: KtClassLikeSymbol,
    override konst missingDeclaration: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.AbstractClassMemberNotImplemented

internal class InvisibleAbstractMemberFromSuperErrorImpl(
    override konst classOrObject: KtClassLikeSymbol,
    override konst invisibleDeclaration: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.InvisibleAbstractMemberFromSuperError

internal class InvisibleAbstractMemberFromSuperWarningImpl(
    override konst classOrObject: KtClassLikeSymbol,
    override konst invisibleDeclaration: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.InvisibleAbstractMemberFromSuperWarning

internal class AmbiguousAnonymousTypeInferredImpl(
    override konst superTypes: List<KtType>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.AmbiguousAnonymousTypeInferred

internal class ManyImplMemberNotImplementedImpl(
    override konst classOrObject: KtClassLikeSymbol,
    override konst missingDeclaration: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.ManyImplMemberNotImplemented

internal class ManyInterfacesMemberNotImplementedImpl(
    override konst classOrObject: KtClassLikeSymbol,
    override konst missingDeclaration: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.ManyInterfacesMemberNotImplemented

internal class OverridingFinalMemberByDelegationImpl(
    override konst delegatedDeclaration: KtCallableSymbol,
    override konst overriddenDeclaration: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.OverridingFinalMemberByDelegation

internal class DelegatedMemberHidesSupertypeOverrideImpl(
    override konst delegatedDeclaration: KtCallableSymbol,
    override konst overriddenDeclaration: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.DelegatedMemberHidesSupertypeOverride

internal class ReturnTypeMismatchOnOverrideImpl(
    override konst function: KtCallableSymbol,
    override konst superFunction: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.ReturnTypeMismatchOnOverride

internal class PropertyTypeMismatchOnOverrideImpl(
    override konst property: KtCallableSymbol,
    override konst superProperty: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.PropertyTypeMismatchOnOverride

internal class VarTypeMismatchOnOverrideImpl(
    override konst variable: KtCallableSymbol,
    override konst superVariable: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.VarTypeMismatchOnOverride

internal class VarOverriddenByValImpl(
    override konst overridingDeclaration: KtCallableSymbol,
    override konst overriddenDeclaration: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.VarOverriddenByVal

internal class VarImplementedByInheritedValErrorImpl(
    override konst classOrObject: KtClassLikeSymbol,
    override konst overridingDeclaration: KtCallableSymbol,
    override konst overriddenDeclaration: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.VarImplementedByInheritedValError

internal class VarImplementedByInheritedValWarningImpl(
    override konst classOrObject: KtClassLikeSymbol,
    override konst overridingDeclaration: KtCallableSymbol,
    override konst overriddenDeclaration: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.VarImplementedByInheritedValWarning

internal class NonFinalMemberInFinalClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.NonFinalMemberInFinalClass

internal class NonFinalMemberInObjectImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.NonFinalMemberInObject

internal class VirtualMemberHiddenImpl(
    override konst declared: KtCallableSymbol,
    override konst overriddenContainer: KtClassLikeSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.VirtualMemberHidden

internal class ManyCompanionObjectsImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtObjectDeclaration>(firDiagnostic, token), KtFirDiagnostic.ManyCompanionObjects

internal class ConflictingOverloadsImpl(
    override konst conflictingOverloads: List<KtSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ConflictingOverloads

internal class RedeclarationImpl(
    override konst conflictingDeclarations: List<KtSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.Redeclaration

internal class PackageOrClassifierRedeclarationImpl(
    override konst conflictingDeclarations: List<KtSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.PackageOrClassifierRedeclaration

internal class MethodOfAnyImplementedInInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.MethodOfAnyImplementedInInterface

internal class LocalObjectNotAllowedImpl(
    override konst objectName: Name,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.LocalObjectNotAllowed

internal class LocalInterfaceNotAllowedImpl(
    override konst interfaceName: Name,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.LocalInterfaceNotAllowed

internal class AbstractFunctionInNonAbstractClassImpl(
    override konst function: KtCallableSymbol,
    override konst containingClass: KtClassLikeSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtFunction>(firDiagnostic, token), KtFirDiagnostic.AbstractFunctionInNonAbstractClass

internal class AbstractFunctionWithBodyImpl(
    override konst function: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtFunction>(firDiagnostic, token), KtFirDiagnostic.AbstractFunctionWithBody

internal class NonAbstractFunctionWithNoBodyImpl(
    override konst function: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtFunction>(firDiagnostic, token), KtFirDiagnostic.NonAbstractFunctionWithNoBody

internal class PrivateFunctionWithNoBodyImpl(
    override konst function: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtFunction>(firDiagnostic, token), KtFirDiagnostic.PrivateFunctionWithNoBody

internal class NonMemberFunctionNoBodyImpl(
    override konst function: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtFunction>(firDiagnostic, token), KtFirDiagnostic.NonMemberFunctionNoBody

internal class FunctionDeclarationWithNoNameImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtFunction>(firDiagnostic, token), KtFirDiagnostic.FunctionDeclarationWithNoName

internal class AnonymousFunctionWithNameImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtFunction>(firDiagnostic, token), KtFirDiagnostic.AnonymousFunctionWithName

internal class AnonymousFunctionParameterWithDefaultValueImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.AnonymousFunctionParameterWithDefaultValue

internal class UselessVarargOnParameterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.UselessVarargOnParameter

internal class MultipleVarargParametersImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.MultipleVarargParameters

internal class ForbiddenVarargParameterTypeImpl(
    override konst varargParameterType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.ForbiddenVarargParameterType

internal class ValueParameterWithNoTypeAnnotationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.ValueParameterWithNoTypeAnnotation

internal class CannotInferParameterTypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.CannotInferParameterType

internal class NoTailCallsFoundImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedFunction>(firDiagnostic, token), KtFirDiagnostic.NoTailCallsFound

internal class TailrecOnVirtualMemberErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedFunction>(firDiagnostic, token), KtFirDiagnostic.TailrecOnVirtualMemberError

internal class NonTailRecursiveCallImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NonTailRecursiveCall

internal class TailRecursionInTryIsNotSupportedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.TailRecursionInTryIsNotSupported

internal class DataObjectCustomEqualsOrHashCodeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedFunction>(firDiagnostic, token), KtFirDiagnostic.DataObjectCustomEqualsOrHashCode

internal class FunInterfaceConstructorReferenceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.FunInterfaceConstructorReference

internal class FunInterfaceWrongCountOfAbstractMembersImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClass>(firDiagnostic, token), KtFirDiagnostic.FunInterfaceWrongCountOfAbstractMembers

internal class FunInterfaceCannotHaveAbstractPropertiesImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.FunInterfaceCannotHaveAbstractProperties

internal class FunInterfaceAbstractMethodWithTypeParametersImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.FunInterfaceAbstractMethodWithTypeParameters

internal class FunInterfaceAbstractMethodWithDefaultValueImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.FunInterfaceAbstractMethodWithDefaultValue

internal class FunInterfaceWithSuspendFunctionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.FunInterfaceWithSuspendFunction

internal class AbstractPropertyInNonAbstractClassImpl(
    override konst property: KtCallableSymbol,
    override konst containingClass: KtClassLikeSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.AbstractPropertyInNonAbstractClass

internal class PrivatePropertyInInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.PrivatePropertyInInterface

internal class AbstractPropertyWithInitializerImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.AbstractPropertyWithInitializer

internal class PropertyInitializerInInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.PropertyInitializerInInterface

internal class PropertyWithNoTypeNoInitializerImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.PropertyWithNoTypeNoInitializer

internal class MustBeInitializedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.MustBeInitialized

internal class MustBeInitializedWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.MustBeInitializedWarning

internal class MustBeInitializedOrBeFinalImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.MustBeInitializedOrBeFinal

internal class MustBeInitializedOrBeFinalWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.MustBeInitializedOrBeFinalWarning

internal class MustBeInitializedOrBeAbstractImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.MustBeInitializedOrBeAbstract

internal class MustBeInitializedOrBeAbstractWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.MustBeInitializedOrBeAbstractWarning

internal class MustBeInitializedOrFinalOrAbstractImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.MustBeInitializedOrFinalOrAbstract

internal class MustBeInitializedOrFinalOrAbstractWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.MustBeInitializedOrFinalOrAbstractWarning

internal class ExtensionPropertyMustHaveAccessorsOrBeAbstractImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.ExtensionPropertyMustHaveAccessorsOrBeAbstract

internal class UnnecessaryLateinitImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.UnnecessaryLateinit

internal class BackingFieldInInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.BackingFieldInInterface

internal class ExtensionPropertyWithBackingFieldImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.ExtensionPropertyWithBackingField

internal class PropertyInitializerNoBackingFieldImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.PropertyInitializerNoBackingField

internal class AbstractDelegatedPropertyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.AbstractDelegatedProperty

internal class DelegatedPropertyInInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.DelegatedPropertyInInterface

internal class AbstractPropertyWithGetterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtPropertyAccessor>(firDiagnostic, token), KtFirDiagnostic.AbstractPropertyWithGetter

internal class AbstractPropertyWithSetterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtPropertyAccessor>(firDiagnostic, token), KtFirDiagnostic.AbstractPropertyWithSetter

internal class PrivateSetterForAbstractPropertyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.PrivateSetterForAbstractProperty

internal class PrivateSetterForOpenPropertyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.PrivateSetterForOpenProperty

internal class ValWithSetterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtPropertyAccessor>(firDiagnostic, token), KtFirDiagnostic.ValWithSetter

internal class ConstValNotTopLevelOrObjectImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ConstValNotTopLevelOrObject

internal class ConstValWithGetterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ConstValWithGetter

internal class ConstValWithDelegateImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.ConstValWithDelegate

internal class TypeCantBeUsedForConstValImpl(
    override konst constValType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.TypeCantBeUsedForConstVal

internal class ConstValWithoutInitializerImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.ConstValWithoutInitializer

internal class ConstValWithNonConstInitializerImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.ConstValWithNonConstInitializer

internal class WrongSetterParameterTypeImpl(
    override konst expectedType: KtType,
    override konst actualType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.WrongSetterParameterType

internal class DelegateUsesExtensionPropertyTypeParameterErrorImpl(
    override konst usedTypeParameter: KtTypeParameterSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.DelegateUsesExtensionPropertyTypeParameterError

internal class DelegateUsesExtensionPropertyTypeParameterWarningImpl(
    override konst usedTypeParameter: KtTypeParameterSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.DelegateUsesExtensionPropertyTypeParameterWarning

internal class InitializerTypeMismatchImpl(
    override konst expectedType: KtType,
    override konst actualType: KtType,
    override konst isMismatchDueToNullability: Boolean,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.InitializerTypeMismatch

internal class GetterVisibilityDiffersFromPropertyVisibilityImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.GetterVisibilityDiffersFromPropertyVisibility

internal class SetterVisibilityInconsistentWithPropertyVisibilityImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.SetterVisibilityInconsistentWithPropertyVisibility

internal class WrongSetterReturnTypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.WrongSetterReturnType

internal class WrongGetterReturnTypeImpl(
    override konst expectedType: KtType,
    override konst actualType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.WrongGetterReturnType

internal class AccessorForDelegatedPropertyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtPropertyAccessor>(firDiagnostic, token), KtFirDiagnostic.AccessorForDelegatedProperty

internal class PropertyInitializerWithExplicitFieldDeclarationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.PropertyInitializerWithExplicitFieldDeclaration

internal class PropertyFieldDeclarationMissingInitializerImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBackingField>(firDiagnostic, token), KtFirDiagnostic.PropertyFieldDeclarationMissingInitializer

internal class LateinitPropertyFieldDeclarationWithInitializerImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBackingField>(firDiagnostic, token), KtFirDiagnostic.LateinitPropertyFieldDeclarationWithInitializer

internal class LateinitFieldInValPropertyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBackingField>(firDiagnostic, token), KtFirDiagnostic.LateinitFieldInValProperty

internal class LateinitNullableBackingFieldImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBackingField>(firDiagnostic, token), KtFirDiagnostic.LateinitNullableBackingField

internal class BackingFieldForDelegatedPropertyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBackingField>(firDiagnostic, token), KtFirDiagnostic.BackingFieldForDelegatedProperty

internal class PropertyMustHaveGetterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.PropertyMustHaveGetter

internal class PropertyMustHaveSetterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.PropertyMustHaveSetter

internal class ExplicitBackingFieldInInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBackingField>(firDiagnostic, token), KtFirDiagnostic.ExplicitBackingFieldInInterface

internal class ExplicitBackingFieldInAbstractPropertyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBackingField>(firDiagnostic, token), KtFirDiagnostic.ExplicitBackingFieldInAbstractProperty

internal class ExplicitBackingFieldInExtensionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBackingField>(firDiagnostic, token), KtFirDiagnostic.ExplicitBackingFieldInExtension

internal class RedundantExplicitBackingFieldImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBackingField>(firDiagnostic, token), KtFirDiagnostic.RedundantExplicitBackingField

internal class AbstractPropertyInPrimaryConstructorParametersImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.AbstractPropertyInPrimaryConstructorParameters

internal class LocalVariableWithTypeParametersWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.LocalVariableWithTypeParametersWarning

internal class LocalVariableWithTypeParametersImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtProperty>(firDiagnostic, token), KtFirDiagnostic.LocalVariableWithTypeParameters

internal class ExplicitTypeArgumentsInPropertyAccessImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.ExplicitTypeArgumentsInPropertyAccess

internal class LateinitIntrinsicCallOnNonLiteralImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.LateinitIntrinsicCallOnNonLiteral

internal class LateinitIntrinsicCallOnNonLateinitImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.LateinitIntrinsicCallOnNonLateinit

internal class LateinitIntrinsicCallInInlineFunctionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.LateinitIntrinsicCallInInlineFunction

internal class LateinitIntrinsicCallOnNonAccessiblePropertyImpl(
    override konst declaration: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.LateinitIntrinsicCallOnNonAccessibleProperty

internal class LocalExtensionPropertyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.LocalExtensionProperty

internal class ExpectedDeclarationWithBodyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.ExpectedDeclarationWithBody

internal class ExpectedClassConstructorDelegationCallImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtConstructorDelegationCall>(firDiagnostic, token), KtFirDiagnostic.ExpectedClassConstructorDelegationCall

internal class ExpectedClassConstructorPropertyParameterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.ExpectedClassConstructorPropertyParameter

internal class ExpectedEnumConstructorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtConstructor<*>>(firDiagnostic, token), KtFirDiagnostic.ExpectedEnumConstructor

internal class ExpectedEnumEntryWithBodyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtEnumEntry>(firDiagnostic, token), KtFirDiagnostic.ExpectedEnumEntryWithBody

internal class ExpectedPropertyInitializerImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.ExpectedPropertyInitializer

internal class ExpectedDelegatedPropertyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.ExpectedDelegatedProperty

internal class ExpectedLateinitPropertyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.ExpectedLateinitProperty

internal class SupertypeInitializedInExpectedClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.SupertypeInitializedInExpectedClass

internal class ExpectedPrivateDeclarationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.ExpectedPrivateDeclaration

internal class ExpectedExternalDeclarationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.ExpectedExternalDeclaration

internal class ExpectedTailrecFunctionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.ExpectedTailrecFunction

internal class ImplementationByDelegationInExpectClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDelegatedSuperTypeEntry>(firDiagnostic, token), KtFirDiagnostic.ImplementationByDelegationInExpectClass

internal class ActualTypeAliasNotToClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeAlias>(firDiagnostic, token), KtFirDiagnostic.ActualTypeAliasNotToClass

internal class ActualTypeAliasToClassWithDeclarationSiteVarianceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeAlias>(firDiagnostic, token), KtFirDiagnostic.ActualTypeAliasToClassWithDeclarationSiteVariance

internal class ActualTypeAliasWithUseSiteVarianceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeAlias>(firDiagnostic, token), KtFirDiagnostic.ActualTypeAliasWithUseSiteVariance

internal class ActualTypeAliasWithComplexSubstitutionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeAlias>(firDiagnostic, token), KtFirDiagnostic.ActualTypeAliasWithComplexSubstitution

internal class ActualFunctionWithDefaultArgumentsImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ActualFunctionWithDefaultArguments

internal class DefaultArgumentsInExpectWithActualTypealiasImpl(
    override konst expectClassSymbol: KtClassLikeSymbol,
    override konst members: List<KtCallableSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeAlias>(firDiagnostic, token), KtFirDiagnostic.DefaultArgumentsInExpectWithActualTypealias

internal class ActualAnnotationConflictingDefaultArgumentValueImpl(
    override konst parameter: KtVariableLikeSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ActualAnnotationConflictingDefaultArgumentValue

internal class ExpectedFunctionSourceWithDefaultArgumentsNotFoundImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ExpectedFunctionSourceWithDefaultArgumentsNotFound

internal class NoActualForExpectImpl(
    override konst declaration: KtSymbol,
    override konst module: FirModuleData,
    override konst compatibility: Map<ExpectActualCompatibility<FirBasedSymbol<*>>, List<KtSymbol>>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.NoActualForExpect

internal class ActualWithoutExpectImpl(
    override konst declaration: KtSymbol,
    override konst compatibility: Map<ExpectActualCompatibility<FirBasedSymbol<*>>, List<KtSymbol>>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.ActualWithoutExpect

internal class AmbiguousActualsImpl(
    override konst declaration: KtSymbol,
    override konst candidates: List<KtSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.AmbiguousActuals

internal class AmbiguousExpectsImpl(
    override konst declaration: KtSymbol,
    override konst modules: List<FirModuleData>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.AmbiguousExpects

internal class NoActualClassMemberForExpectedClassImpl(
    override konst declaration: KtSymbol,
    override konst members: List<Pair<KtSymbol, Map<Incompatible<FirBasedSymbol<*>>, List<KtSymbol>>>>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.NoActualClassMemberForExpectedClass

internal class ActualMissingImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.ActualMissing

internal class InitializerRequiredForDestructuringDeclarationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDestructuringDeclaration>(firDiagnostic, token), KtFirDiagnostic.InitializerRequiredForDestructuringDeclaration

internal class ComponentFunctionMissingImpl(
    override konst missingFunctionName: Name,
    override konst destructingType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ComponentFunctionMissing

internal class ComponentFunctionAmbiguityImpl(
    override konst functionWithAmbiguityName: Name,
    override konst candidates: List<KtSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ComponentFunctionAmbiguity

internal class ComponentFunctionOnNullableImpl(
    override konst componentFunctionName: Name,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.ComponentFunctionOnNullable

internal class ComponentFunctionReturnTypeMismatchImpl(
    override konst componentFunctionName: Name,
    override konst destructingType: KtType,
    override konst expectedType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.ComponentFunctionReturnTypeMismatch

internal class UninitializedVariableImpl(
    override konst variable: KtVariableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.UninitializedVariable

internal class UninitializedParameterImpl(
    override konst parameter: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtSimpleNameExpression>(firDiagnostic, token), KtFirDiagnostic.UninitializedParameter

internal class UninitializedEnumEntryImpl(
    override konst enumEntry: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.UninitializedEnumEntry

internal class UninitializedEnumCompanionImpl(
    override konst enumClass: KtClassLikeSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.UninitializedEnumCompanion

internal class ValReassignmentImpl(
    override konst variable: KtVariableLikeSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.ValReassignment

internal class ValReassignmentViaBackingFieldErrorImpl(
    override konst property: KtVariableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.ValReassignmentViaBackingFieldError

internal class ValReassignmentViaBackingFieldWarningImpl(
    override konst property: KtVariableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.ValReassignmentViaBackingFieldWarning

internal class CapturedValInitializationImpl(
    override konst property: KtVariableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.CapturedValInitialization

internal class CapturedMemberValInitializationImpl(
    override konst property: KtVariableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.CapturedMemberValInitialization

internal class SetterProjectedOutImpl(
    override konst property: KtVariableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBinaryExpression>(firDiagnostic, token), KtFirDiagnostic.SetterProjectedOut

internal class WrongInvocationKindImpl(
    override konst declaration: KtSymbol,
    override konst requiredRange: EventOccurrencesRange,
    override konst actualRange: EventOccurrencesRange,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.WrongInvocationKind

internal class LeakedInPlaceLambdaImpl(
    override konst lambda: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.LeakedInPlaceLambda

internal class WrongImpliesConditionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.WrongImpliesCondition

internal class VariableWithNoTypeNoInitializerImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtVariableDeclaration>(firDiagnostic, token), KtFirDiagnostic.VariableWithNoTypeNoInitializer

internal class InitializationBeforeDeclarationImpl(
    override konst property: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.InitializationBeforeDeclaration

internal class UnreachableCodeImpl(
    override konst reachable: List<PsiElement>,
    override konst unreachable: List<PsiElement>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.UnreachableCode

internal class SenselessComparisonImpl(
    override konst expression: KtExpression,
    override konst compareResult: Boolean,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.SenselessComparison

internal class SenselessNullInWhenImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.SenselessNullInWhen

internal class TypecheckerHasRunIntoRecursiveProblemImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.TypecheckerHasRunIntoRecursiveProblem

internal class UnsafeCallImpl(
    override konst receiverType: KtType,
    override konst receiverExpression: KtExpression?,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.UnsafeCall

internal class UnsafeImplicitInvokeCallImpl(
    override konst receiverType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.UnsafeImplicitInvokeCall

internal class UnsafeInfixCallImpl(
    override konst receiverExpression: KtExpression,
    override konst operator: String,
    override konst argumentExpression: KtExpression,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.UnsafeInfixCall

internal class UnsafeOperatorCallImpl(
    override konst receiverExpression: KtExpression,
    override konst operator: String,
    override konst argumentExpression: KtExpression,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.UnsafeOperatorCall

internal class IteratorOnNullableImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.IteratorOnNullable

internal class UnnecessarySafeCallImpl(
    override konst receiverType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.UnnecessarySafeCall

internal class SafeCallWillChangeNullabilityImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtSafeQualifiedExpression>(firDiagnostic, token), KtFirDiagnostic.SafeCallWillChangeNullability

internal class UnexpectedSafeCallImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.UnexpectedSafeCall

internal class UnnecessaryNotNullAssertionImpl(
    override konst receiverType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.UnnecessaryNotNullAssertion

internal class NotNullAssertionOnLambdaExpressionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.NotNullAssertionOnLambdaExpression

internal class NotNullAssertionOnCallableReferenceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.NotNullAssertionOnCallableReference

internal class UselessElvisImpl(
    override konst receiverType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBinaryExpression>(firDiagnostic, token), KtFirDiagnostic.UselessElvis

internal class UselessElvisRightIsNullImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBinaryExpression>(firDiagnostic, token), KtFirDiagnostic.UselessElvisRightIsNull

internal class CannotCheckForErasedImpl(
    override konst type: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.CannotCheckForErased

internal class CastNeverSucceedsImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBinaryExpressionWithTypeRHS>(firDiagnostic, token), KtFirDiagnostic.CastNeverSucceeds

internal class UselessCastImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBinaryExpressionWithTypeRHS>(firDiagnostic, token), KtFirDiagnostic.UselessCast

internal class UncheckedCastImpl(
    override konst originalType: KtType,
    override konst targetType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBinaryExpressionWithTypeRHS>(firDiagnostic, token), KtFirDiagnostic.UncheckedCast

internal class UselessIsCheckImpl(
    override konst compileTimeCheckResult: Boolean,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.UselessIsCheck

internal class IsEnumEntryImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.IsEnumEntry

internal class EnumEntryAsTypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.EnumEntryAsType

internal class ExpectedConditionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtWhenCondition>(firDiagnostic, token), KtFirDiagnostic.ExpectedCondition

internal class NoElseInWhenImpl(
    override konst missingWhenCases: List<WhenMissingCase>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtWhenExpression>(firDiagnostic, token), KtFirDiagnostic.NoElseInWhen

internal class NonExhaustiveWhenStatementImpl(
    override konst type: String,
    override konst missingWhenCases: List<WhenMissingCase>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtWhenExpression>(firDiagnostic, token), KtFirDiagnostic.NonExhaustiveWhenStatement

internal class InkonstidIfAsExpressionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtIfExpression>(firDiagnostic, token), KtFirDiagnostic.InkonstidIfAsExpression

internal class ElseMisplacedInWhenImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtWhenEntry>(firDiagnostic, token), KtFirDiagnostic.ElseMisplacedInWhen

internal class IllegalDeclarationInWhenSubjectImpl(
    override konst illegalReason: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.IllegalDeclarationInWhenSubject

internal class CommaInWhenConditionWithoutArgumentImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.CommaInWhenConditionWithoutArgument

internal class DuplicateLabelInWhenImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.DuplicateLabelInWhen

internal class ConfusingBranchConditionErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ConfusingBranchConditionError

internal class ConfusingBranchConditionWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ConfusingBranchConditionWarning

internal class TypeParameterIsNotAnExpressionImpl(
    override konst typeParameter: KtTypeParameterSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtSimpleNameExpression>(firDiagnostic, token), KtFirDiagnostic.TypeParameterIsNotAnExpression

internal class TypeParameterOnLhsOfDotImpl(
    override konst typeParameter: KtTypeParameterSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtSimpleNameExpression>(firDiagnostic, token), KtFirDiagnostic.TypeParameterOnLhsOfDot

internal class NoCompanionObjectImpl(
    override konst klass: KtClassLikeSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.NoCompanionObject

internal class ExpressionExpectedPackageFoundImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.ExpressionExpectedPackageFound

internal class ErrorInContractDescriptionImpl(
    override konst reason: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ErrorInContractDescription

internal class ContractNotAllowedImpl(
    override konst reason: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ContractNotAllowed

internal class NoGetMethodImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtArrayAccessExpression>(firDiagnostic, token), KtFirDiagnostic.NoGetMethod

internal class NoSetMethodImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtArrayAccessExpression>(firDiagnostic, token), KtFirDiagnostic.NoSetMethod

internal class IteratorMissingImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.IteratorMissing

internal class HasNextMissingImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.HasNextMissing

internal class NextMissingImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.NextMissing

internal class HasNextFunctionNoneApplicableImpl(
    override konst candidates: List<KtSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.HasNextFunctionNoneApplicable

internal class NextNoneApplicableImpl(
    override konst candidates: List<KtSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.NextNoneApplicable

internal class DelegateSpecialFunctionMissingImpl(
    override konst expectedFunctionSignature: String,
    override konst delegateType: KtType,
    override konst description: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.DelegateSpecialFunctionMissing

internal class DelegateSpecialFunctionAmbiguityImpl(
    override konst expectedFunctionSignature: String,
    override konst candidates: List<KtSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.DelegateSpecialFunctionAmbiguity

internal class DelegateSpecialFunctionNoneApplicableImpl(
    override konst expectedFunctionSignature: String,
    override konst candidates: List<KtSymbol>,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.DelegateSpecialFunctionNoneApplicable

internal class DelegateSpecialFunctionReturnTypeMismatchImpl(
    override konst delegateFunction: String,
    override konst expectedType: KtType,
    override konst actualType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.DelegateSpecialFunctionReturnTypeMismatch

internal class UnderscoreIsReservedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.UnderscoreIsReserved

internal class UnderscoreUsageWithoutBackticksImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.UnderscoreUsageWithoutBackticks

internal class ResolvedToUnderscoreNamedCatchParameterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNameReferenceExpression>(firDiagnostic, token), KtFirDiagnostic.ResolvedToUnderscoreNamedCatchParameter

internal class InkonstidCharactersImpl(
    override konst message: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.InkonstidCharacters

internal class DangerousCharactersImpl(
    override konst characters: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.DangerousCharacters

internal class EqualityNotApplicableImpl(
    override konst operator: String,
    override konst leftType: KtType,
    override konst rightType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBinaryExpression>(firDiagnostic, token), KtFirDiagnostic.EqualityNotApplicable

internal class EqualityNotApplicableWarningImpl(
    override konst operator: String,
    override konst leftType: KtType,
    override konst rightType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtBinaryExpression>(firDiagnostic, token), KtFirDiagnostic.EqualityNotApplicableWarning

internal class IncompatibleEnumComparisonErrorImpl(
    override konst leftType: KtType,
    override konst rightType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.IncompatibleEnumComparisonError

internal class IncompatibleEnumComparisonImpl(
    override konst leftType: KtType,
    override konst rightType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.IncompatibleEnumComparison

internal class ForbiddenIdentityEqualsImpl(
    override konst leftType: KtType,
    override konst rightType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ForbiddenIdentityEquals

internal class ForbiddenIdentityEqualsWarningImpl(
    override konst leftType: KtType,
    override konst rightType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ForbiddenIdentityEqualsWarning

internal class DeprecatedIdentityEqualsImpl(
    override konst leftType: KtType,
    override konst rightType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.DeprecatedIdentityEquals

internal class ImplicitBoxingInIdentityEqualsImpl(
    override konst leftType: KtType,
    override konst rightType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ImplicitBoxingInIdentityEquals

internal class IncDecShouldNotReturnUnitImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.IncDecShouldNotReturnUnit

internal class AssignmentOperatorShouldReturnUnitImpl(
    override konst functionSymbol: KtFunctionLikeSymbol,
    override konst operator: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.AssignmentOperatorShouldReturnUnit

internal class PropertyAsOperatorImpl(
    override konst property: KtVariableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.PropertyAsOperator

internal class DslScopeViolationImpl(
    override konst calleeSymbol: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DslScopeViolation

internal class ToplevelTypealiasesOnlyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeAlias>(firDiagnostic, token), KtFirDiagnostic.ToplevelTypealiasesOnly

internal class RecursiveTypealiasExpansionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.RecursiveTypealiasExpansion

internal class TypealiasShouldExpandToClassImpl(
    override konst expandedType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.TypealiasShouldExpandToClass

internal class RedundantVisibilityModifierImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.RedundantVisibilityModifier

internal class RedundantModalityModifierImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtModifierListOwner>(firDiagnostic, token), KtFirDiagnostic.RedundantModalityModifier

internal class RedundantReturnUnitTypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtTypeReference>(firDiagnostic, token), KtFirDiagnostic.RedundantReturnUnitType

internal class RedundantExplicitTypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.RedundantExplicitType

internal class RedundantSingleExpressionStringTemplateImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.RedundantSingleExpressionStringTemplate

internal class CanBeValImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.CanBeVal

internal class CanBeReplacedWithOperatorAssignmentImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.CanBeReplacedWithOperatorAssignment

internal class RedundantCallOfConversionMethodImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.RedundantCallOfConversionMethod

internal class ArrayEqualityOperatorCanBeReplacedWithEqualsImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.ArrayEqualityOperatorCanBeReplacedWithEquals

internal class EmptyRangeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.EmptyRange

internal class RedundantSetterParameterTypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.RedundantSetterParameterType

internal class UnusedVariableImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.UnusedVariable

internal class AssignedValueIsNeverReadImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.AssignedValueIsNeverRead

internal class VariableInitializerIsRedundantImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.VariableInitializerIsRedundant

internal class VariableNeverReadImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedDeclaration>(firDiagnostic, token), KtFirDiagnostic.VariableNeverRead

internal class UselessCallOnNotNullImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.UselessCallOnNotNull

internal class ReturnNotAllowedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtReturnExpression>(firDiagnostic, token), KtFirDiagnostic.ReturnNotAllowed

internal class NotAFunctionLabelImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtReturnExpression>(firDiagnostic, token), KtFirDiagnostic.NotAFunctionLabel

internal class ReturnInFunctionWithExpressionBodyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtReturnExpression>(firDiagnostic, token), KtFirDiagnostic.ReturnInFunctionWithExpressionBody

internal class NoReturnInFunctionWithBlockBodyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclarationWithBody>(firDiagnostic, token), KtFirDiagnostic.NoReturnInFunctionWithBlockBody

internal class AnonymousInitializerInInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnonymousInitializer>(firDiagnostic, token), KtFirDiagnostic.AnonymousInitializerInInterface

internal class UsageIsNotInlinableImpl(
    override konst parameter: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.UsageIsNotInlinable

internal class NonLocalReturnNotAllowedImpl(
    override konst parameter: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.NonLocalReturnNotAllowed

internal class NotYetSupportedInInlineImpl(
    override konst message: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.NotYetSupportedInInline

internal class NothingToInlineImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.NothingToInline

internal class NullableInlineParameterImpl(
    override konst parameter: KtSymbol,
    override konst function: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.NullableInlineParameter

internal class RecursionInInlineImpl(
    override konst symbol: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.RecursionInInline

internal class NonPublicCallFromPublicInlineImpl(
    override konst inlineDeclaration: KtSymbol,
    override konst referencedDeclaration: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.NonPublicCallFromPublicInline

internal class ProtectedConstructorCallFromPublicInlineImpl(
    override konst inlineDeclaration: KtSymbol,
    override konst referencedDeclaration: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ProtectedConstructorCallFromPublicInline

internal class ProtectedCallFromPublicInlineErrorImpl(
    override konst inlineDeclaration: KtSymbol,
    override konst referencedDeclaration: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ProtectedCallFromPublicInlineError

internal class ProtectedCallFromPublicInlineImpl(
    override konst inlineDeclaration: KtSymbol,
    override konst referencedDeclaration: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ProtectedCallFromPublicInline

internal class PrivateClassMemberFromInlineImpl(
    override konst inlineDeclaration: KtSymbol,
    override konst referencedDeclaration: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.PrivateClassMemberFromInline

internal class SuperCallFromPublicInlineImpl(
    override konst symbol: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.SuperCallFromPublicInline

internal class DeclarationCantBeInlinedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.DeclarationCantBeInlined

internal class OverrideByInlineImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.OverrideByInline

internal class NonInternalPublishedApiImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.NonInternalPublishedApi

internal class InkonstidDefaultFunctionalParameterForInlineImpl(
    override konst defaultValue: KtExpression,
    override konst parameter: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.InkonstidDefaultFunctionalParameterForInline

internal class ReifiedTypeParameterInOverrideImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ReifiedTypeParameterInOverride

internal class InlinePropertyWithBackingFieldImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.InlinePropertyWithBackingField

internal class IllegalInlineParameterModifierImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.IllegalInlineParameterModifier

internal class InlineSuspendFunctionTypeUnsupportedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.InlineSuspendFunctionTypeUnsupported

internal class InefficientEqualsOverridingInValueClassImpl(
    override konst type: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtNamedFunction>(firDiagnostic, token), KtFirDiagnostic.InefficientEqualsOverridingInValueClass

internal class CannotAllUnderImportFromSingletonImpl(
    override konst objectName: Name,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtImportDirective>(firDiagnostic, token), KtFirDiagnostic.CannotAllUnderImportFromSingleton

internal class PackageCannotBeImportedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtImportDirective>(firDiagnostic, token), KtFirDiagnostic.PackageCannotBeImported

internal class CannotBeImportedImpl(
    override konst name: Name,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtImportDirective>(firDiagnostic, token), KtFirDiagnostic.CannotBeImported

internal class ConflictingImportImpl(
    override konst name: Name,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtImportDirective>(firDiagnostic, token), KtFirDiagnostic.ConflictingImport

internal class OperatorRenamedOnImportImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtImportDirective>(firDiagnostic, token), KtFirDiagnostic.OperatorRenamedOnImport

internal class IllegalSuspendFunctionCallImpl(
    override konst suspendCallable: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.IllegalSuspendFunctionCall

internal class IllegalSuspendPropertyAccessImpl(
    override konst suspendCallable: KtSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.IllegalSuspendPropertyAccess

internal class NonLocalSuspensionPointImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NonLocalSuspensionPoint

internal class IllegalRestrictedSuspendingFunctionCallImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.IllegalRestrictedSuspendingFunctionCall

internal class NonModifierFormForBuiltInSuspendImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NonModifierFormForBuiltInSuspend

internal class ModifierFormForNonBuiltInSuspendImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ModifierFormForNonBuiltInSuspend

internal class ModifierFormForNonBuiltInSuspendFunErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ModifierFormForNonBuiltInSuspendFunError

internal class ModifierFormForNonBuiltInSuspendFunWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ModifierFormForNonBuiltInSuspendFunWarning

internal class ReturnForBuiltInSuspendImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtReturnExpression>(firDiagnostic, token), KtFirDiagnostic.ReturnForBuiltInSuspend

internal class RedundantLabelWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtLabelReferenceExpression>(firDiagnostic, token), KtFirDiagnostic.RedundantLabelWarning

internal class ConflictingJvmDeclarationsImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ConflictingJvmDeclarations

internal class OverrideCannotBeStaticImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.OverrideCannotBeStatic

internal class JvmStaticNotInObjectOrClassCompanionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.JvmStaticNotInObjectOrClassCompanion

internal class JvmStaticNotInObjectOrCompanionImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.JvmStaticNotInObjectOrCompanion

internal class JvmStaticOnNonPublicMemberImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.JvmStaticOnNonPublicMember

internal class JvmStaticOnConstOrJvmFieldImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.JvmStaticOnConstOrJvmField

internal class JvmStaticOnExternalInInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.JvmStaticOnExternalInInterface

internal class InapplicableJvmNameImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.InapplicableJvmName

internal class IllegalJvmNameImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.IllegalJvmName

internal class FunctionDelegateMemberNameClashImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.FunctionDelegateMemberNameClash

internal class ValueClassWithoutJvmInlineAnnotationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ValueClassWithoutJvmInlineAnnotation

internal class JvmInlineWithoutValueClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.JvmInlineWithoutValueClass

internal class JavaTypeMismatchImpl(
    override konst expectedType: KtType,
    override konst actualType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.JavaTypeMismatch

internal class UpperBoundCannotBeArrayImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.UpperBoundCannotBeArray

internal class StrictfpOnClassImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.StrictfpOnClass

internal class SynchronizedOnAbstractImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.SynchronizedOnAbstract

internal class SynchronizedInInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.SynchronizedInInterface

internal class SynchronizedOnInlineImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.SynchronizedOnInline

internal class SynchronizedOnSuspendErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.SynchronizedOnSuspendError

internal class SynchronizedOnSuspendWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.SynchronizedOnSuspendWarning

internal class OverloadsWithoutDefaultArgumentsImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.OverloadsWithoutDefaultArguments

internal class OverloadsAbstractImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.OverloadsAbstract

internal class OverloadsInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.OverloadsInterface

internal class OverloadsLocalImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.OverloadsLocal

internal class OverloadsAnnotationClassConstructorErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.OverloadsAnnotationClassConstructorError

internal class OverloadsAnnotationClassConstructorWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.OverloadsAnnotationClassConstructorWarning

internal class OverloadsPrivateImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.OverloadsPrivate

internal class DeprecatedJavaAnnotationImpl(
    override konst kotlinName: FqName,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.DeprecatedJavaAnnotation

internal class JvmPackageNameCannotBeEmptyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.JvmPackageNameCannotBeEmpty

internal class JvmPackageNameMustBeValidNameImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.JvmPackageNameMustBeValidName

internal class JvmPackageNameNotSupportedInFilesWithClassesImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.JvmPackageNameNotSupportedInFilesWithClasses

internal class PositionedValueArgumentForJavaAnnotationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.PositionedValueArgumentForJavaAnnotation

internal class RedundantRepeatableAnnotationImpl(
    override konst kotlinRepeatable: FqName,
    override konst javaRepeatable: FqName,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.RedundantRepeatableAnnotation

internal class LocalJvmRecordImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.LocalJvmRecord

internal class NonFinalJvmRecordImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NonFinalJvmRecord

internal class EnumJvmRecordImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.EnumJvmRecord

internal class JvmRecordWithoutPrimaryConstructorParametersImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.JvmRecordWithoutPrimaryConstructorParameters

internal class NonDataClassJvmRecordImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.NonDataClassJvmRecord

internal class JvmRecordNotValParameterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.JvmRecordNotValParameter

internal class JvmRecordNotLastVarargParameterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.JvmRecordNotLastVarargParameter

internal class InnerJvmRecordImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.InnerJvmRecord

internal class FieldInJvmRecordImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.FieldInJvmRecord

internal class DelegationByInJvmRecordImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.DelegationByInJvmRecord

internal class JvmRecordExtendsClassImpl(
    override konst superType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.JvmRecordExtendsClass

internal class IllegalJavaLangRecordSupertypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.IllegalJavaLangRecordSupertype

internal class JvmDefaultInDeclarationImpl(
    override konst annotation: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.JvmDefaultInDeclaration

internal class JvmDefaultWithCompatibilityInDeclarationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.JvmDefaultWithCompatibilityInDeclaration

internal class JvmDefaultWithCompatibilityNotOnInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.JvmDefaultWithCompatibilityNotOnInterface

internal class ExternalDeclarationCannotBeAbstractImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.ExternalDeclarationCannotBeAbstract

internal class ExternalDeclarationCannotHaveBodyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.ExternalDeclarationCannotHaveBody

internal class ExternalDeclarationInInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.ExternalDeclarationInInterface

internal class ExternalDeclarationCannotBeInlinedImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.ExternalDeclarationCannotBeInlined

internal class NonSourceRepeatedAnnotationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.NonSourceRepeatedAnnotation

internal class RepeatedAnnotationWithContainerImpl(
    override konst name: ClassId,
    override konst explicitContainerName: ClassId,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.RepeatedAnnotationWithContainer

internal class RepeatableContainerMustHaveValueArrayErrorImpl(
    override konst container: ClassId,
    override konst annotation: ClassId,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.RepeatableContainerMustHaveValueArrayError

internal class RepeatableContainerMustHaveValueArrayWarningImpl(
    override konst container: ClassId,
    override konst annotation: ClassId,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.RepeatableContainerMustHaveValueArrayWarning

internal class RepeatableContainerHasNonDefaultParameterErrorImpl(
    override konst container: ClassId,
    override konst nonDefault: Name,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.RepeatableContainerHasNonDefaultParameterError

internal class RepeatableContainerHasNonDefaultParameterWarningImpl(
    override konst container: ClassId,
    override konst nonDefault: Name,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.RepeatableContainerHasNonDefaultParameterWarning

internal class RepeatableContainerHasShorterRetentionErrorImpl(
    override konst container: ClassId,
    override konst retention: String,
    override konst annotation: ClassId,
    override konst annotationRetention: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.RepeatableContainerHasShorterRetentionError

internal class RepeatableContainerHasShorterRetentionWarningImpl(
    override konst container: ClassId,
    override konst retention: String,
    override konst annotation: ClassId,
    override konst annotationRetention: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.RepeatableContainerHasShorterRetentionWarning

internal class RepeatableContainerTargetSetNotASubsetErrorImpl(
    override konst container: ClassId,
    override konst annotation: ClassId,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.RepeatableContainerTargetSetNotASubsetError

internal class RepeatableContainerTargetSetNotASubsetWarningImpl(
    override konst container: ClassId,
    override konst annotation: ClassId,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.RepeatableContainerTargetSetNotASubsetWarning

internal class RepeatableAnnotationHasNestedClassNamedContainerErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.RepeatableAnnotationHasNestedClassNamedContainerError

internal class RepeatableAnnotationHasNestedClassNamedContainerWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.RepeatableAnnotationHasNestedClassNamedContainerWarning

internal class SuspensionPointInsideCriticalSectionImpl(
    override konst function: KtCallableSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.SuspensionPointInsideCriticalSection

internal class InapplicableJvmFieldImpl(
    override konst message: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.InapplicableJvmField

internal class InapplicableJvmFieldWarningImpl(
    override konst message: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.InapplicableJvmFieldWarning

internal class JvmSyntheticOnDelegateImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnnotationEntry>(firDiagnostic, token), KtFirDiagnostic.JvmSyntheticOnDelegate

internal class SubclassCantCallCompanionProtectedNonStaticImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.SubclassCantCallCompanionProtectedNonStatic

internal class ConcurrentHashMapContainsOperatorErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ConcurrentHashMapContainsOperatorError

internal class ConcurrentHashMapContainsOperatorWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.ConcurrentHashMapContainsOperatorWarning

internal class SpreadOnSignaturePolymorphicCallErrorImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.SpreadOnSignaturePolymorphicCallError

internal class SpreadOnSignaturePolymorphicCallWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.SpreadOnSignaturePolymorphicCallWarning

internal class JavaSamInterfaceConstructorReferenceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.JavaSamInterfaceConstructorReference

internal class ImplementingFunctionInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtClassOrObject>(firDiagnostic, token), KtFirDiagnostic.ImplementingFunctionInterface

internal class OverridingExternalFunWithOptionalParamsImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.OverridingExternalFunWithOptionalParams

internal class OverridingExternalFunWithOptionalParamsWithFakeImpl(
    override konst function: KtFunctionLikeSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.OverridingExternalFunWithOptionalParamsWithFake

internal class CallToDefinedExternallyFromNonExternalDeclarationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.CallToDefinedExternallyFromNonExternalDeclaration

internal class ExternalClassConstructorPropertyParameterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtParameter>(firDiagnostic, token), KtFirDiagnostic.ExternalClassConstructorPropertyParameter

internal class ExternalEnumEntryWithBodyImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ExternalEnumEntryWithBody

internal class ExternalAnonymousInitializerImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtAnonymousInitializer>(firDiagnostic, token), KtFirDiagnostic.ExternalAnonymousInitializer

internal class ExternalDelegationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ExternalDelegation

internal class ExternalDelegatedConstructorCallImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ExternalDelegatedConstructorCall

internal class WrongBodyOfExternalDeclarationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.WrongBodyOfExternalDeclaration

internal class WrongInitializerOfExternalDeclarationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.WrongInitializerOfExternalDeclaration

internal class WrongDefaultValueForExternalFunParameterImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.WrongDefaultValueForExternalFunParameter

internal class NestedExternalDeclarationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.NestedExternalDeclaration

internal class WrongExternalDeclarationImpl(
    override konst classKind: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.WrongExternalDeclaration

internal class NestedClassInExternalInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.NestedClassInExternalInterface

internal class ExternalTypeExtendsNonExternalTypeImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ExternalTypeExtendsNonExternalType

internal class InlineExternalDeclarationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.InlineExternalDeclaration

internal class EnumClassInExternalDeclarationWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.EnumClassInExternalDeclarationWarning

internal class InlineClassInExternalDeclarationWarningImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.InlineClassInExternalDeclarationWarning

internal class InlineClassInExternalDeclarationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.InlineClassInExternalDeclaration

internal class ExtensionFunctionInExternalDeclarationImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ExtensionFunctionInExternalDeclaration

internal class NonAbstractMemberOfExternalInterfaceImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.NonAbstractMemberOfExternalInterface

internal class NonExternalDeclarationInInappropriateFileImpl(
    override konst type: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.NonExternalDeclarationInInappropriateFile

internal class CannotCheckForExternalInterfaceImpl(
    override konst targetType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.CannotCheckForExternalInterface

internal class UncheckedCastToExternalInterfaceImpl(
    override konst sourceType: KtType,
    override konst targetType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.UncheckedCastToExternalInterface

internal class ExternalInterfaceAsClassLiteralImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.ExternalInterfaceAsClassLiteral

internal class JsExternalInheritorsOnlyImpl(
    override konst parent: KtClassLikeSymbol,
    override konst kid: KtClassLikeSymbol,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtDeclaration>(firDiagnostic, token), KtFirDiagnostic.JsExternalInheritorsOnly

internal class JsExternalArgumentImpl(
    override konst argType: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtExpression>(firDiagnostic, token), KtFirDiagnostic.JsExternalArgument

internal class NestedJsExportImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.NestedJsExport

internal class WrongExportedDeclarationImpl(
    override konst kind: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.WrongExportedDeclaration

internal class NonExportableTypeImpl(
    override konst kind: String,
    override konst type: KtType,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.NonExportableType

internal class NonConsumableExportedIdentifierImpl(
    override konst name: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.NonConsumableExportedIdentifier

internal class DelegationByDynamicImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.DelegationByDynamic

internal class SpreadOperatorInDynamicCallImpl(
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.SpreadOperatorInDynamicCall

internal class WrongOperationWithDynamicImpl(
    override konst operation: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<KtElement>(firDiagnostic, token), KtFirDiagnostic.WrongOperationWithDynamic

internal class SyntaxImpl(
    override konst message: String,
    firDiagnostic: KtPsiDiagnostic,
    token: KtLifetimeToken,
) : KtAbstractFirDiagnostic<PsiElement>(firDiagnostic, token), KtFirDiagnostic.Syntax

