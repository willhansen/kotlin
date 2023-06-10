/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.diagnostics

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.analysis.api.diagnostics.KtDiagnosticWithPsi
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

sealed interface KtFirDiagnostic<PSI : PsiElement> : KtDiagnosticWithPsi<PSI> {
    interface Unsupported : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = Unsupported::class
        konst unsupported: String
    }

    interface UnsupportedFeature : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = UnsupportedFeature::class
        konst unsupportedFeature: Pair<LanguageFeature, LanguageVersionSettings>
    }

    interface NewInferenceError : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NewInferenceError::class
        konst error: String
    }

    interface OtherError : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = OtherError::class
    }

    interface IllegalConstExpression : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = IllegalConstExpression::class
    }

    interface IllegalUnderscore : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = IllegalUnderscore::class
    }

    interface ExpressionExpected : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ExpressionExpected::class
    }

    interface AssignmentInExpressionContext : KtFirDiagnostic<KtBinaryExpression> {
        override konst diagnosticClass get() = AssignmentInExpressionContext::class
    }

    interface BreakOrContinueOutsideALoop : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = BreakOrContinueOutsideALoop::class
    }

    interface NotALoopLabel : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NotALoopLabel::class
    }

    interface BreakOrContinueJumpsAcrossFunctionBoundary : KtFirDiagnostic<KtExpressionWithLabel> {
        override konst diagnosticClass get() = BreakOrContinueJumpsAcrossFunctionBoundary::class
    }

    interface VariableExpected : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = VariableExpected::class
    }

    interface DelegationInInterface : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DelegationInInterface::class
    }

    interface DelegationNotToInterface : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DelegationNotToInterface::class
    }

    interface NestedClassNotAllowed : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = NestedClassNotAllowed::class
        konst declaration: String
    }

    interface IncorrectCharacterLiteral : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = IncorrectCharacterLiteral::class
    }

    interface EmptyCharacterLiteral : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = EmptyCharacterLiteral::class
    }

    interface TooManyCharactersInCharacterLiteral : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = TooManyCharactersInCharacterLiteral::class
    }

    interface IllegalEscape : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = IllegalEscape::class
    }

    interface IntLiteralOutOfRange : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = IntLiteralOutOfRange::class
    }

    interface FloatLiteralOutOfRange : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = FloatLiteralOutOfRange::class
    }

    interface WrongLongSuffix : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = WrongLongSuffix::class
    }

    interface UnsignedLiteralWithoutDeclarationsOnClasspath : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = UnsignedLiteralWithoutDeclarationsOnClasspath::class
    }

    interface DivisionByZero : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = DivisionByZero::class
    }

    interface ValOrVarOnLoopParameter : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = ValOrVarOnLoopParameter::class
        konst konstOrVar: KtKeywordToken
    }

    interface ValOrVarOnFunParameter : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = ValOrVarOnFunParameter::class
        konst konstOrVar: KtKeywordToken
    }

    interface ValOrVarOnCatchParameter : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = ValOrVarOnCatchParameter::class
        konst konstOrVar: KtKeywordToken
    }

    interface ValOrVarOnSecondaryConstructorParameter : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = ValOrVarOnSecondaryConstructorParameter::class
        konst konstOrVar: KtKeywordToken
    }

    interface InvisibleSetter : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = InvisibleSetter::class
        konst property: KtVariableSymbol
        konst visibility: Visibility
        konst callableId: CallableId
    }

    interface InvisibleReference : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = InvisibleReference::class
        konst reference: KtSymbol
    }

    interface UnresolvedReference : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = UnresolvedReference::class
        konst reference: String
    }

    interface UnresolvedLabel : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = UnresolvedLabel::class
    }

    interface DeserializationError : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DeserializationError::class
    }

    interface ErrorFromJavaResolution : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ErrorFromJavaResolution::class
    }

    interface MissingStdlibClass : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = MissingStdlibClass::class
    }

    interface NoThis : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NoThis::class
    }

    interface DeprecationError : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DeprecationError::class
        konst reference: KtSymbol
        konst message: String
    }

    interface Deprecation : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = Deprecation::class
        konst reference: KtSymbol
        konst message: String
    }

    interface TypealiasExpansionDeprecationError : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = TypealiasExpansionDeprecationError::class
        konst alias: KtSymbol
        konst reference: KtSymbol
        konst message: String
    }

    interface TypealiasExpansionDeprecation : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = TypealiasExpansionDeprecation::class
        konst alias: KtSymbol
        konst reference: KtSymbol
        konst message: String
    }

    interface ApiNotAvailable : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ApiNotAvailable::class
        konst sinceKotlinVersion: ApiVersion
        konst currentVersion: ApiVersion
    }

    interface UnresolvedReferenceWrongReceiver : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = UnresolvedReferenceWrongReceiver::class
        konst candidates: List<KtSymbol>
    }

    interface UnresolvedImport : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = UnresolvedImport::class
        konst reference: String
    }

    interface CreatingAnInstanceOfAbstractClass : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = CreatingAnInstanceOfAbstractClass::class
    }

    interface FunctionCallExpected : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = FunctionCallExpected::class
        konst functionName: String
        konst hasValueParameters: Boolean
    }

    interface IllegalSelector : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = IllegalSelector::class
    }

    interface NoReceiverAllowed : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NoReceiverAllowed::class
    }

    interface FunctionExpected : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = FunctionExpected::class
        konst expression: String
        konst type: KtType
    }

    interface ResolutionToClassifier : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ResolutionToClassifier::class
        konst classSymbol: KtClassLikeSymbol
    }

    interface AmbiguousAlteredAssign : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = AmbiguousAlteredAssign::class
        konst altererNames: List<String?>
    }

    interface ForbiddenBinaryMod : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ForbiddenBinaryMod::class
        konst forbiddenFunction: KtSymbol
        konst suggestedFunction: String
    }

    interface DeprecatedBinaryMod : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DeprecatedBinaryMod::class
        konst forbiddenFunction: KtSymbol
        konst suggestedFunction: String
    }

    interface SuperIsNotAnExpression : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = SuperIsNotAnExpression::class
    }

    interface SuperNotAvailable : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = SuperNotAvailable::class
    }

    interface AbstractSuperCall : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = AbstractSuperCall::class
    }

    interface AbstractSuperCallWarning : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = AbstractSuperCallWarning::class
    }

    interface InstanceAccessBeforeSuperCall : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = InstanceAccessBeforeSuperCall::class
        konst target: String
    }

    interface SuperCallWithDefaultParameters : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = SuperCallWithDefaultParameters::class
        konst name: String
    }

    interface InterfaceCantCallDefaultMethodViaSuper : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = InterfaceCantCallDefaultMethodViaSuper::class
    }

    interface NotASupertype : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NotASupertype::class
    }

    interface TypeArgumentsRedundantInSuperQualifier : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = TypeArgumentsRedundantInSuperQualifier::class
    }

    interface SuperclassNotAccessibleFromInterface : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = SuperclassNotAccessibleFromInterface::class
    }

    interface QualifiedSupertypeExtendedByOtherSupertype : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = QualifiedSupertypeExtendedByOtherSupertype::class
        konst otherSuperType: KtSymbol
    }

    interface SupertypeInitializedInInterface : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = SupertypeInitializedInInterface::class
    }

    interface InterfaceWithSuperclass : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = InterfaceWithSuperclass::class
    }

    interface FinalSupertype : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = FinalSupertype::class
    }

    interface ClassCannotBeExtendedDirectly : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = ClassCannotBeExtendedDirectly::class
        konst classSymbol: KtClassLikeSymbol
    }

    interface SupertypeIsExtensionFunctionType : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = SupertypeIsExtensionFunctionType::class
    }

    interface SingletonInSupertype : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = SingletonInSupertype::class
    }

    interface NullableSupertype : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = NullableSupertype::class
    }

    interface ManyClassesInSupertypeList : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = ManyClassesInSupertypeList::class
    }

    interface SupertypeAppearsTwice : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = SupertypeAppearsTwice::class
    }

    interface ClassInSupertypeForEnum : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = ClassInSupertypeForEnum::class
    }

    interface SealedSupertype : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = SealedSupertype::class
    }

    interface SealedSupertypeInLocalClass : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = SealedSupertypeInLocalClass::class
        konst declarationType: String
        konst sealedClassKind: ClassKind
    }

    interface SealedInheritorInDifferentPackage : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = SealedInheritorInDifferentPackage::class
    }

    interface SealedInheritorInDifferentModule : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = SealedInheritorInDifferentModule::class
    }

    interface ClassInheritsJavaSealedClass : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = ClassInheritsJavaSealedClass::class
    }

    interface SupertypeNotAClassOrInterface : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = SupertypeNotAClassOrInterface::class
        konst reason: String
    }

    interface CyclicInheritanceHierarchy : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = CyclicInheritanceHierarchy::class
    }

    interface ExpandedTypeCannotBeInherited : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = ExpandedTypeCannotBeInherited::class
        konst type: KtType
    }

    interface ProjectionInImmediateArgumentToSupertype : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = ProjectionInImmediateArgumentToSupertype::class
    }

    interface InconsistentTypeParameterValues : KtFirDiagnostic<KtClass> {
        override konst diagnosticClass get() = InconsistentTypeParameterValues::class
        konst typeParameter: KtTypeParameterSymbol
        konst type: KtClassLikeSymbol
        konst bounds: List<KtType>
    }

    interface InconsistentTypeParameterBounds : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = InconsistentTypeParameterBounds::class
        konst typeParameter: KtTypeParameterSymbol
        konst type: KtClassLikeSymbol
        konst bounds: List<KtType>
    }

    interface AmbiguousSuper : KtFirDiagnostic<KtSuperExpression> {
        override konst diagnosticClass get() = AmbiguousSuper::class
        konst candidates: List<KtType>
    }

    interface WrongMultipleInheritance : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = WrongMultipleInheritance::class
        konst symbol: KtCallableSymbol
    }

    interface ConstructorInObject : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = ConstructorInObject::class
    }

    interface ConstructorInInterface : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = ConstructorInInterface::class
    }

    interface NonPrivateConstructorInEnum : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NonPrivateConstructorInEnum::class
    }

    interface NonPrivateOrProtectedConstructorInSealed : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NonPrivateOrProtectedConstructorInSealed::class
    }

    interface CyclicConstructorDelegationCall : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = CyclicConstructorDelegationCall::class
    }

    interface PrimaryConstructorDelegationCallExpected : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = PrimaryConstructorDelegationCallExpected::class
    }

    interface SupertypeNotInitialized : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = SupertypeNotInitialized::class
    }

    interface SupertypeInitializedWithoutPrimaryConstructor : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = SupertypeInitializedWithoutPrimaryConstructor::class
    }

    interface DelegationSuperCallInEnumConstructor : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DelegationSuperCallInEnumConstructor::class
    }

    interface PrimaryConstructorRequiredForDataClass : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = PrimaryConstructorRequiredForDataClass::class
    }

    interface ExplicitDelegationCallRequired : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ExplicitDelegationCallRequired::class
    }

    interface SealedClassConstructorCall : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = SealedClassConstructorCall::class
    }

    interface DataClassWithoutParameters : KtFirDiagnostic<KtPrimaryConstructor> {
        override konst diagnosticClass get() = DataClassWithoutParameters::class
    }

    interface DataClassVarargParameter : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = DataClassVarargParameter::class
    }

    interface DataClassNotPropertyParameter : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = DataClassNotPropertyParameter::class
    }

    interface AnnotationArgumentKclassLiteralOfTypeParameterError : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = AnnotationArgumentKclassLiteralOfTypeParameterError::class
    }

    interface AnnotationArgumentMustBeConst : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = AnnotationArgumentMustBeConst::class
    }

    interface AnnotationArgumentMustBeEnumConst : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = AnnotationArgumentMustBeEnumConst::class
    }

    interface AnnotationArgumentMustBeKclassLiteral : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = AnnotationArgumentMustBeKclassLiteral::class
    }

    interface AnnotationClassMember : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = AnnotationClassMember::class
    }

    interface AnnotationParameterDefaultValueMustBeConstant : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = AnnotationParameterDefaultValueMustBeConstant::class
    }

    interface InkonstidTypeOfAnnotationMember : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = InkonstidTypeOfAnnotationMember::class
    }

    interface LocalAnnotationClassError : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = LocalAnnotationClassError::class
    }

    interface MissingValOnAnnotationParameter : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = MissingValOnAnnotationParameter::class
    }

    interface NonConstValUsedInConstantExpression : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = NonConstValUsedInConstantExpression::class
    }

    interface CycleInAnnotationParameterError : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = CycleInAnnotationParameterError::class
    }

    interface CycleInAnnotationParameterWarning : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = CycleInAnnotationParameterWarning::class
    }

    interface AnnotationClassConstructorCall : KtFirDiagnostic<KtCallExpression> {
        override konst diagnosticClass get() = AnnotationClassConstructorCall::class
    }

    interface NotAnAnnotationClass : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NotAnAnnotationClass::class
        konst annotationName: String
    }

    interface NullableTypeOfAnnotationMember : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = NullableTypeOfAnnotationMember::class
    }

    interface VarAnnotationParameter : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = VarAnnotationParameter::class
    }

    interface SupertypesForAnnotationClass : KtFirDiagnostic<KtClass> {
        override konst diagnosticClass get() = SupertypesForAnnotationClass::class
    }

    interface AnnotationUsedAsAnnotationArgument : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = AnnotationUsedAsAnnotationArgument::class
    }

    interface IllegalKotlinVersionStringValue : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = IllegalKotlinVersionStringValue::class
    }

    interface NewerVersionInSinceKotlin : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = NewerVersionInSinceKotlin::class
        konst specifiedVersion: String
    }

    interface DeprecatedSinceKotlinWithUnorderedVersions : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DeprecatedSinceKotlinWithUnorderedVersions::class
    }

    interface DeprecatedSinceKotlinWithoutArguments : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DeprecatedSinceKotlinWithoutArguments::class
    }

    interface DeprecatedSinceKotlinWithoutDeprecated : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DeprecatedSinceKotlinWithoutDeprecated::class
    }

    interface DeprecatedSinceKotlinWithDeprecatedLevel : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DeprecatedSinceKotlinWithDeprecatedLevel::class
    }

    interface DeprecatedSinceKotlinOutsideKotlinSubpackage : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DeprecatedSinceKotlinOutsideKotlinSubpackage::class
    }

    interface OverrideDeprecation : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = OverrideDeprecation::class
        konst overridenSymbol: KtSymbol
        konst deprecationInfo: DeprecationInfo
    }

    interface AnnotationOnSuperclassError : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = AnnotationOnSuperclassError::class
    }

    interface AnnotationOnSuperclassWarning : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = AnnotationOnSuperclassWarning::class
    }

    interface RestrictedRetentionForExpressionAnnotationError : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = RestrictedRetentionForExpressionAnnotationError::class
    }

    interface RestrictedRetentionForExpressionAnnotationWarning : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = RestrictedRetentionForExpressionAnnotationWarning::class
    }

    interface WrongAnnotationTarget : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = WrongAnnotationTarget::class
        konst actualTarget: String
    }

    interface WrongAnnotationTargetWithUseSiteTarget : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = WrongAnnotationTargetWithUseSiteTarget::class
        konst actualTarget: String
        konst useSiteTarget: String
    }

    interface InapplicableTargetOnProperty : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = InapplicableTargetOnProperty::class
        konst useSiteDescription: String
    }

    interface InapplicableTargetOnPropertyWarning : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = InapplicableTargetOnPropertyWarning::class
        konst useSiteDescription: String
    }

    interface InapplicableTargetPropertyImmutable : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = InapplicableTargetPropertyImmutable::class
        konst useSiteDescription: String
    }

    interface InapplicableTargetPropertyHasNoDelegate : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = InapplicableTargetPropertyHasNoDelegate::class
    }

    interface InapplicableTargetPropertyHasNoBackingField : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = InapplicableTargetPropertyHasNoBackingField::class
    }

    interface InapplicableParamTarget : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = InapplicableParamTarget::class
    }

    interface RedundantAnnotationTarget : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = RedundantAnnotationTarget::class
        konst useSiteDescription: String
    }

    interface InapplicableFileTarget : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = InapplicableFileTarget::class
    }

    interface RepeatedAnnotation : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = RepeatedAnnotation::class
    }

    interface RepeatedAnnotationWarning : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = RepeatedAnnotationWarning::class
    }

    interface NotAClass : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NotAClass::class
    }

    interface WrongExtensionFunctionType : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = WrongExtensionFunctionType::class
    }

    interface WrongExtensionFunctionTypeWarning : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = WrongExtensionFunctionTypeWarning::class
    }

    interface AnnotationInWhereClauseError : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = AnnotationInWhereClauseError::class
    }

    interface PluginAnnotationAmbiguity : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = PluginAnnotationAmbiguity::class
        konst typeFromCompilerPhase: KtType
        konst typeFromTypesPhase: KtType
    }

    interface AmbiguousAnnotationArgument : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = AmbiguousAnnotationArgument::class
        konst symbols: List<KtSymbol>
    }

    interface VolatileOnValue : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = VolatileOnValue::class
    }

    interface VolatileOnDelegate : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = VolatileOnDelegate::class
    }

    interface WrongJsQualifier : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = WrongJsQualifier::class
    }

    interface JsModuleProhibitedOnVar : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = JsModuleProhibitedOnVar::class
    }

    interface JsModuleProhibitedOnNonNative : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = JsModuleProhibitedOnNonNative::class
    }

    interface NestedJsModuleProhibited : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = NestedJsModuleProhibited::class
    }

    interface RuntimeAnnotationNotSupported : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = RuntimeAnnotationNotSupported::class
    }

    interface RuntimeAnnotationOnExternalDeclaration : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = RuntimeAnnotationOnExternalDeclaration::class
    }

    interface NativeAnnotationsAllowedOnlyOnMemberOrExtensionFun : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = NativeAnnotationsAllowedOnlyOnMemberOrExtensionFun::class
        konst type: KtType
    }

    interface NativeIndexerKeyShouldBeStringOrNumber : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = NativeIndexerKeyShouldBeStringOrNumber::class
        konst kind: String
    }

    interface NativeIndexerWrongParameterCount : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = NativeIndexerWrongParameterCount::class
        konst parametersCount: Int
        konst kind: String
    }

    interface NativeIndexerCanNotHaveDefaultArguments : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = NativeIndexerCanNotHaveDefaultArguments::class
        konst kind: String
    }

    interface NativeGetterReturnTypeShouldBeNullable : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = NativeGetterReturnTypeShouldBeNullable::class
    }

    interface NativeSetterWrongReturnType : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = NativeSetterWrongReturnType::class
    }

    interface JsNameIsNotOnAllAccessors : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = JsNameIsNotOnAllAccessors::class
    }

    interface JsNameProhibitedForNamedNative : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = JsNameProhibitedForNamedNative::class
    }

    interface JsNameProhibitedForOverride : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = JsNameProhibitedForOverride::class
    }

    interface JsNameOnPrimaryConstructorProhibited : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = JsNameOnPrimaryConstructorProhibited::class
    }

    interface JsNameOnAccessorAndProperty : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = JsNameOnAccessorAndProperty::class
    }

    interface JsNameProhibitedForExtensionProperty : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = JsNameProhibitedForExtensionProperty::class
    }

    interface OptInUsage : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = OptInUsage::class
        konst optInMarkerFqName: FqName
        konst message: String
    }

    interface OptInUsageError : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = OptInUsageError::class
        konst optInMarkerFqName: FqName
        konst message: String
    }

    interface OptInOverride : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = OptInOverride::class
        konst optInMarkerFqName: FqName
        konst message: String
    }

    interface OptInOverrideError : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = OptInOverrideError::class
        konst optInMarkerFqName: FqName
        konst message: String
    }

    interface OptInIsNotEnabled : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = OptInIsNotEnabled::class
    }

    interface OptInCanOnlyBeUsedAsAnnotation : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = OptInCanOnlyBeUsedAsAnnotation::class
    }

    interface OptInMarkerCanOnlyBeUsedAsAnnotationOrArgumentInOptIn : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = OptInMarkerCanOnlyBeUsedAsAnnotationOrArgumentInOptIn::class
    }

    interface OptInWithoutArguments : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = OptInWithoutArguments::class
    }

    interface OptInArgumentIsNotMarker : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = OptInArgumentIsNotMarker::class
        konst notMarkerFqName: FqName
    }

    interface OptInMarkerWithWrongTarget : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = OptInMarkerWithWrongTarget::class
        konst target: String
    }

    interface OptInMarkerWithWrongRetention : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = OptInMarkerWithWrongRetention::class
    }

    interface OptInMarkerOnWrongTarget : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = OptInMarkerOnWrongTarget::class
        konst target: String
    }

    interface OptInMarkerOnOverride : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = OptInMarkerOnOverride::class
    }

    interface OptInMarkerOnOverrideWarning : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = OptInMarkerOnOverrideWarning::class
    }

    interface SubclassOptInInapplicable : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = SubclassOptInInapplicable::class
        konst target: String
    }

    interface ExposedTypealiasExpandedType : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = ExposedTypealiasExpandedType::class
        konst elementVisibility: EffectiveVisibility
        konst restrictingDeclaration: KtSymbol
        konst restrictingVisibility: EffectiveVisibility
    }

    interface ExposedFunctionReturnType : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = ExposedFunctionReturnType::class
        konst elementVisibility: EffectiveVisibility
        konst restrictingDeclaration: KtSymbol
        konst restrictingVisibility: EffectiveVisibility
    }

    interface ExposedReceiverType : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = ExposedReceiverType::class
        konst elementVisibility: EffectiveVisibility
        konst restrictingDeclaration: KtSymbol
        konst restrictingVisibility: EffectiveVisibility
    }

    interface ExposedPropertyType : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = ExposedPropertyType::class
        konst elementVisibility: EffectiveVisibility
        konst restrictingDeclaration: KtSymbol
        konst restrictingVisibility: EffectiveVisibility
    }

    interface ExposedPropertyTypeInConstructorError : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = ExposedPropertyTypeInConstructorError::class
        konst elementVisibility: EffectiveVisibility
        konst restrictingDeclaration: KtSymbol
        konst restrictingVisibility: EffectiveVisibility
    }

    interface ExposedPropertyTypeInConstructorWarning : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = ExposedPropertyTypeInConstructorWarning::class
        konst elementVisibility: EffectiveVisibility
        konst restrictingDeclaration: KtSymbol
        konst restrictingVisibility: EffectiveVisibility
    }

    interface ExposedParameterType : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = ExposedParameterType::class
        konst elementVisibility: EffectiveVisibility
        konst restrictingDeclaration: KtSymbol
        konst restrictingVisibility: EffectiveVisibility
    }

    interface ExposedSuperInterface : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = ExposedSuperInterface::class
        konst elementVisibility: EffectiveVisibility
        konst restrictingDeclaration: KtSymbol
        konst restrictingVisibility: EffectiveVisibility
    }

    interface ExposedSuperClass : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = ExposedSuperClass::class
        konst elementVisibility: EffectiveVisibility
        konst restrictingDeclaration: KtSymbol
        konst restrictingVisibility: EffectiveVisibility
    }

    interface ExposedTypeParameterBound : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = ExposedTypeParameterBound::class
        konst elementVisibility: EffectiveVisibility
        konst restrictingDeclaration: KtSymbol
        konst restrictingVisibility: EffectiveVisibility
    }

    interface InapplicableInfixModifier : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = InapplicableInfixModifier::class
    }

    interface RepeatedModifier : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = RepeatedModifier::class
        konst modifier: KtModifierKeywordToken
    }

    interface RedundantModifier : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = RedundantModifier::class
        konst redundantModifier: KtModifierKeywordToken
        konst conflictingModifier: KtModifierKeywordToken
    }

    interface DeprecatedModifier : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DeprecatedModifier::class
        konst deprecatedModifier: KtModifierKeywordToken
        konst actualModifier: KtModifierKeywordToken
    }

    interface DeprecatedModifierPair : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DeprecatedModifierPair::class
        konst deprecatedModifier: KtModifierKeywordToken
        konst conflictingModifier: KtModifierKeywordToken
    }

    interface DeprecatedModifierForTarget : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DeprecatedModifierForTarget::class
        konst deprecatedModifier: KtModifierKeywordToken
        konst target: String
    }

    interface RedundantModifierForTarget : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = RedundantModifierForTarget::class
        konst redundantModifier: KtModifierKeywordToken
        konst target: String
    }

    interface IncompatibleModifiers : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = IncompatibleModifiers::class
        konst modifier1: KtModifierKeywordToken
        konst modifier2: KtModifierKeywordToken
    }

    interface RedundantOpenInInterface : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = RedundantOpenInInterface::class
    }

    interface WrongModifierTarget : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = WrongModifierTarget::class
        konst modifier: KtModifierKeywordToken
        konst target: String
    }

    interface OperatorModifierRequired : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = OperatorModifierRequired::class
        konst functionSymbol: KtFunctionLikeSymbol
        konst name: String
    }

    interface InfixModifierRequired : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = InfixModifierRequired::class
        konst functionSymbol: KtFunctionLikeSymbol
    }

    interface WrongModifierContainingDeclaration : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = WrongModifierContainingDeclaration::class
        konst modifier: KtModifierKeywordToken
        konst target: String
    }

    interface DeprecatedModifierContainingDeclaration : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DeprecatedModifierContainingDeclaration::class
        konst modifier: KtModifierKeywordToken
        konst target: String
    }

    interface InapplicableOperatorModifier : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = InapplicableOperatorModifier::class
        konst message: String
    }

    interface NoExplicitVisibilityInApiMode : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = NoExplicitVisibilityInApiMode::class
    }

    interface NoExplicitVisibilityInApiModeWarning : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = NoExplicitVisibilityInApiModeWarning::class
    }

    interface NoExplicitReturnTypeInApiMode : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = NoExplicitReturnTypeInApiMode::class
    }

    interface NoExplicitReturnTypeInApiModeWarning : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = NoExplicitReturnTypeInApiModeWarning::class
    }

    interface ValueClassNotTopLevel : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = ValueClassNotTopLevel::class
    }

    interface ValueClassNotFinal : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = ValueClassNotFinal::class
    }

    interface AbsenceOfPrimaryConstructorForValueClass : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = AbsenceOfPrimaryConstructorForValueClass::class
    }

    interface InlineClassConstructorWrongParametersSize : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = InlineClassConstructorWrongParametersSize::class
    }

    interface ValueClassEmptyConstructor : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ValueClassEmptyConstructor::class
    }

    interface ValueClassConstructorNotFinalReadOnlyParameter : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = ValueClassConstructorNotFinalReadOnlyParameter::class
    }

    interface PropertyWithBackingFieldInsideValueClass : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = PropertyWithBackingFieldInsideValueClass::class
    }

    interface DelegatedPropertyInsideValueClass : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DelegatedPropertyInsideValueClass::class
    }

    interface ValueClassHasInapplicableParameterType : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = ValueClassHasInapplicableParameterType::class
        konst type: KtType
    }

    interface ValueClassCannotImplementInterfaceByDelegation : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ValueClassCannotImplementInterfaceByDelegation::class
    }

    interface ValueClassCannotExtendClasses : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = ValueClassCannotExtendClasses::class
    }

    interface ValueClassCannotBeRecursive : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = ValueClassCannotBeRecursive::class
    }

    interface MultiFieldValueClassPrimaryConstructorDefaultParameter : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = MultiFieldValueClassPrimaryConstructorDefaultParameter::class
    }

    interface SecondaryConstructorWithBodyInsideValueClass : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = SecondaryConstructorWithBodyInsideValueClass::class
    }

    interface ReservedMemberInsideValueClass : KtFirDiagnostic<KtFunction> {
        override konst diagnosticClass get() = ReservedMemberInsideValueClass::class
        konst name: String
    }

    interface TypeArgumentOnTypedValueClassEquals : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = TypeArgumentOnTypedValueClassEquals::class
    }

    interface InnerClassInsideValueClass : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = InnerClassInsideValueClass::class
    }

    interface ValueClassCannotBeCloneable : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = ValueClassCannotBeCloneable::class
    }

    interface AnnotationOnIllegalMultiFieldValueClassTypedTarget : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = AnnotationOnIllegalMultiFieldValueClassTypedTarget::class
        konst name: String
    }

    interface NoneApplicable : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NoneApplicable::class
        konst candidates: List<KtSymbol>
    }

    interface InapplicableCandidate : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = InapplicableCandidate::class
        konst candidate: KtSymbol
    }

    interface TypeMismatch : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = TypeMismatch::class
        konst expectedType: KtType
        konst actualType: KtType
        konst isMismatchDueToNullability: Boolean
    }

    interface TypeInferenceOnlyInputTypesError : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = TypeInferenceOnlyInputTypesError::class
        konst typeParameter: KtTypeParameterSymbol
    }

    interface ThrowableTypeMismatch : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ThrowableTypeMismatch::class
        konst actualType: KtType
        konst isMismatchDueToNullability: Boolean
    }

    interface ConditionTypeMismatch : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ConditionTypeMismatch::class
        konst actualType: KtType
        konst isMismatchDueToNullability: Boolean
    }

    interface ArgumentTypeMismatch : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ArgumentTypeMismatch::class
        konst expectedType: KtType
        konst actualType: KtType
        konst isMismatchDueToNullability: Boolean
    }

    interface NullForNonnullType : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NullForNonnullType::class
    }

    interface InapplicableLateinitModifier : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = InapplicableLateinitModifier::class
        konst reason: String
    }

    interface VarargOutsideParentheses : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = VarargOutsideParentheses::class
    }

    interface NamedArgumentsNotAllowed : KtFirDiagnostic<KtValueArgument> {
        override konst diagnosticClass get() = NamedArgumentsNotAllowed::class
        konst forbiddenNamedArgumentsTarget: ForbiddenNamedArgumentsTarget
    }

    interface NonVarargSpread : KtFirDiagnostic<LeafPsiElement> {
        override konst diagnosticClass get() = NonVarargSpread::class
    }

    interface ArgumentPassedTwice : KtFirDiagnostic<KtValueArgument> {
        override konst diagnosticClass get() = ArgumentPassedTwice::class
    }

    interface TooManyArguments : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = TooManyArguments::class
        konst function: KtCallableSymbol
    }

    interface NoValueForParameter : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = NoValueForParameter::class
        konst violatedParameter: KtSymbol
    }

    interface NamedParameterNotFound : KtFirDiagnostic<KtValueArgument> {
        override konst diagnosticClass get() = NamedParameterNotFound::class
        konst name: String
    }

    interface NameForAmbiguousParameter : KtFirDiagnostic<KtValueArgument> {
        override konst diagnosticClass get() = NameForAmbiguousParameter::class
    }

    interface AssignmentTypeMismatch : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = AssignmentTypeMismatch::class
        konst expectedType: KtType
        konst actualType: KtType
        konst isMismatchDueToNullability: Boolean
    }

    interface ResultTypeMismatch : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = ResultTypeMismatch::class
        konst expectedType: KtType
        konst actualType: KtType
    }

    interface ManyLambdaExpressionArguments : KtFirDiagnostic<KtValueArgument> {
        override konst diagnosticClass get() = ManyLambdaExpressionArguments::class
    }

    interface NewInferenceNoInformationForParameter : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = NewInferenceNoInformationForParameter::class
        konst name: String
    }

    interface SpreadOfNullable : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = SpreadOfNullable::class
    }

    interface AssigningSingleElementToVarargInNamedFormFunctionError : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = AssigningSingleElementToVarargInNamedFormFunctionError::class
        konst expectedArrayType: KtType
    }

    interface AssigningSingleElementToVarargInNamedFormFunctionWarning : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = AssigningSingleElementToVarargInNamedFormFunctionWarning::class
        konst expectedArrayType: KtType
    }

    interface AssigningSingleElementToVarargInNamedFormAnnotationError : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = AssigningSingleElementToVarargInNamedFormAnnotationError::class
    }

    interface AssigningSingleElementToVarargInNamedFormAnnotationWarning : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = AssigningSingleElementToVarargInNamedFormAnnotationWarning::class
    }

    interface RedundantSpreadOperatorInNamedFormInAnnotation : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = RedundantSpreadOperatorInNamedFormInAnnotation::class
    }

    interface RedundantSpreadOperatorInNamedFormInFunction : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = RedundantSpreadOperatorInNamedFormInFunction::class
    }

    interface InferenceUnsuccessfulFork : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = InferenceUnsuccessfulFork::class
        konst message: String
    }

    interface OverloadResolutionAmbiguity : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = OverloadResolutionAmbiguity::class
        konst candidates: List<KtSymbol>
    }

    interface AssignOperatorAmbiguity : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = AssignOperatorAmbiguity::class
        konst candidates: List<KtSymbol>
    }

    interface IteratorAmbiguity : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = IteratorAmbiguity::class
        konst candidates: List<KtSymbol>
    }

    interface HasNextFunctionAmbiguity : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = HasNextFunctionAmbiguity::class
        konst candidates: List<KtSymbol>
    }

    interface NextAmbiguity : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NextAmbiguity::class
        konst candidates: List<KtSymbol>
    }

    interface AmbiguousFunctionTypeKind : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = AmbiguousFunctionTypeKind::class
        konst kinds: List<FunctionTypeKind>
    }

    interface NoContextReceiver : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = NoContextReceiver::class
        konst contextReceiverRepresentation: KtType
    }

    interface MultipleArgumentsApplicableForContextReceiver : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = MultipleArgumentsApplicableForContextReceiver::class
        konst contextReceiverRepresentation: KtType
    }

    interface AmbiguousCallWithImplicitContextReceiver : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = AmbiguousCallWithImplicitContextReceiver::class
    }

    interface UnsupportedContextualDeclarationCall : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = UnsupportedContextualDeclarationCall::class
    }

    interface RecursionInImplicitTypes : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = RecursionInImplicitTypes::class
    }

    interface InferenceError : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = InferenceError::class
    }

    interface ProjectionOnNonClassTypeArgument : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ProjectionOnNonClassTypeArgument::class
    }

    interface UpperBoundViolated : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = UpperBoundViolated::class
        konst expectedUpperBound: KtType
        konst actualUpperBound: KtType
    }

    interface UpperBoundViolatedInTypealiasExpansion : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = UpperBoundViolatedInTypealiasExpansion::class
        konst expectedUpperBound: KtType
        konst actualUpperBound: KtType
    }

    interface TypeArgumentsNotAllowed : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = TypeArgumentsNotAllowed::class
    }

    interface WrongNumberOfTypeArguments : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = WrongNumberOfTypeArguments::class
        konst expectedCount: Int
        konst classifier: KtClassLikeSymbol
    }

    interface NoTypeArgumentsOnRhs : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NoTypeArgumentsOnRhs::class
        konst expectedCount: Int
        konst classifier: KtClassLikeSymbol
    }

    interface OuterClassArgumentsRequired : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = OuterClassArgumentsRequired::class
        konst outer: KtClassLikeSymbol
    }

    interface TypeParametersInObject : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = TypeParametersInObject::class
    }

    interface TypeParametersInAnonymousObject : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = TypeParametersInAnonymousObject::class
    }

    interface IllegalProjectionUsage : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = IllegalProjectionUsage::class
    }

    interface TypeParametersInEnum : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = TypeParametersInEnum::class
    }

    interface ConflictingProjection : KtFirDiagnostic<KtTypeProjection> {
        override konst diagnosticClass get() = ConflictingProjection::class
        konst type: KtType
    }

    interface ConflictingProjectionInTypealiasExpansion : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ConflictingProjectionInTypealiasExpansion::class
        konst type: KtType
    }

    interface RedundantProjection : KtFirDiagnostic<KtTypeProjection> {
        override konst diagnosticClass get() = RedundantProjection::class
        konst type: KtType
    }

    interface VarianceOnTypeParameterNotAllowed : KtFirDiagnostic<KtTypeParameter> {
        override konst diagnosticClass get() = VarianceOnTypeParameterNotAllowed::class
    }

    interface CatchParameterWithDefaultValue : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = CatchParameterWithDefaultValue::class
    }

    interface ReifiedTypeInCatchClause : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ReifiedTypeInCatchClause::class
    }

    interface TypeParameterInCatchClause : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = TypeParameterInCatchClause::class
    }

    interface GenericThrowableSubclass : KtFirDiagnostic<KtTypeParameter> {
        override konst diagnosticClass get() = GenericThrowableSubclass::class
    }

    interface InnerClassOfGenericThrowableSubclass : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = InnerClassOfGenericThrowableSubclass::class
    }

    interface KclassWithNullableTypeParameterInSignature : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = KclassWithNullableTypeParameterInSignature::class
        konst typeParameter: KtTypeParameterSymbol
    }

    interface TypeParameterAsReified : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = TypeParameterAsReified::class
        konst typeParameter: KtTypeParameterSymbol
    }

    interface TypeParameterAsReifiedArrayError : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = TypeParameterAsReifiedArrayError::class
        konst typeParameter: KtTypeParameterSymbol
    }

    interface TypeParameterAsReifiedArrayWarning : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = TypeParameterAsReifiedArrayWarning::class
        konst typeParameter: KtTypeParameterSymbol
    }

    interface ReifiedTypeForbiddenSubstitution : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ReifiedTypeForbiddenSubstitution::class
        konst type: KtType
    }

    interface DefinitelyNonNullableAsReified : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DefinitelyNonNullableAsReified::class
    }

    interface FinalUpperBound : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = FinalUpperBound::class
        konst type: KtType
    }

    interface UpperBoundIsExtensionFunctionType : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = UpperBoundIsExtensionFunctionType::class
    }

    interface BoundsNotAllowedIfBoundedByTypeParameter : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = BoundsNotAllowedIfBoundedByTypeParameter::class
    }

    interface OnlyOneClassBoundAllowed : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = OnlyOneClassBoundAllowed::class
    }

    interface RepeatedBound : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = RepeatedBound::class
    }

    interface ConflictingUpperBounds : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = ConflictingUpperBounds::class
        konst typeParameter: KtTypeParameterSymbol
    }

    interface NameInConstraintIsNotATypeParameter : KtFirDiagnostic<KtSimpleNameExpression> {
        override konst diagnosticClass get() = NameInConstraintIsNotATypeParameter::class
        konst typeParameterName: Name
        konst typeParametersOwner: KtSymbol
    }

    interface BoundOnTypeAliasParameterNotAllowed : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = BoundOnTypeAliasParameterNotAllowed::class
    }

    interface ReifiedTypeParameterNoInline : KtFirDiagnostic<KtTypeParameter> {
        override konst diagnosticClass get() = ReifiedTypeParameterNoInline::class
    }

    interface TypeParametersNotAllowed : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = TypeParametersNotAllowed::class
    }

    interface TypeParameterOfPropertyNotUsedInReceiver : KtFirDiagnostic<KtTypeParameter> {
        override konst diagnosticClass get() = TypeParameterOfPropertyNotUsedInReceiver::class
    }

    interface ReturnTypeMismatch : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = ReturnTypeMismatch::class
        konst expectedType: KtType
        konst actualType: KtType
        konst targetFunction: KtSymbol
        konst isMismatchDueToNullability: Boolean
    }

    interface ImplicitNothingReturnType : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ImplicitNothingReturnType::class
    }

    interface ImplicitNothingPropertyType : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ImplicitNothingPropertyType::class
    }

    interface CyclicGenericUpperBound : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = CyclicGenericUpperBound::class
    }

    interface DeprecatedTypeParameterSyntax : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = DeprecatedTypeParameterSyntax::class
    }

    interface MisplacedTypeParameterConstraints : KtFirDiagnostic<KtTypeParameter> {
        override konst diagnosticClass get() = MisplacedTypeParameterConstraints::class
    }

    interface DynamicSupertype : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = DynamicSupertype::class
    }

    interface DynamicUpperBound : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = DynamicUpperBound::class
    }

    interface DynamicReceiverNotAllowed : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = DynamicReceiverNotAllowed::class
    }

    interface IncompatibleTypes : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = IncompatibleTypes::class
        konst typeA: KtType
        konst typeB: KtType
    }

    interface IncompatibleTypesWarning : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = IncompatibleTypesWarning::class
        konst typeA: KtType
        konst typeB: KtType
    }

    interface TypeVarianceConflictError : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = TypeVarianceConflictError::class
        konst typeParameter: KtTypeParameterSymbol
        konst typeParameterVariance: Variance
        konst variance: Variance
        konst containingType: KtType
    }

    interface TypeVarianceConflictInExpandedType : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = TypeVarianceConflictInExpandedType::class
        konst typeParameter: KtTypeParameterSymbol
        konst typeParameterVariance: Variance
        konst variance: Variance
        konst containingType: KtType
    }

    interface SmartcastImpossible : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = SmartcastImpossible::class
        konst desiredType: KtType
        konst subject: KtExpression
        konst description: String
        konst isCastToNotNull: Boolean
    }

    interface RedundantNullable : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = RedundantNullable::class
    }

    interface PlatformClassMappedToKotlin : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = PlatformClassMappedToKotlin::class
        konst kotlinClass: FqName
    }

    interface InferredTypeVariableIntoEmptyIntersectionError : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = InferredTypeVariableIntoEmptyIntersectionError::class
        konst typeVariableDescription: String
        konst incompatibleTypes: List<KtType>
        konst description: String
        konst causingTypes: String
    }

    interface InferredTypeVariableIntoEmptyIntersectionWarning : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = InferredTypeVariableIntoEmptyIntersectionWarning::class
        konst typeVariableDescription: String
        konst incompatibleTypes: List<KtType>
        konst description: String
        konst causingTypes: String
    }

    interface InferredTypeVariableIntoPossibleEmptyIntersection : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = InferredTypeVariableIntoPossibleEmptyIntersection::class
        konst typeVariableDescription: String
        konst incompatibleTypes: List<KtType>
        konst description: String
        konst causingTypes: String
    }

    interface IncorrectLeftComponentOfIntersection : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = IncorrectLeftComponentOfIntersection::class
    }

    interface IncorrectRightComponentOfIntersection : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = IncorrectRightComponentOfIntersection::class
    }

    interface NullableOnDefinitelyNotNullable : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = NullableOnDefinitelyNotNullable::class
    }

    interface ExtensionInClassReferenceNotAllowed : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = ExtensionInClassReferenceNotAllowed::class
        konst referencedDeclaration: KtCallableSymbol
    }

    interface CallableReferenceLhsNotAClass : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = CallableReferenceLhsNotAClass::class
    }

    interface CallableReferenceToAnnotationConstructor : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = CallableReferenceToAnnotationConstructor::class
    }

    interface ClassLiteralLhsNotAClass : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = ClassLiteralLhsNotAClass::class
    }

    interface NullableTypeInClassLiteralLhs : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = NullableTypeInClassLiteralLhs::class
    }

    interface ExpressionOfNullableTypeInClassLiteralLhs : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ExpressionOfNullableTypeInClassLiteralLhs::class
        konst lhsType: KtType
    }

    interface NothingToOverride : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = NothingToOverride::class
        konst declaration: KtCallableSymbol
    }

    interface CannotOverrideInvisibleMember : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = CannotOverrideInvisibleMember::class
        konst overridingMember: KtCallableSymbol
        konst baseMember: KtCallableSymbol
    }

    interface DataClassOverrideConflict : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = DataClassOverrideConflict::class
        konst overridingMember: KtCallableSymbol
        konst baseMember: KtCallableSymbol
    }

    interface CannotWeakenAccessPrivilege : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = CannotWeakenAccessPrivilege::class
        konst overridingVisibility: Visibility
        konst overridden: KtCallableSymbol
        konst containingClassName: Name
    }

    interface CannotChangeAccessPrivilege : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = CannotChangeAccessPrivilege::class
        konst overridingVisibility: Visibility
        konst overridden: KtCallableSymbol
        konst containingClassName: Name
    }

    interface OverridingFinalMember : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = OverridingFinalMember::class
        konst overriddenDeclaration: KtCallableSymbol
        konst containingClassName: Name
    }

    interface ReturnTypeMismatchOnInheritance : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = ReturnTypeMismatchOnInheritance::class
        konst conflictingDeclaration1: KtCallableSymbol
        konst conflictingDeclaration2: KtCallableSymbol
    }

    interface PropertyTypeMismatchOnInheritance : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = PropertyTypeMismatchOnInheritance::class
        konst conflictingDeclaration1: KtCallableSymbol
        konst conflictingDeclaration2: KtCallableSymbol
    }

    interface VarTypeMismatchOnInheritance : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = VarTypeMismatchOnInheritance::class
        konst conflictingDeclaration1: KtCallableSymbol
        konst conflictingDeclaration2: KtCallableSymbol
    }

    interface ReturnTypeMismatchByDelegation : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = ReturnTypeMismatchByDelegation::class
        konst delegateDeclaration: KtCallableSymbol
        konst baseDeclaration: KtCallableSymbol
    }

    interface PropertyTypeMismatchByDelegation : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = PropertyTypeMismatchByDelegation::class
        konst delegateDeclaration: KtCallableSymbol
        konst baseDeclaration: KtCallableSymbol
    }

    interface VarOverriddenByValByDelegation : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = VarOverriddenByValByDelegation::class
        konst delegateDeclaration: KtCallableSymbol
        konst baseDeclaration: KtCallableSymbol
    }

    interface ConflictingInheritedMembers : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = ConflictingInheritedMembers::class
        konst owner: KtClassLikeSymbol
        konst conflictingDeclarations: List<KtCallableSymbol>
    }

    interface AbstractMemberNotImplemented : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = AbstractMemberNotImplemented::class
        konst classOrObject: KtClassLikeSymbol
        konst missingDeclaration: KtCallableSymbol
    }

    interface AbstractMemberNotImplementedByEnumEntry : KtFirDiagnostic<KtEnumEntry> {
        override konst diagnosticClass get() = AbstractMemberNotImplementedByEnumEntry::class
        konst enumEntry: KtSymbol
        konst missingDeclarations: List<KtCallableSymbol>
    }

    interface AbstractClassMemberNotImplemented : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = AbstractClassMemberNotImplemented::class
        konst classOrObject: KtClassLikeSymbol
        konst missingDeclaration: KtCallableSymbol
    }

    interface InvisibleAbstractMemberFromSuperError : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = InvisibleAbstractMemberFromSuperError::class
        konst classOrObject: KtClassLikeSymbol
        konst invisibleDeclaration: KtCallableSymbol
    }

    interface InvisibleAbstractMemberFromSuperWarning : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = InvisibleAbstractMemberFromSuperWarning::class
        konst classOrObject: KtClassLikeSymbol
        konst invisibleDeclaration: KtCallableSymbol
    }

    interface AmbiguousAnonymousTypeInferred : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = AmbiguousAnonymousTypeInferred::class
        konst superTypes: List<KtType>
    }

    interface ManyImplMemberNotImplemented : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = ManyImplMemberNotImplemented::class
        konst classOrObject: KtClassLikeSymbol
        konst missingDeclaration: KtCallableSymbol
    }

    interface ManyInterfacesMemberNotImplemented : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = ManyInterfacesMemberNotImplemented::class
        konst classOrObject: KtClassLikeSymbol
        konst missingDeclaration: KtCallableSymbol
    }

    interface OverridingFinalMemberByDelegation : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = OverridingFinalMemberByDelegation::class
        konst delegatedDeclaration: KtCallableSymbol
        konst overriddenDeclaration: KtCallableSymbol
    }

    interface DelegatedMemberHidesSupertypeOverride : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = DelegatedMemberHidesSupertypeOverride::class
        konst delegatedDeclaration: KtCallableSymbol
        konst overriddenDeclaration: KtCallableSymbol
    }

    interface ReturnTypeMismatchOnOverride : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = ReturnTypeMismatchOnOverride::class
        konst function: KtCallableSymbol
        konst superFunction: KtCallableSymbol
    }

    interface PropertyTypeMismatchOnOverride : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = PropertyTypeMismatchOnOverride::class
        konst property: KtCallableSymbol
        konst superProperty: KtCallableSymbol
    }

    interface VarTypeMismatchOnOverride : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = VarTypeMismatchOnOverride::class
        konst variable: KtCallableSymbol
        konst superVariable: KtCallableSymbol
    }

    interface VarOverriddenByVal : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = VarOverriddenByVal::class
        konst overridingDeclaration: KtCallableSymbol
        konst overriddenDeclaration: KtCallableSymbol
    }

    interface VarImplementedByInheritedValError : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = VarImplementedByInheritedValError::class
        konst classOrObject: KtClassLikeSymbol
        konst overridingDeclaration: KtCallableSymbol
        konst overriddenDeclaration: KtCallableSymbol
    }

    interface VarImplementedByInheritedValWarning : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = VarImplementedByInheritedValWarning::class
        konst classOrObject: KtClassLikeSymbol
        konst overridingDeclaration: KtCallableSymbol
        konst overriddenDeclaration: KtCallableSymbol
    }

    interface NonFinalMemberInFinalClass : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = NonFinalMemberInFinalClass::class
    }

    interface NonFinalMemberInObject : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = NonFinalMemberInObject::class
    }

    interface VirtualMemberHidden : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = VirtualMemberHidden::class
        konst declared: KtCallableSymbol
        konst overriddenContainer: KtClassLikeSymbol
    }

    interface ManyCompanionObjects : KtFirDiagnostic<KtObjectDeclaration> {
        override konst diagnosticClass get() = ManyCompanionObjects::class
    }

    interface ConflictingOverloads : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ConflictingOverloads::class
        konst conflictingOverloads: List<KtSymbol>
    }

    interface Redeclaration : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = Redeclaration::class
        konst conflictingDeclarations: List<KtSymbol>
    }

    interface PackageOrClassifierRedeclaration : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = PackageOrClassifierRedeclaration::class
        konst conflictingDeclarations: List<KtSymbol>
    }

    interface MethodOfAnyImplementedInInterface : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = MethodOfAnyImplementedInInterface::class
    }

    interface LocalObjectNotAllowed : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = LocalObjectNotAllowed::class
        konst objectName: Name
    }

    interface LocalInterfaceNotAllowed : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = LocalInterfaceNotAllowed::class
        konst interfaceName: Name
    }

    interface AbstractFunctionInNonAbstractClass : KtFirDiagnostic<KtFunction> {
        override konst diagnosticClass get() = AbstractFunctionInNonAbstractClass::class
        konst function: KtCallableSymbol
        konst containingClass: KtClassLikeSymbol
    }

    interface AbstractFunctionWithBody : KtFirDiagnostic<KtFunction> {
        override konst diagnosticClass get() = AbstractFunctionWithBody::class
        konst function: KtCallableSymbol
    }

    interface NonAbstractFunctionWithNoBody : KtFirDiagnostic<KtFunction> {
        override konst diagnosticClass get() = NonAbstractFunctionWithNoBody::class
        konst function: KtCallableSymbol
    }

    interface PrivateFunctionWithNoBody : KtFirDiagnostic<KtFunction> {
        override konst diagnosticClass get() = PrivateFunctionWithNoBody::class
        konst function: KtCallableSymbol
    }

    interface NonMemberFunctionNoBody : KtFirDiagnostic<KtFunction> {
        override konst diagnosticClass get() = NonMemberFunctionNoBody::class
        konst function: KtCallableSymbol
    }

    interface FunctionDeclarationWithNoName : KtFirDiagnostic<KtFunction> {
        override konst diagnosticClass get() = FunctionDeclarationWithNoName::class
    }

    interface AnonymousFunctionWithName : KtFirDiagnostic<KtFunction> {
        override konst diagnosticClass get() = AnonymousFunctionWithName::class
    }

    interface AnonymousFunctionParameterWithDefaultValue : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = AnonymousFunctionParameterWithDefaultValue::class
    }

    interface UselessVarargOnParameter : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = UselessVarargOnParameter::class
    }

    interface MultipleVarargParameters : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = MultipleVarargParameters::class
    }

    interface ForbiddenVarargParameterType : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = ForbiddenVarargParameterType::class
        konst varargParameterType: KtType
    }

    interface ValueParameterWithNoTypeAnnotation : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = ValueParameterWithNoTypeAnnotation::class
    }

    interface CannotInferParameterType : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = CannotInferParameterType::class
    }

    interface NoTailCallsFound : KtFirDiagnostic<KtNamedFunction> {
        override konst diagnosticClass get() = NoTailCallsFound::class
    }

    interface TailrecOnVirtualMemberError : KtFirDiagnostic<KtNamedFunction> {
        override konst diagnosticClass get() = TailrecOnVirtualMemberError::class
    }

    interface NonTailRecursiveCall : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NonTailRecursiveCall::class
    }

    interface TailRecursionInTryIsNotSupported : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = TailRecursionInTryIsNotSupported::class
    }

    interface DataObjectCustomEqualsOrHashCode : KtFirDiagnostic<KtNamedFunction> {
        override konst diagnosticClass get() = DataObjectCustomEqualsOrHashCode::class
    }

    interface FunInterfaceConstructorReference : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = FunInterfaceConstructorReference::class
    }

    interface FunInterfaceWrongCountOfAbstractMembers : KtFirDiagnostic<KtClass> {
        override konst diagnosticClass get() = FunInterfaceWrongCountOfAbstractMembers::class
    }

    interface FunInterfaceCannotHaveAbstractProperties : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = FunInterfaceCannotHaveAbstractProperties::class
    }

    interface FunInterfaceAbstractMethodWithTypeParameters : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = FunInterfaceAbstractMethodWithTypeParameters::class
    }

    interface FunInterfaceAbstractMethodWithDefaultValue : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = FunInterfaceAbstractMethodWithDefaultValue::class
    }

    interface FunInterfaceWithSuspendFunction : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = FunInterfaceWithSuspendFunction::class
    }

    interface AbstractPropertyInNonAbstractClass : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = AbstractPropertyInNonAbstractClass::class
        konst property: KtCallableSymbol
        konst containingClass: KtClassLikeSymbol
    }

    interface PrivatePropertyInInterface : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = PrivatePropertyInInterface::class
    }

    interface AbstractPropertyWithInitializer : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = AbstractPropertyWithInitializer::class
    }

    interface PropertyInitializerInInterface : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = PropertyInitializerInInterface::class
    }

    interface PropertyWithNoTypeNoInitializer : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = PropertyWithNoTypeNoInitializer::class
    }

    interface MustBeInitialized : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = MustBeInitialized::class
    }

    interface MustBeInitializedWarning : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = MustBeInitializedWarning::class
    }

    interface MustBeInitializedOrBeFinal : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = MustBeInitializedOrBeFinal::class
    }

    interface MustBeInitializedOrBeFinalWarning : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = MustBeInitializedOrBeFinalWarning::class
    }

    interface MustBeInitializedOrBeAbstract : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = MustBeInitializedOrBeAbstract::class
    }

    interface MustBeInitializedOrBeAbstractWarning : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = MustBeInitializedOrBeAbstractWarning::class
    }

    interface MustBeInitializedOrFinalOrAbstract : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = MustBeInitializedOrFinalOrAbstract::class
    }

    interface MustBeInitializedOrFinalOrAbstractWarning : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = MustBeInitializedOrFinalOrAbstractWarning::class
    }

    interface ExtensionPropertyMustHaveAccessorsOrBeAbstract : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = ExtensionPropertyMustHaveAccessorsOrBeAbstract::class
    }

    interface UnnecessaryLateinit : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = UnnecessaryLateinit::class
    }

    interface BackingFieldInInterface : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = BackingFieldInInterface::class
    }

    interface ExtensionPropertyWithBackingField : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = ExtensionPropertyWithBackingField::class
    }

    interface PropertyInitializerNoBackingField : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = PropertyInitializerNoBackingField::class
    }

    interface AbstractDelegatedProperty : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = AbstractDelegatedProperty::class
    }

    interface DelegatedPropertyInInterface : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = DelegatedPropertyInInterface::class
    }

    interface AbstractPropertyWithGetter : KtFirDiagnostic<KtPropertyAccessor> {
        override konst diagnosticClass get() = AbstractPropertyWithGetter::class
    }

    interface AbstractPropertyWithSetter : KtFirDiagnostic<KtPropertyAccessor> {
        override konst diagnosticClass get() = AbstractPropertyWithSetter::class
    }

    interface PrivateSetterForAbstractProperty : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = PrivateSetterForAbstractProperty::class
    }

    interface PrivateSetterForOpenProperty : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = PrivateSetterForOpenProperty::class
    }

    interface ValWithSetter : KtFirDiagnostic<KtPropertyAccessor> {
        override konst diagnosticClass get() = ValWithSetter::class
    }

    interface ConstValNotTopLevelOrObject : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ConstValNotTopLevelOrObject::class
    }

    interface ConstValWithGetter : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ConstValWithGetter::class
    }

    interface ConstValWithDelegate : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = ConstValWithDelegate::class
    }

    interface TypeCantBeUsedForConstVal : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = TypeCantBeUsedForConstVal::class
        konst constValType: KtType
    }

    interface ConstValWithoutInitializer : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = ConstValWithoutInitializer::class
    }

    interface ConstValWithNonConstInitializer : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = ConstValWithNonConstInitializer::class
    }

    interface WrongSetterParameterType : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = WrongSetterParameterType::class
        konst expectedType: KtType
        konst actualType: KtType
    }

    interface DelegateUsesExtensionPropertyTypeParameterError : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = DelegateUsesExtensionPropertyTypeParameterError::class
        konst usedTypeParameter: KtTypeParameterSymbol
    }

    interface DelegateUsesExtensionPropertyTypeParameterWarning : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = DelegateUsesExtensionPropertyTypeParameterWarning::class
        konst usedTypeParameter: KtTypeParameterSymbol
    }

    interface InitializerTypeMismatch : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = InitializerTypeMismatch::class
        konst expectedType: KtType
        konst actualType: KtType
        konst isMismatchDueToNullability: Boolean
    }

    interface GetterVisibilityDiffersFromPropertyVisibility : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = GetterVisibilityDiffersFromPropertyVisibility::class
    }

    interface SetterVisibilityInconsistentWithPropertyVisibility : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = SetterVisibilityInconsistentWithPropertyVisibility::class
    }

    interface WrongSetterReturnType : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = WrongSetterReturnType::class
    }

    interface WrongGetterReturnType : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = WrongGetterReturnType::class
        konst expectedType: KtType
        konst actualType: KtType
    }

    interface AccessorForDelegatedProperty : KtFirDiagnostic<KtPropertyAccessor> {
        override konst diagnosticClass get() = AccessorForDelegatedProperty::class
    }

    interface PropertyInitializerWithExplicitFieldDeclaration : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = PropertyInitializerWithExplicitFieldDeclaration::class
    }

    interface PropertyFieldDeclarationMissingInitializer : KtFirDiagnostic<KtBackingField> {
        override konst diagnosticClass get() = PropertyFieldDeclarationMissingInitializer::class
    }

    interface LateinitPropertyFieldDeclarationWithInitializer : KtFirDiagnostic<KtBackingField> {
        override konst diagnosticClass get() = LateinitPropertyFieldDeclarationWithInitializer::class
    }

    interface LateinitFieldInValProperty : KtFirDiagnostic<KtBackingField> {
        override konst diagnosticClass get() = LateinitFieldInValProperty::class
    }

    interface LateinitNullableBackingField : KtFirDiagnostic<KtBackingField> {
        override konst diagnosticClass get() = LateinitNullableBackingField::class
    }

    interface BackingFieldForDelegatedProperty : KtFirDiagnostic<KtBackingField> {
        override konst diagnosticClass get() = BackingFieldForDelegatedProperty::class
    }

    interface PropertyMustHaveGetter : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = PropertyMustHaveGetter::class
    }

    interface PropertyMustHaveSetter : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = PropertyMustHaveSetter::class
    }

    interface ExplicitBackingFieldInInterface : KtFirDiagnostic<KtBackingField> {
        override konst diagnosticClass get() = ExplicitBackingFieldInInterface::class
    }

    interface ExplicitBackingFieldInAbstractProperty : KtFirDiagnostic<KtBackingField> {
        override konst diagnosticClass get() = ExplicitBackingFieldInAbstractProperty::class
    }

    interface ExplicitBackingFieldInExtension : KtFirDiagnostic<KtBackingField> {
        override konst diagnosticClass get() = ExplicitBackingFieldInExtension::class
    }

    interface RedundantExplicitBackingField : KtFirDiagnostic<KtBackingField> {
        override konst diagnosticClass get() = RedundantExplicitBackingField::class
    }

    interface AbstractPropertyInPrimaryConstructorParameters : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = AbstractPropertyInPrimaryConstructorParameters::class
    }

    interface LocalVariableWithTypeParametersWarning : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = LocalVariableWithTypeParametersWarning::class
    }

    interface LocalVariableWithTypeParameters : KtFirDiagnostic<KtProperty> {
        override konst diagnosticClass get() = LocalVariableWithTypeParameters::class
    }

    interface ExplicitTypeArgumentsInPropertyAccess : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = ExplicitTypeArgumentsInPropertyAccess::class
    }

    interface LateinitIntrinsicCallOnNonLiteral : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = LateinitIntrinsicCallOnNonLiteral::class
    }

    interface LateinitIntrinsicCallOnNonLateinit : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = LateinitIntrinsicCallOnNonLateinit::class
    }

    interface LateinitIntrinsicCallInInlineFunction : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = LateinitIntrinsicCallInInlineFunction::class
    }

    interface LateinitIntrinsicCallOnNonAccessibleProperty : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = LateinitIntrinsicCallOnNonAccessibleProperty::class
        konst declaration: KtSymbol
    }

    interface LocalExtensionProperty : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = LocalExtensionProperty::class
    }

    interface ExpectedDeclarationWithBody : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = ExpectedDeclarationWithBody::class
    }

    interface ExpectedClassConstructorDelegationCall : KtFirDiagnostic<KtConstructorDelegationCall> {
        override konst diagnosticClass get() = ExpectedClassConstructorDelegationCall::class
    }

    interface ExpectedClassConstructorPropertyParameter : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = ExpectedClassConstructorPropertyParameter::class
    }

    interface ExpectedEnumConstructor : KtFirDiagnostic<KtConstructor<*>> {
        override konst diagnosticClass get() = ExpectedEnumConstructor::class
    }

    interface ExpectedEnumEntryWithBody : KtFirDiagnostic<KtEnumEntry> {
        override konst diagnosticClass get() = ExpectedEnumEntryWithBody::class
    }

    interface ExpectedPropertyInitializer : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = ExpectedPropertyInitializer::class
    }

    interface ExpectedDelegatedProperty : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = ExpectedDelegatedProperty::class
    }

    interface ExpectedLateinitProperty : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = ExpectedLateinitProperty::class
    }

    interface SupertypeInitializedInExpectedClass : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = SupertypeInitializedInExpectedClass::class
    }

    interface ExpectedPrivateDeclaration : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = ExpectedPrivateDeclaration::class
    }

    interface ExpectedExternalDeclaration : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = ExpectedExternalDeclaration::class
    }

    interface ExpectedTailrecFunction : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = ExpectedTailrecFunction::class
    }

    interface ImplementationByDelegationInExpectClass : KtFirDiagnostic<KtDelegatedSuperTypeEntry> {
        override konst diagnosticClass get() = ImplementationByDelegationInExpectClass::class
    }

    interface ActualTypeAliasNotToClass : KtFirDiagnostic<KtTypeAlias> {
        override konst diagnosticClass get() = ActualTypeAliasNotToClass::class
    }

    interface ActualTypeAliasToClassWithDeclarationSiteVariance : KtFirDiagnostic<KtTypeAlias> {
        override konst diagnosticClass get() = ActualTypeAliasToClassWithDeclarationSiteVariance::class
    }

    interface ActualTypeAliasWithUseSiteVariance : KtFirDiagnostic<KtTypeAlias> {
        override konst diagnosticClass get() = ActualTypeAliasWithUseSiteVariance::class
    }

    interface ActualTypeAliasWithComplexSubstitution : KtFirDiagnostic<KtTypeAlias> {
        override konst diagnosticClass get() = ActualTypeAliasWithComplexSubstitution::class
    }

    interface ActualFunctionWithDefaultArguments : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ActualFunctionWithDefaultArguments::class
    }

    interface DefaultArgumentsInExpectWithActualTypealias : KtFirDiagnostic<KtTypeAlias> {
        override konst diagnosticClass get() = DefaultArgumentsInExpectWithActualTypealias::class
        konst expectClassSymbol: KtClassLikeSymbol
        konst members: List<KtCallableSymbol>
    }

    interface ActualAnnotationConflictingDefaultArgumentValue : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ActualAnnotationConflictingDefaultArgumentValue::class
        konst parameter: KtVariableLikeSymbol
    }

    interface ExpectedFunctionSourceWithDefaultArgumentsNotFound : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ExpectedFunctionSourceWithDefaultArgumentsNotFound::class
    }

    interface NoActualForExpect : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = NoActualForExpect::class
        konst declaration: KtSymbol
        konst module: FirModuleData
        konst compatibility: Map<ExpectActualCompatibility<FirBasedSymbol<*>>, List<KtSymbol>>
    }

    interface ActualWithoutExpect : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = ActualWithoutExpect::class
        konst declaration: KtSymbol
        konst compatibility: Map<ExpectActualCompatibility<FirBasedSymbol<*>>, List<KtSymbol>>
    }

    interface AmbiguousActuals : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = AmbiguousActuals::class
        konst declaration: KtSymbol
        konst candidates: List<KtSymbol>
    }

    interface AmbiguousExpects : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = AmbiguousExpects::class
        konst declaration: KtSymbol
        konst modules: List<FirModuleData>
    }

    interface NoActualClassMemberForExpectedClass : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = NoActualClassMemberForExpectedClass::class
        konst declaration: KtSymbol
        konst members: List<Pair<KtSymbol, Map<Incompatible<FirBasedSymbol<*>>, List<KtSymbol>>>>
    }

    interface ActualMissing : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = ActualMissing::class
    }

    interface InitializerRequiredForDestructuringDeclaration : KtFirDiagnostic<KtDestructuringDeclaration> {
        override konst diagnosticClass get() = InitializerRequiredForDestructuringDeclaration::class
    }

    interface ComponentFunctionMissing : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ComponentFunctionMissing::class
        konst missingFunctionName: Name
        konst destructingType: KtType
    }

    interface ComponentFunctionAmbiguity : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ComponentFunctionAmbiguity::class
        konst functionWithAmbiguityName: Name
        konst candidates: List<KtSymbol>
    }

    interface ComponentFunctionOnNullable : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = ComponentFunctionOnNullable::class
        konst componentFunctionName: Name
    }

    interface ComponentFunctionReturnTypeMismatch : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = ComponentFunctionReturnTypeMismatch::class
        konst componentFunctionName: Name
        konst destructingType: KtType
        konst expectedType: KtType
    }

    interface UninitializedVariable : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = UninitializedVariable::class
        konst variable: KtVariableSymbol
    }

    interface UninitializedParameter : KtFirDiagnostic<KtSimpleNameExpression> {
        override konst diagnosticClass get() = UninitializedParameter::class
        konst parameter: KtSymbol
    }

    interface UninitializedEnumEntry : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = UninitializedEnumEntry::class
        konst enumEntry: KtSymbol
    }

    interface UninitializedEnumCompanion : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = UninitializedEnumCompanion::class
        konst enumClass: KtClassLikeSymbol
    }

    interface ValReassignment : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = ValReassignment::class
        konst variable: KtVariableLikeSymbol
    }

    interface ValReassignmentViaBackingFieldError : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = ValReassignmentViaBackingFieldError::class
        konst property: KtVariableSymbol
    }

    interface ValReassignmentViaBackingFieldWarning : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = ValReassignmentViaBackingFieldWarning::class
        konst property: KtVariableSymbol
    }

    interface CapturedValInitialization : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = CapturedValInitialization::class
        konst property: KtVariableSymbol
    }

    interface CapturedMemberValInitialization : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = CapturedMemberValInitialization::class
        konst property: KtVariableSymbol
    }

    interface SetterProjectedOut : KtFirDiagnostic<KtBinaryExpression> {
        override konst diagnosticClass get() = SetterProjectedOut::class
        konst property: KtVariableSymbol
    }

    interface WrongInvocationKind : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = WrongInvocationKind::class
        konst declaration: KtSymbol
        konst requiredRange: EventOccurrencesRange
        konst actualRange: EventOccurrencesRange
    }

    interface LeakedInPlaceLambda : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = LeakedInPlaceLambda::class
        konst lambda: KtSymbol
    }

    interface WrongImpliesCondition : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = WrongImpliesCondition::class
    }

    interface VariableWithNoTypeNoInitializer : KtFirDiagnostic<KtVariableDeclaration> {
        override konst diagnosticClass get() = VariableWithNoTypeNoInitializer::class
    }

    interface InitializationBeforeDeclaration : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = InitializationBeforeDeclaration::class
        konst property: KtSymbol
    }

    interface UnreachableCode : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = UnreachableCode::class
        konst reachable: List<PsiElement>
        konst unreachable: List<PsiElement>
    }

    interface SenselessComparison : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = SenselessComparison::class
        konst expression: KtExpression
        konst compareResult: Boolean
    }

    interface SenselessNullInWhen : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = SenselessNullInWhen::class
    }

    interface TypecheckerHasRunIntoRecursiveProblem : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = TypecheckerHasRunIntoRecursiveProblem::class
    }

    interface UnsafeCall : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = UnsafeCall::class
        konst receiverType: KtType
        konst receiverExpression: KtExpression?
    }

    interface UnsafeImplicitInvokeCall : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = UnsafeImplicitInvokeCall::class
        konst receiverType: KtType
    }

    interface UnsafeInfixCall : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = UnsafeInfixCall::class
        konst receiverExpression: KtExpression
        konst operator: String
        konst argumentExpression: KtExpression
    }

    interface UnsafeOperatorCall : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = UnsafeOperatorCall::class
        konst receiverExpression: KtExpression
        konst operator: String
        konst argumentExpression: KtExpression
    }

    interface IteratorOnNullable : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = IteratorOnNullable::class
    }

    interface UnnecessarySafeCall : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = UnnecessarySafeCall::class
        konst receiverType: KtType
    }

    interface SafeCallWillChangeNullability : KtFirDiagnostic<KtSafeQualifiedExpression> {
        override konst diagnosticClass get() = SafeCallWillChangeNullability::class
    }

    interface UnexpectedSafeCall : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = UnexpectedSafeCall::class
    }

    interface UnnecessaryNotNullAssertion : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = UnnecessaryNotNullAssertion::class
        konst receiverType: KtType
    }

    interface NotNullAssertionOnLambdaExpression : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = NotNullAssertionOnLambdaExpression::class
    }

    interface NotNullAssertionOnCallableReference : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = NotNullAssertionOnCallableReference::class
    }

    interface UselessElvis : KtFirDiagnostic<KtBinaryExpression> {
        override konst diagnosticClass get() = UselessElvis::class
        konst receiverType: KtType
    }

    interface UselessElvisRightIsNull : KtFirDiagnostic<KtBinaryExpression> {
        override konst diagnosticClass get() = UselessElvisRightIsNull::class
    }

    interface CannotCheckForErased : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = CannotCheckForErased::class
        konst type: KtType
    }

    interface CastNeverSucceeds : KtFirDiagnostic<KtBinaryExpressionWithTypeRHS> {
        override konst diagnosticClass get() = CastNeverSucceeds::class
    }

    interface UselessCast : KtFirDiagnostic<KtBinaryExpressionWithTypeRHS> {
        override konst diagnosticClass get() = UselessCast::class
    }

    interface UncheckedCast : KtFirDiagnostic<KtBinaryExpressionWithTypeRHS> {
        override konst diagnosticClass get() = UncheckedCast::class
        konst originalType: KtType
        konst targetType: KtType
    }

    interface UselessIsCheck : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = UselessIsCheck::class
        konst compileTimeCheckResult: Boolean
    }

    interface IsEnumEntry : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = IsEnumEntry::class
    }

    interface EnumEntryAsType : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = EnumEntryAsType::class
    }

    interface ExpectedCondition : KtFirDiagnostic<KtWhenCondition> {
        override konst diagnosticClass get() = ExpectedCondition::class
    }

    interface NoElseInWhen : KtFirDiagnostic<KtWhenExpression> {
        override konst diagnosticClass get() = NoElseInWhen::class
        konst missingWhenCases: List<WhenMissingCase>
    }

    interface NonExhaustiveWhenStatement : KtFirDiagnostic<KtWhenExpression> {
        override konst diagnosticClass get() = NonExhaustiveWhenStatement::class
        konst type: String
        konst missingWhenCases: List<WhenMissingCase>
    }

    interface InkonstidIfAsExpression : KtFirDiagnostic<KtIfExpression> {
        override konst diagnosticClass get() = InkonstidIfAsExpression::class
    }

    interface ElseMisplacedInWhen : KtFirDiagnostic<KtWhenEntry> {
        override konst diagnosticClass get() = ElseMisplacedInWhen::class
    }

    interface IllegalDeclarationInWhenSubject : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = IllegalDeclarationInWhenSubject::class
        konst illegalReason: String
    }

    interface CommaInWhenConditionWithoutArgument : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = CommaInWhenConditionWithoutArgument::class
    }

    interface DuplicateLabelInWhen : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = DuplicateLabelInWhen::class
    }

    interface ConfusingBranchConditionError : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ConfusingBranchConditionError::class
    }

    interface ConfusingBranchConditionWarning : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ConfusingBranchConditionWarning::class
    }

    interface TypeParameterIsNotAnExpression : KtFirDiagnostic<KtSimpleNameExpression> {
        override konst diagnosticClass get() = TypeParameterIsNotAnExpression::class
        konst typeParameter: KtTypeParameterSymbol
    }

    interface TypeParameterOnLhsOfDot : KtFirDiagnostic<KtSimpleNameExpression> {
        override konst diagnosticClass get() = TypeParameterOnLhsOfDot::class
        konst typeParameter: KtTypeParameterSymbol
    }

    interface NoCompanionObject : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = NoCompanionObject::class
        konst klass: KtClassLikeSymbol
    }

    interface ExpressionExpectedPackageFound : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = ExpressionExpectedPackageFound::class
    }

    interface ErrorInContractDescription : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ErrorInContractDescription::class
        konst reason: String
    }

    interface ContractNotAllowed : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ContractNotAllowed::class
        konst reason: String
    }

    interface NoGetMethod : KtFirDiagnostic<KtArrayAccessExpression> {
        override konst diagnosticClass get() = NoGetMethod::class
    }

    interface NoSetMethod : KtFirDiagnostic<KtArrayAccessExpression> {
        override konst diagnosticClass get() = NoSetMethod::class
    }

    interface IteratorMissing : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = IteratorMissing::class
    }

    interface HasNextMissing : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = HasNextMissing::class
    }

    interface NextMissing : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = NextMissing::class
    }

    interface HasNextFunctionNoneApplicable : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = HasNextFunctionNoneApplicable::class
        konst candidates: List<KtSymbol>
    }

    interface NextNoneApplicable : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = NextNoneApplicable::class
        konst candidates: List<KtSymbol>
    }

    interface DelegateSpecialFunctionMissing : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = DelegateSpecialFunctionMissing::class
        konst expectedFunctionSignature: String
        konst delegateType: KtType
        konst description: String
    }

    interface DelegateSpecialFunctionAmbiguity : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = DelegateSpecialFunctionAmbiguity::class
        konst expectedFunctionSignature: String
        konst candidates: List<KtSymbol>
    }

    interface DelegateSpecialFunctionNoneApplicable : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = DelegateSpecialFunctionNoneApplicable::class
        konst expectedFunctionSignature: String
        konst candidates: List<KtSymbol>
    }

    interface DelegateSpecialFunctionReturnTypeMismatch : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = DelegateSpecialFunctionReturnTypeMismatch::class
        konst delegateFunction: String
        konst expectedType: KtType
        konst actualType: KtType
    }

    interface UnderscoreIsReserved : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = UnderscoreIsReserved::class
    }

    interface UnderscoreUsageWithoutBackticks : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = UnderscoreUsageWithoutBackticks::class
    }

    interface ResolvedToUnderscoreNamedCatchParameter : KtFirDiagnostic<KtNameReferenceExpression> {
        override konst diagnosticClass get() = ResolvedToUnderscoreNamedCatchParameter::class
    }

    interface InkonstidCharacters : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = InkonstidCharacters::class
        konst message: String
    }

    interface DangerousCharacters : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = DangerousCharacters::class
        konst characters: String
    }

    interface EqualityNotApplicable : KtFirDiagnostic<KtBinaryExpression> {
        override konst diagnosticClass get() = EqualityNotApplicable::class
        konst operator: String
        konst leftType: KtType
        konst rightType: KtType
    }

    interface EqualityNotApplicableWarning : KtFirDiagnostic<KtBinaryExpression> {
        override konst diagnosticClass get() = EqualityNotApplicableWarning::class
        konst operator: String
        konst leftType: KtType
        konst rightType: KtType
    }

    interface IncompatibleEnumComparisonError : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = IncompatibleEnumComparisonError::class
        konst leftType: KtType
        konst rightType: KtType
    }

    interface IncompatibleEnumComparison : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = IncompatibleEnumComparison::class
        konst leftType: KtType
        konst rightType: KtType
    }

    interface ForbiddenIdentityEquals : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ForbiddenIdentityEquals::class
        konst leftType: KtType
        konst rightType: KtType
    }

    interface ForbiddenIdentityEqualsWarning : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ForbiddenIdentityEqualsWarning::class
        konst leftType: KtType
        konst rightType: KtType
    }

    interface DeprecatedIdentityEquals : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = DeprecatedIdentityEquals::class
        konst leftType: KtType
        konst rightType: KtType
    }

    interface ImplicitBoxingInIdentityEquals : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ImplicitBoxingInIdentityEquals::class
        konst leftType: KtType
        konst rightType: KtType
    }

    interface IncDecShouldNotReturnUnit : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = IncDecShouldNotReturnUnit::class
    }

    interface AssignmentOperatorShouldReturnUnit : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = AssignmentOperatorShouldReturnUnit::class
        konst functionSymbol: KtFunctionLikeSymbol
        konst operator: String
    }

    interface PropertyAsOperator : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = PropertyAsOperator::class
        konst property: KtVariableSymbol
    }

    interface DslScopeViolation : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DslScopeViolation::class
        konst calleeSymbol: KtSymbol
    }

    interface ToplevelTypealiasesOnly : KtFirDiagnostic<KtTypeAlias> {
        override konst diagnosticClass get() = ToplevelTypealiasesOnly::class
    }

    interface RecursiveTypealiasExpansion : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = RecursiveTypealiasExpansion::class
    }

    interface TypealiasShouldExpandToClass : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = TypealiasShouldExpandToClass::class
        konst expandedType: KtType
    }

    interface RedundantVisibilityModifier : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = RedundantVisibilityModifier::class
    }

    interface RedundantModalityModifier : KtFirDiagnostic<KtModifierListOwner> {
        override konst diagnosticClass get() = RedundantModalityModifier::class
    }

    interface RedundantReturnUnitType : KtFirDiagnostic<KtTypeReference> {
        override konst diagnosticClass get() = RedundantReturnUnitType::class
    }

    interface RedundantExplicitType : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = RedundantExplicitType::class
    }

    interface RedundantSingleExpressionStringTemplate : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = RedundantSingleExpressionStringTemplate::class
    }

    interface CanBeVal : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = CanBeVal::class
    }

    interface CanBeReplacedWithOperatorAssignment : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = CanBeReplacedWithOperatorAssignment::class
    }

    interface RedundantCallOfConversionMethod : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = RedundantCallOfConversionMethod::class
    }

    interface ArrayEqualityOperatorCanBeReplacedWithEquals : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = ArrayEqualityOperatorCanBeReplacedWithEquals::class
    }

    interface EmptyRange : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = EmptyRange::class
    }

    interface RedundantSetterParameterType : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = RedundantSetterParameterType::class
    }

    interface UnusedVariable : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = UnusedVariable::class
    }

    interface AssignedValueIsNeverRead : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = AssignedValueIsNeverRead::class
    }

    interface VariableInitializerIsRedundant : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = VariableInitializerIsRedundant::class
    }

    interface VariableNeverRead : KtFirDiagnostic<KtNamedDeclaration> {
        override konst diagnosticClass get() = VariableNeverRead::class
    }

    interface UselessCallOnNotNull : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = UselessCallOnNotNull::class
    }

    interface ReturnNotAllowed : KtFirDiagnostic<KtReturnExpression> {
        override konst diagnosticClass get() = ReturnNotAllowed::class
    }

    interface NotAFunctionLabel : KtFirDiagnostic<KtReturnExpression> {
        override konst diagnosticClass get() = NotAFunctionLabel::class
    }

    interface ReturnInFunctionWithExpressionBody : KtFirDiagnostic<KtReturnExpression> {
        override konst diagnosticClass get() = ReturnInFunctionWithExpressionBody::class
    }

    interface NoReturnInFunctionWithBlockBody : KtFirDiagnostic<KtDeclarationWithBody> {
        override konst diagnosticClass get() = NoReturnInFunctionWithBlockBody::class
    }

    interface AnonymousInitializerInInterface : KtFirDiagnostic<KtAnonymousInitializer> {
        override konst diagnosticClass get() = AnonymousInitializerInInterface::class
    }

    interface UsageIsNotInlinable : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = UsageIsNotInlinable::class
        konst parameter: KtSymbol
    }

    interface NonLocalReturnNotAllowed : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = NonLocalReturnNotAllowed::class
        konst parameter: KtSymbol
    }

    interface NotYetSupportedInInline : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = NotYetSupportedInInline::class
        konst message: String
    }

    interface NothingToInline : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = NothingToInline::class
    }

    interface NullableInlineParameter : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = NullableInlineParameter::class
        konst parameter: KtSymbol
        konst function: KtSymbol
    }

    interface RecursionInInline : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = RecursionInInline::class
        konst symbol: KtSymbol
    }

    interface NonPublicCallFromPublicInline : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = NonPublicCallFromPublicInline::class
        konst inlineDeclaration: KtSymbol
        konst referencedDeclaration: KtSymbol
    }

    interface ProtectedConstructorCallFromPublicInline : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ProtectedConstructorCallFromPublicInline::class
        konst inlineDeclaration: KtSymbol
        konst referencedDeclaration: KtSymbol
    }

    interface ProtectedCallFromPublicInlineError : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ProtectedCallFromPublicInlineError::class
        konst inlineDeclaration: KtSymbol
        konst referencedDeclaration: KtSymbol
    }

    interface ProtectedCallFromPublicInline : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ProtectedCallFromPublicInline::class
        konst inlineDeclaration: KtSymbol
        konst referencedDeclaration: KtSymbol
    }

    interface PrivateClassMemberFromInline : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = PrivateClassMemberFromInline::class
        konst inlineDeclaration: KtSymbol
        konst referencedDeclaration: KtSymbol
    }

    interface SuperCallFromPublicInline : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = SuperCallFromPublicInline::class
        konst symbol: KtSymbol
    }

    interface DeclarationCantBeInlined : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = DeclarationCantBeInlined::class
    }

    interface OverrideByInline : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = OverrideByInline::class
    }

    interface NonInternalPublishedApi : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = NonInternalPublishedApi::class
    }

    interface InkonstidDefaultFunctionalParameterForInline : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = InkonstidDefaultFunctionalParameterForInline::class
        konst defaultValue: KtExpression
        konst parameter: KtSymbol
    }

    interface ReifiedTypeParameterInOverride : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ReifiedTypeParameterInOverride::class
    }

    interface InlinePropertyWithBackingField : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = InlinePropertyWithBackingField::class
    }

    interface IllegalInlineParameterModifier : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = IllegalInlineParameterModifier::class
    }

    interface InlineSuspendFunctionTypeUnsupported : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = InlineSuspendFunctionTypeUnsupported::class
    }

    interface InefficientEqualsOverridingInValueClass : KtFirDiagnostic<KtNamedFunction> {
        override konst diagnosticClass get() = InefficientEqualsOverridingInValueClass::class
        konst type: KtType
    }

    interface CannotAllUnderImportFromSingleton : KtFirDiagnostic<KtImportDirective> {
        override konst diagnosticClass get() = CannotAllUnderImportFromSingleton::class
        konst objectName: Name
    }

    interface PackageCannotBeImported : KtFirDiagnostic<KtImportDirective> {
        override konst diagnosticClass get() = PackageCannotBeImported::class
    }

    interface CannotBeImported : KtFirDiagnostic<KtImportDirective> {
        override konst diagnosticClass get() = CannotBeImported::class
        konst name: Name
    }

    interface ConflictingImport : KtFirDiagnostic<KtImportDirective> {
        override konst diagnosticClass get() = ConflictingImport::class
        konst name: Name
    }

    interface OperatorRenamedOnImport : KtFirDiagnostic<KtImportDirective> {
        override konst diagnosticClass get() = OperatorRenamedOnImport::class
    }

    interface IllegalSuspendFunctionCall : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = IllegalSuspendFunctionCall::class
        konst suspendCallable: KtSymbol
    }

    interface IllegalSuspendPropertyAccess : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = IllegalSuspendPropertyAccess::class
        konst suspendCallable: KtSymbol
    }

    interface NonLocalSuspensionPoint : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NonLocalSuspensionPoint::class
    }

    interface IllegalRestrictedSuspendingFunctionCall : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = IllegalRestrictedSuspendingFunctionCall::class
    }

    interface NonModifierFormForBuiltInSuspend : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NonModifierFormForBuiltInSuspend::class
    }

    interface ModifierFormForNonBuiltInSuspend : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ModifierFormForNonBuiltInSuspend::class
    }

    interface ModifierFormForNonBuiltInSuspendFunError : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ModifierFormForNonBuiltInSuspendFunError::class
    }

    interface ModifierFormForNonBuiltInSuspendFunWarning : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ModifierFormForNonBuiltInSuspendFunWarning::class
    }

    interface ReturnForBuiltInSuspend : KtFirDiagnostic<KtReturnExpression> {
        override konst diagnosticClass get() = ReturnForBuiltInSuspend::class
    }

    interface RedundantLabelWarning : KtFirDiagnostic<KtLabelReferenceExpression> {
        override konst diagnosticClass get() = RedundantLabelWarning::class
    }

    interface ConflictingJvmDeclarations : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ConflictingJvmDeclarations::class
    }

    interface OverrideCannotBeStatic : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = OverrideCannotBeStatic::class
    }

    interface JvmStaticNotInObjectOrClassCompanion : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = JvmStaticNotInObjectOrClassCompanion::class
    }

    interface JvmStaticNotInObjectOrCompanion : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = JvmStaticNotInObjectOrCompanion::class
    }

    interface JvmStaticOnNonPublicMember : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = JvmStaticOnNonPublicMember::class
    }

    interface JvmStaticOnConstOrJvmField : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = JvmStaticOnConstOrJvmField::class
    }

    interface JvmStaticOnExternalInInterface : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = JvmStaticOnExternalInInterface::class
    }

    interface InapplicableJvmName : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = InapplicableJvmName::class
    }

    interface IllegalJvmName : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = IllegalJvmName::class
    }

    interface FunctionDelegateMemberNameClash : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = FunctionDelegateMemberNameClash::class
    }

    interface ValueClassWithoutJvmInlineAnnotation : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ValueClassWithoutJvmInlineAnnotation::class
    }

    interface JvmInlineWithoutValueClass : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = JvmInlineWithoutValueClass::class
    }

    interface JavaTypeMismatch : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = JavaTypeMismatch::class
        konst expectedType: KtType
        konst actualType: KtType
    }

    interface UpperBoundCannotBeArray : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = UpperBoundCannotBeArray::class
    }

    interface StrictfpOnClass : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = StrictfpOnClass::class
    }

    interface SynchronizedOnAbstract : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = SynchronizedOnAbstract::class
    }

    interface SynchronizedInInterface : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = SynchronizedInInterface::class
    }

    interface SynchronizedOnInline : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = SynchronizedOnInline::class
    }

    interface SynchronizedOnSuspendError : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = SynchronizedOnSuspendError::class
    }

    interface SynchronizedOnSuspendWarning : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = SynchronizedOnSuspendWarning::class
    }

    interface OverloadsWithoutDefaultArguments : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = OverloadsWithoutDefaultArguments::class
    }

    interface OverloadsAbstract : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = OverloadsAbstract::class
    }

    interface OverloadsInterface : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = OverloadsInterface::class
    }

    interface OverloadsLocal : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = OverloadsLocal::class
    }

    interface OverloadsAnnotationClassConstructorError : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = OverloadsAnnotationClassConstructorError::class
    }

    interface OverloadsAnnotationClassConstructorWarning : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = OverloadsAnnotationClassConstructorWarning::class
    }

    interface OverloadsPrivate : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = OverloadsPrivate::class
    }

    interface DeprecatedJavaAnnotation : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = DeprecatedJavaAnnotation::class
        konst kotlinName: FqName
    }

    interface JvmPackageNameCannotBeEmpty : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = JvmPackageNameCannotBeEmpty::class
    }

    interface JvmPackageNameMustBeValidName : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = JvmPackageNameMustBeValidName::class
    }

    interface JvmPackageNameNotSupportedInFilesWithClasses : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = JvmPackageNameNotSupportedInFilesWithClasses::class
    }

    interface PositionedValueArgumentForJavaAnnotation : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = PositionedValueArgumentForJavaAnnotation::class
    }

    interface RedundantRepeatableAnnotation : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = RedundantRepeatableAnnotation::class
        konst kotlinRepeatable: FqName
        konst javaRepeatable: FqName
    }

    interface LocalJvmRecord : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = LocalJvmRecord::class
    }

    interface NonFinalJvmRecord : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NonFinalJvmRecord::class
    }

    interface EnumJvmRecord : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = EnumJvmRecord::class
    }

    interface JvmRecordWithoutPrimaryConstructorParameters : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = JvmRecordWithoutPrimaryConstructorParameters::class
    }

    interface NonDataClassJvmRecord : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = NonDataClassJvmRecord::class
    }

    interface JvmRecordNotValParameter : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = JvmRecordNotValParameter::class
    }

    interface JvmRecordNotLastVarargParameter : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = JvmRecordNotLastVarargParameter::class
    }

    interface InnerJvmRecord : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = InnerJvmRecord::class
    }

    interface FieldInJvmRecord : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = FieldInJvmRecord::class
    }

    interface DelegationByInJvmRecord : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = DelegationByInJvmRecord::class
    }

    interface JvmRecordExtendsClass : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = JvmRecordExtendsClass::class
        konst superType: KtType
    }

    interface IllegalJavaLangRecordSupertype : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = IllegalJavaLangRecordSupertype::class
    }

    interface JvmDefaultInDeclaration : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = JvmDefaultInDeclaration::class
        konst annotation: String
    }

    interface JvmDefaultWithCompatibilityInDeclaration : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = JvmDefaultWithCompatibilityInDeclaration::class
    }

    interface JvmDefaultWithCompatibilityNotOnInterface : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = JvmDefaultWithCompatibilityNotOnInterface::class
    }

    interface ExternalDeclarationCannotBeAbstract : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = ExternalDeclarationCannotBeAbstract::class
    }

    interface ExternalDeclarationCannotHaveBody : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = ExternalDeclarationCannotHaveBody::class
    }

    interface ExternalDeclarationInInterface : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = ExternalDeclarationInInterface::class
    }

    interface ExternalDeclarationCannotBeInlined : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = ExternalDeclarationCannotBeInlined::class
    }

    interface NonSourceRepeatedAnnotation : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = NonSourceRepeatedAnnotation::class
    }

    interface RepeatedAnnotationWithContainer : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = RepeatedAnnotationWithContainer::class
        konst name: ClassId
        konst explicitContainerName: ClassId
    }

    interface RepeatableContainerMustHaveValueArrayError : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = RepeatableContainerMustHaveValueArrayError::class
        konst container: ClassId
        konst annotation: ClassId
    }

    interface RepeatableContainerMustHaveValueArrayWarning : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = RepeatableContainerMustHaveValueArrayWarning::class
        konst container: ClassId
        konst annotation: ClassId
    }

    interface RepeatableContainerHasNonDefaultParameterError : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = RepeatableContainerHasNonDefaultParameterError::class
        konst container: ClassId
        konst nonDefault: Name
    }

    interface RepeatableContainerHasNonDefaultParameterWarning : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = RepeatableContainerHasNonDefaultParameterWarning::class
        konst container: ClassId
        konst nonDefault: Name
    }

    interface RepeatableContainerHasShorterRetentionError : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = RepeatableContainerHasShorterRetentionError::class
        konst container: ClassId
        konst retention: String
        konst annotation: ClassId
        konst annotationRetention: String
    }

    interface RepeatableContainerHasShorterRetentionWarning : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = RepeatableContainerHasShorterRetentionWarning::class
        konst container: ClassId
        konst retention: String
        konst annotation: ClassId
        konst annotationRetention: String
    }

    interface RepeatableContainerTargetSetNotASubsetError : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = RepeatableContainerTargetSetNotASubsetError::class
        konst container: ClassId
        konst annotation: ClassId
    }

    interface RepeatableContainerTargetSetNotASubsetWarning : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = RepeatableContainerTargetSetNotASubsetWarning::class
        konst container: ClassId
        konst annotation: ClassId
    }

    interface RepeatableAnnotationHasNestedClassNamedContainerError : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = RepeatableAnnotationHasNestedClassNamedContainerError::class
    }

    interface RepeatableAnnotationHasNestedClassNamedContainerWarning : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = RepeatableAnnotationHasNestedClassNamedContainerWarning::class
    }

    interface SuspensionPointInsideCriticalSection : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = SuspensionPointInsideCriticalSection::class
        konst function: KtCallableSymbol
    }

    interface InapplicableJvmField : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = InapplicableJvmField::class
        konst message: String
    }

    interface InapplicableJvmFieldWarning : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = InapplicableJvmFieldWarning::class
        konst message: String
    }

    interface JvmSyntheticOnDelegate : KtFirDiagnostic<KtAnnotationEntry> {
        override konst diagnosticClass get() = JvmSyntheticOnDelegate::class
    }

    interface SubclassCantCallCompanionProtectedNonStatic : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = SubclassCantCallCompanionProtectedNonStatic::class
    }

    interface ConcurrentHashMapContainsOperatorError : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ConcurrentHashMapContainsOperatorError::class
    }

    interface ConcurrentHashMapContainsOperatorWarning : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = ConcurrentHashMapContainsOperatorWarning::class
    }

    interface SpreadOnSignaturePolymorphicCallError : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = SpreadOnSignaturePolymorphicCallError::class
    }

    interface SpreadOnSignaturePolymorphicCallWarning : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = SpreadOnSignaturePolymorphicCallWarning::class
    }

    interface JavaSamInterfaceConstructorReference : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = JavaSamInterfaceConstructorReference::class
    }

    interface ImplementingFunctionInterface : KtFirDiagnostic<KtClassOrObject> {
        override konst diagnosticClass get() = ImplementingFunctionInterface::class
    }

    interface OverridingExternalFunWithOptionalParams : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = OverridingExternalFunWithOptionalParams::class
    }

    interface OverridingExternalFunWithOptionalParamsWithFake : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = OverridingExternalFunWithOptionalParamsWithFake::class
        konst function: KtFunctionLikeSymbol
    }

    interface CallToDefinedExternallyFromNonExternalDeclaration : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = CallToDefinedExternallyFromNonExternalDeclaration::class
    }

    interface ExternalClassConstructorPropertyParameter : KtFirDiagnostic<KtParameter> {
        override konst diagnosticClass get() = ExternalClassConstructorPropertyParameter::class
    }

    interface ExternalEnumEntryWithBody : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ExternalEnumEntryWithBody::class
    }

    interface ExternalAnonymousInitializer : KtFirDiagnostic<KtAnonymousInitializer> {
        override konst diagnosticClass get() = ExternalAnonymousInitializer::class
    }

    interface ExternalDelegation : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ExternalDelegation::class
    }

    interface ExternalDelegatedConstructorCall : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ExternalDelegatedConstructorCall::class
    }

    interface WrongBodyOfExternalDeclaration : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = WrongBodyOfExternalDeclaration::class
    }

    interface WrongInitializerOfExternalDeclaration : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = WrongInitializerOfExternalDeclaration::class
    }

    interface WrongDefaultValueForExternalFunParameter : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = WrongDefaultValueForExternalFunParameter::class
    }

    interface NestedExternalDeclaration : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = NestedExternalDeclaration::class
    }

    interface WrongExternalDeclaration : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = WrongExternalDeclaration::class
        konst classKind: String
    }

    interface NestedClassInExternalInterface : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = NestedClassInExternalInterface::class
    }

    interface ExternalTypeExtendsNonExternalType : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ExternalTypeExtendsNonExternalType::class
    }

    interface InlineExternalDeclaration : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = InlineExternalDeclaration::class
    }

    interface EnumClassInExternalDeclarationWarning : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = EnumClassInExternalDeclarationWarning::class
    }

    interface InlineClassInExternalDeclarationWarning : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = InlineClassInExternalDeclarationWarning::class
    }

    interface InlineClassInExternalDeclaration : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = InlineClassInExternalDeclaration::class
    }

    interface ExtensionFunctionInExternalDeclaration : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ExtensionFunctionInExternalDeclaration::class
    }

    interface NonAbstractMemberOfExternalInterface : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = NonAbstractMemberOfExternalInterface::class
    }

    interface NonExternalDeclarationInInappropriateFile : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = NonExternalDeclarationInInappropriateFile::class
        konst type: KtType
    }

    interface CannotCheckForExternalInterface : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = CannotCheckForExternalInterface::class
        konst targetType: KtType
    }

    interface UncheckedCastToExternalInterface : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = UncheckedCastToExternalInterface::class
        konst sourceType: KtType
        konst targetType: KtType
    }

    interface ExternalInterfaceAsClassLiteral : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = ExternalInterfaceAsClassLiteral::class
    }

    interface JsExternalInheritorsOnly : KtFirDiagnostic<KtDeclaration> {
        override konst diagnosticClass get() = JsExternalInheritorsOnly::class
        konst parent: KtClassLikeSymbol
        konst kid: KtClassLikeSymbol
    }

    interface JsExternalArgument : KtFirDiagnostic<KtExpression> {
        override konst diagnosticClass get() = JsExternalArgument::class
        konst argType: KtType
    }

    interface NestedJsExport : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = NestedJsExport::class
    }

    interface WrongExportedDeclaration : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = WrongExportedDeclaration::class
        konst kind: String
    }

    interface NonExportableType : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = NonExportableType::class
        konst kind: String
        konst type: KtType
    }

    interface NonConsumableExportedIdentifier : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = NonConsumableExportedIdentifier::class
        konst name: String
    }

    interface DelegationByDynamic : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = DelegationByDynamic::class
    }

    interface SpreadOperatorInDynamicCall : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = SpreadOperatorInDynamicCall::class
    }

    interface WrongOperationWithDynamic : KtFirDiagnostic<KtElement> {
        override konst diagnosticClass get() = WrongOperationWithDynamic::class
        konst operation: String
    }

    interface Syntax : KtFirDiagnostic<PsiElement> {
        override konst diagnosticClass get() = Syntax::class
        konst message: String
    }

}
