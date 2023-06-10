/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.tree.generator

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.KtSourceFileLinesMapping
import org.jetbrains.kotlin.contracts.description.EventOccurrencesRange
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.fir.tree.generator.context.generatedType
import org.jetbrains.kotlin.fir.tree.generator.context.type
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeSimpleKotlinType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.SmartcastStability
import org.jetbrains.kotlin.types.Variance

konst sourceElementType = type(KtSourceElement::class)
konst sourceFileType = type(KtSourceFile::class)
konst sourceFileLinesMappingType = type(KtSourceFileLinesMapping::class)
konst jumpTargetType = type("fir", "FirTarget")
konst constKindType = type("types", "ConstantValueKind")
konst operationType = type("fir.expressions", "FirOperation")
konst classKindType = type(ClassKind::class)
konst eventOccurrencesRangeType = type(EventOccurrencesRange::class)
konst inlineStatusType = type("fir.declarations", "InlineStatus")
konst varianceType = type(Variance::class)
konst nameType = type(Name::class)
konst visibilityType = type(Visibility::class)
konst effectiveVisibilityType = type("descriptors", "EffectiveVisibility")
konst modalityType = type(Modality::class)
konst smartcastStabilityType = type(SmartcastStability::class)
konst fqNameType = type(FqName::class)
konst classIdType = type(ClassId::class)
konst annotationUseSiteTargetType = type(AnnotationUseSiteTarget::class)
konst operationKindType = type("contracts.description", "LogicOperationKind")
konst coneKotlinTypeType = type(ConeKotlinType::class)
konst coneSimpleKotlinTypeType = type(ConeSimpleKotlinType::class)
konst coneClassLikeTypeType = type(ConeClassLikeType::class)

konst whenRefType = generatedType("", "FirExpressionRef<FirWhenExpression>")
konst referenceToSimpleExpressionType = generatedType("", "FirExpressionRef<FirExpression>")
konst safeCallCheckedSubjectReferenceType = generatedType("", "FirExpressionRef<FirCheckedSafeCallSubject>")

konst firModuleDataType = type("fir", "FirModuleData")
konst noReceiverExpressionType = generatedType("expressions.impl", "FirNoReceiverExpression", firType = true)
konst firImplicitTypeWithoutSourceType = generatedType("types.impl", "FirImplicitTypeRefImplWithoutSource")
konst firQualifierPartType = type("fir.types", "FirQualifierPart")
konst simpleNamedReferenceType = generatedType("references.impl", "FirSimpleNamedReference")
konst explicitThisReferenceType = generatedType("references.impl", "FirExplicitThisReference", firType = true)
konst explicitSuperReferenceType = generatedType("references.impl", "FirExplicitSuperReference", firType = true)
konst implicitBooleanTypeRefType = generatedType("types.impl", "FirImplicitBooleanTypeRef", firType = true)
konst implicitNothingTypeRefType = generatedType("types.impl", "FirImplicitNothingTypeRef", firType = true)
konst implicitStringTypeRefType = generatedType("types.impl", "FirImplicitStringTypeRef", firType = true)
konst implicitUnitTypeRefType = generatedType("types.impl", "FirImplicitUnitTypeRef", firType = true)
konst resolvePhaseType = type("fir.declarations", "FirResolvePhase")
konst resolveStateType = type("fir.declarations", "FirResolveState")
konst propertyBodyResolveStateType = type("fir.declarations", "FirPropertyBodyResolveState")
konst stubReferenceType = generatedType("references.impl", "FirStubReference", firType = true)

konst firBasedSymbolType = type("fir.symbols", "FirBasedSymbol")
konst functionSymbolType = type("fir.symbols.impl", "FirFunctionSymbol")
konst backingFieldSymbolType = type("fir.symbols.impl", "FirBackingFieldSymbol")
konst delegateFieldSymbolType = type("fir.symbols.impl", "FirDelegateFieldSymbol")
konst classSymbolType = type("fir.symbols.impl", "FirClassSymbol")
konst classLikeSymbolType = type("fir.symbols.impl", "FirClassLikeSymbol<*>")
konst regularClassSymbolType = type("fir.symbols.impl", "FirRegularClassSymbol")
konst typeParameterSymbolType = type("fir.symbols.impl", "FirTypeParameterSymbol")
konst emptyArgumentListType = type("fir.expressions", "FirEmptyArgumentList")
konst firScopeProviderType = type("fir.scopes", "FirScopeProvider")

konst pureAbstractElementType = generatedType("FirPureAbstractElement")
konst coneContractElementType = type("fir.contracts.description", "ConeContractDescriptionElement")
konst coneEffectDeclarationType = type("fir.contracts.description", "ConeEffectDeclaration")
konst emptyContractDescriptionType = generatedType("contracts.impl", "FirEmptyContractDescription")
konst coneDiagnosticType = generatedType("diagnostics", "ConeDiagnostic")
konst coneStubDiagnosticType = generatedType("diagnostics", "ConeStubDiagnostic")

konst dslBuilderAnnotationType = generatedType("builder", "FirBuilderDsl")
konst firImplementationDetailType = generatedType("FirImplementationDetail")
konst declarationOriginType = generatedType("declarations", "FirDeclarationOrigin")
konst declarationAttributesType = generatedType("declarations", "FirDeclarationAttributes")

konst exhaustivenessStatusType = generatedType("expressions", "ExhaustivenessStatus")

konst callableReferenceMappedArgumentsType = type("fir.resolve.calls", "CallableReferenceMappedArguments")

konst functionCallOrigin = type("fir.expressions", "FirFunctionCallOrigin")

konst resolvedDeclarationStatusImplType = type("fir.declarations.impl", "FirResolvedDeclarationStatusImpl")

konst deprecationsProviderType = type("fir.declarations", "DeprecationsProvider")
konst unresolvedDeprecationsProviderType = type("fir.declarations", "UnresolvedDeprecationProvider")
konst emptyAnnotationArgumentMappingType = type("fir.expressions.impl", "FirEmptyAnnotationArgumentMapping")

konst firPropertySymbolType = type("fir.symbols.impl", "FirPropertySymbol")
konst errorTypeRefImplType = type("fir.types.impl", "FirErrorTypeRefImpl", firType = true)

konst annotationResolvePhaseType = generatedType("expressions", "FirAnnotationResolvePhase")
