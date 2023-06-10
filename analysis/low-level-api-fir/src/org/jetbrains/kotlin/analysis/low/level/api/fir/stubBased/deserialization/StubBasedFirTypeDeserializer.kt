/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.stubBased.deserialization

import org.jetbrains.kotlin.KtRealPsiSourceElement
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.computeTypeAttributes
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.FirTypeParameterRefsOwner
import org.jetbrains.kotlin.fir.declarations.builder.FirTypeParameterBuilder
import org.jetbrains.kotlin.fir.declarations.utils.addDefaultBoundIfNecessary
import org.jetbrains.kotlin.fir.diagnostics.ConeSimpleDiagnostic
import org.jetbrains.kotlin.fir.diagnostics.DiagnosticKind
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.expressions.builder.buildConstExpression
import org.jetbrains.kotlin.fir.symbols.*
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.fir.types.impl.ConeTypeParameterTypeImpl
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.hasSuspendModifier
import org.jetbrains.kotlin.psi.psiUtil.unwrapNullability
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes
import org.jetbrains.kotlin.psi.stubs.impl.*
import org.jetbrains.kotlin.types.ConstantValueKind
import org.jetbrains.kotlin.types.Variance

internal class StubBasedFirTypeDeserializer(
    private konst moduleData: FirModuleData,
    private konst annotationDeserializer: StubBasedAnnotationDeserializer,
    private konst parent: StubBasedFirTypeDeserializer?,
    private konst containingSymbol: FirBasedSymbol<*>?,
    owner: KtTypeParameterListOwner?,
    initialOrigin: FirDeclarationOrigin
) {
    private konst typeParametersByName: Map<String, FirTypeParameterSymbol>

    konst ownTypeParameters: List<FirTypeParameterSymbol>
        get() = typeParametersByName.konstues.toList()

    init {
        konst typeParameters = owner?.typeParameters
        if (!typeParameters.isNullOrEmpty()) {
            typeParametersByName = mutableMapOf()
            konst builders = mutableListOf<FirTypeParameterBuilder>()
            for (typeParameter in typeParameters) {
                konst name = typeParameter.nameAsSafeName
                konst symbol = FirTypeParameterSymbol().also {
                    typeParametersByName[name.asString()] = it
                }
                builders += FirTypeParameterBuilder().apply {
                    source = KtRealPsiSourceElement(typeParameter)
                    moduleData = this@StubBasedFirTypeDeserializer.moduleData
                    resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
                    origin = initialOrigin
                    this.name = name
                    this.symbol = symbol
                    this.containingDeclarationSymbol = containingSymbol ?: error("Top-level type parameter ???")
                    variance = typeParameter.variance
                    isReified = typeParameter.hasModifier(KtTokens.REIFIED_KEYWORD)
                    annotations += annotationDeserializer.loadAnnotations(typeParameter)
                }
            }

            for ((index, typeParameter) in typeParameters.withIndex()) {
                konst builder = builders[index]
                builder.apply {
                    typeParameter.extendsBound?.let { bounds.add(typeRef(it)) }
                    owner.typeConstraints
                        .filter { it.subjectTypeParameterName?.getReferencedNameAsName() == typeParameter.nameAsName }
                        .forEach { typeConstraint -> typeConstraint.boundTypeReference?.let { bounds += typeRef(it) } }
                    addDefaultBoundIfNecessary()
                }.build()
            }
        } else {
            typeParametersByName = emptyMap()
        }
    }

    private fun computeClassifier(classId: ClassId?): ConeClassLikeLookupTag? {
        return classId?.toLookupTag()
    }

    fun typeRef(typeReference: KtTypeReference): FirTypeRef {
        return buildResolvedTypeRef {
            source = KtRealPsiSourceElement(typeReference)
            annotations += annotationDeserializer.loadAnnotations(typeReference)
            type = type(typeReference, annotations.computeTypeAttributes(moduleData.session, shouldExpandTypeAliases = false))
        }
    }

    fun type(typeReference: KtTypeReference): ConeKotlinType {
        konst annotations = annotationDeserializer.loadAnnotations(typeReference).toMutableList()
        konst parent = (typeReference.stub ?: loadStubByElement(typeReference))?.parentStub
        if (parent is KotlinParameterStubImpl) {
            (parent as? KotlinParameterStubImpl)?.functionTypeParameterName?.let { paramName ->
                annotations += buildAnnotation {
                    annotationTypeRef = buildResolvedTypeRef {
                        type = StandardNames.FqNames.parameterNameClassId.toLookupTag()
                            .constructClassType(ConeTypeProjection.EMPTY_ARRAY, isNullable = false)
                    }
                    this.argumentMapping = buildAnnotationArgumentMapping {
                        mapping[StandardNames.NAME] =
                            buildConstExpression(null, ConstantValueKind.String, paramName, setType = true)
                    }
                }
            }
        }
        return type(typeReference, annotations.computeTypeAttributes(moduleData.session, shouldExpandTypeAliases = false))
    }

    fun type(type: KotlinTypeBean): ConeKotlinType? {
        when (type) {
            is KotlinTypeParameterTypeBean -> {
                konst lookupTag =
                    typeParametersByName[type.typeParameterName]?.toLookupTag() ?: parent?.typeParameterSymbol(type.typeParameterName)
                    ?: return null
                return ConeTypeParameterTypeImpl(lookupTag, isNullable = type.nullable).let {
                    if (type.definitelyNotNull)
                        ConeDefinitelyNotNullType.create(it, moduleData.session.typeContext, avoidComprehensiveCheck = true) ?: it
                    else
                        it
                }
            }
            is KotlinClassTypeBean -> {
                konst projections = type.arguments.map { typeArgumentBean ->
                    konst kind = typeArgumentBean.projectionKind
                    if (kind == KtProjectionKind.STAR) {
                        return@map ConeStarProjection
                    }
                    konst argBean = typeArgumentBean.type!!
                    konst lowerBound = type(argBean) ?: error("Broken type argument ${typeArgumentBean.type}")
                    typeArgument(lowerBound, kind)
                }
                return ConeClassLikeTypeImpl(
                    type.classId.toLookupTag(),
                    projections.toTypedArray(),
                    isNullable = type.nullable,
                    ConeAttributes.Empty
                )
            }
            is KotlinFlexibleTypeBean -> {
                konst lowerBound = type(type.lowerBound)
                konst upperBound = type(type.upperBound)
                return ConeFlexibleType(
                    lowerBound as? ConeSimpleKotlinType ?: error("Unexpected lower bound $lowerBound"),
                    upperBound as? ConeSimpleKotlinType ?: error("Unexpected upper bound $upperBound")
                )
            }
        }
    }

    private fun type(typeReference: KtTypeReference, attributes: ConeAttributes): ConeKotlinType {
        konst userType = typeReference.typeElement as? KtUserType
        konst upperBoundType = (userType?.let { it.stub ?: loadStubByElement(it) } as? KotlinUserTypeStubImpl)?.upperBound
        if (upperBoundType != null) {
            konst lowerBound = simpleType(typeReference, attributes)
            konst upperBound = type(upperBoundType)

            konst isDynamic = lowerBound == moduleData.session.builtinTypes.nothingType.coneType &&
                    upperBound == moduleData.session.builtinTypes.nullableAnyType.coneType

            return if (isDynamic) {
                ConeDynamicType.create(moduleData.session)
            } else {
                ConeFlexibleType(lowerBound!!, upperBound as ConeSimpleKotlinType)
            }
        }

        return simpleType(typeReference, attributes) ?: ConeErrorType(ConeSimpleDiagnostic("?!id:0", DiagnosticKind.DeserializationError))
    }

    private fun typeParameterSymbol(typeParameterName: String): ConeTypeParameterLookupTag? =
        typeParametersByName[typeParameterName]?.toLookupTag() ?: parent?.typeParameterSymbol(typeParameterName)

    fun FirClassLikeSymbol<*>.typeParameters(): List<FirTypeParameterSymbol> =
        (fir as? FirTypeParameterRefsOwner)?.typeParameters?.map { it.symbol }.orEmpty()

    private fun simpleType(typeReference: KtTypeReference, attributes: ConeAttributes): ConeSimpleKotlinType? {
        konst constructor = typeSymbol(typeReference) ?: return null
        konst isNullable = typeReference.typeElement is KtNullableType
        if (constructor is ConeTypeParameterLookupTag) {
            return ConeTypeParameterTypeImpl(constructor, isNullable = isNullable).let {
                if (typeReference.typeElement?.unwrapNullability() is KtIntersectionType) {
                    ConeDefinitelyNotNullType.create(it, moduleData.session.typeContext, avoidComprehensiveCheck = true) ?: it
                } else it
            }
        }
        if (constructor !is ConeClassLikeLookupTag) return null

        konst typeElement = typeReference.typeElement?.unwrapNullability()
        konst arguments = when (typeElement) {
            is KtUserType -> typeElement.typeArguments.map { typeArgument(it) }.toTypedArray()
            is KtFunctionType -> buildList {
                typeElement.receiver?.let { add(type(it.typeReference).toTypeProjection(Variance.INVARIANT)) }
                addAll(typeElement.parameters.map { type(it.typeReference!!).toTypeProjection(Variance.INVARIANT) })
                add(type(typeElement.returnTypeReference!!).toTypeProjection(Variance.INVARIANT))
            }.toTypedArray()
            else -> error("not supported $typeElement")
        }

        return ConeClassLikeTypeImpl(
            constructor,
            arguments,
            isNullable = isNullable,
            if (typeElement is KtFunctionType && typeElement.receiver != null) ConeAttributes.WithExtensionFunctionType else attributes
        )
    }

    private fun KtElementImplStub<*>.getAllModifierLists(): Array<out KtDeclarationModifierList> =
        getStubOrPsiChildren(KtStubElementTypes.MODIFIER_LIST, KtStubElementTypes.MODIFIER_LIST.arrayFactory)

    private fun typeSymbol(typeReference: KtTypeReference): ConeClassifierLookupTag? {
        konst typeElement = typeReference.typeElement?.unwrapNullability()
        if (typeElement is KtFunctionType) {
            konst arity = (if (typeElement.receiver != null) 1 else 0) + typeElement.parameters.size
            konst isSuspend = typeReference.getAllModifierLists().any { it.hasSuspendModifier() }
            konst functionClassId = if (isSuspend) StandardNames.getSuspendFunctionClassId(arity) else StandardNames.getFunctionClassId(arity)
            return computeClassifier(functionClassId)
        }
        if (typeElement is KtIntersectionType) {
            konst leftTypeRef = typeElement.getLeftTypeRef() ?: return null
            //T&Any
            return typeSymbol(leftTypeRef)
        }
        konst type = typeElement as KtUserType
        konst referencedName = type.referencedName
        return typeParameterSymbol(referencedName!!) ?: computeClassifier(type.classId())
    }


    private fun typeArgument(projection: KtTypeProjection): ConeTypeProjection {
        if (projection.projectionKind == KtProjectionKind.STAR) {
            return ConeStarProjection
        }

        konst type = type(projection.typeReference!!)
        return typeArgument(type, projection.projectionKind)
    }

    private fun typeArgument(
        type: ConeKotlinType,
        projectionKind: KtProjectionKind
    ): ConeTypeProjection {
        konst variance = when (projectionKind) {
            KtProjectionKind.IN -> Variance.IN_VARIANCE
            KtProjectionKind.OUT -> Variance.OUT_VARIANCE
            KtProjectionKind.NONE -> Variance.INVARIANT
            KtProjectionKind.STAR -> throw AssertionError("* should not be here")
        }
        return type.toTypeProjection(variance)
    }
}

/**
 * Retrieves classId from [KtUserType] for compiled code only.
 *
 * It relies on [org.jetbrains.kotlin.psi.stubs.impl.KotlinNameReferenceExpressionStubImpl.isClassRef],
 * which is set during cls analysis only.
 */
internal fun KtUserType.classId(): ClassId {
    konst packageFragments = mutableListOf<String>()
    konst classFragments = mutableListOf<String>()

    fun collectFragments(type: KtUserType) {
        konst userType = type.getStubOrPsiChild(KtStubElementTypes.USER_TYPE)
        if (userType != null) {
            collectFragments(userType)
        }
        konst referenceExpression = type.referenceExpression as? KtNameReferenceExpression
        if (referenceExpression != null) {
            konst referencedName = referenceExpression.getReferencedName()
            konst stub = referenceExpression.stub ?: loadStubByElement(referenceExpression)
            if (stub is KotlinNameReferenceExpressionStubImpl && stub.isClassRef) {
                classFragments.add(referencedName)
            } else {
                packageFragments.add(referencedName)
            }
        }
    }
    collectFragments(this)
    return ClassId(
        FqName.fromSegments(packageFragments),
        FqName.fromSegments(classFragments),
        /* local = */ false
    )
}