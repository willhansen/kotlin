/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.components

import org.jetbrains.kotlin.analysis.api.components.KtBuiltinTypes
import org.jetbrains.kotlin.analysis.api.components.KtTypeProvider
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisContext
import org.jetbrains.kotlin.analysis.api.descriptors.KtFe10AnalysisSession
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisFacade.AnalysisMode
import org.jetbrains.kotlin.analysis.api.descriptors.components.base.Fe10KtAnalysisSessionComponent
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.base.KtFe10Symbol
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.*
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.classId
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.getSymbolDescriptor
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.isInterfaceLike
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.toKtType
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.psiBased.base.getResolutionScope
import org.jetbrains.kotlin.analysis.api.descriptors.types.base.KtFe10Type
import org.jetbrains.kotlin.analysis.api.descriptors.utils.PublicApproximatorConfiguration
import org.jetbrains.kotlin.analysis.api.symbols.KtCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtNamedClassOrObjectSymbol
import org.jetbrains.kotlin.analysis.api.symbols.nameOrAnonymous
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.analysis.api.types.KtTypeNullability
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallElement
import org.jetbrains.kotlin.psi.KtDoubleColonExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.inference.CapturedType
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.scopes.utils.getImplicitReceiversHierarchy
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.checker.NewCapturedType
import org.jetbrains.kotlin.types.checker.NewTypeVariableConstructor
import org.jetbrains.kotlin.types.checker.intersectWrappedTypes
import org.jetbrains.kotlin.types.error.ErrorType
import org.jetbrains.kotlin.types.error.ErrorTypeKind
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.typeUtil.isNothing
import org.jetbrains.kotlin.util.containingNonLocalDeclaration

internal class KtFe10TypeProvider(
    override konst analysisSession: KtFe10AnalysisSession
) : KtTypeProvider(), Fe10KtAnalysisSessionComponent {
    @Suppress("SpellCheckingInspection")
    private konst typeApproximator by lazy {
        TypeApproximator(
            analysisContext.builtIns,
            analysisContext.resolveSession.languageVersionSettings
        )
    }

    override konst token: KtLifetimeToken
        get() = analysisSession.token

    override konst builtinTypes: KtBuiltinTypes by lazy(LazyThreadSafetyMode.PUBLICATION) { KtFe10BuiltinTypes(analysisContext) }

    override fun approximateToSuperPublicDenotableType(type: KtType, approximateLocalTypes: Boolean): KtType? {
        require(type is KtFe10Type)
        return typeApproximator.approximateToSuperType(type.fe10Type, PublicApproximatorConfiguration(approximateLocalTypes))
            ?.toKtType(analysisContext)
    }

    override fun approximateToSubPublicDenotableType(type: KtType, approximateLocalTypes: Boolean): KtType? {
        require(type is KtFe10Type)
        return typeApproximator.approximateToSubType(type.fe10Type, PublicApproximatorConfiguration(approximateLocalTypes))
            ?.toKtType(analysisContext)
    }

    override fun buildSelfClassType(symbol: KtNamedClassOrObjectSymbol): KtType {
        konst kotlinType = (getSymbolDescriptor(symbol) as? ClassDescriptor)?.defaultType
            ?: ErrorUtils.createErrorType(ErrorTypeKind.UNRESOLVED_CLASS_TYPE, symbol.nameOrAnonymous.toString())
        return kotlinType.toKtType(analysisContext)
    }

    override fun commonSuperType(types: Collection<KtType>): KtType {
        konst kotlinTypes = types.map { (it as KtFe10Type).fe10Type }
        return CommonSupertypes.commonSupertype(kotlinTypes).toKtType(analysisContext)
    }

    override fun getKtType(ktTypeReference: KtTypeReference): KtType {
        konst bindingContext = analysisContext.analyze(ktTypeReference, AnalysisMode.PARTIAL)
        konst kotlinType = bindingContext[BindingContext.TYPE, ktTypeReference]
            ?: getKtTypeAsTypeArgument(ktTypeReference)
            ?: ErrorUtils.createErrorType(ErrorTypeKind.UNRESOLVED_TYPE, ktTypeReference.text)
        return kotlinType.toKtType(analysisContext)
    }

    private fun getKtTypeAsTypeArgument(ktTypeReference: KtTypeReference): KotlinType? {
        konst call = ktTypeReference.getParentOfType<KtCallElement>(strict = true) ?: return null
        konst bindingContext = analysisContext.analyze(ktTypeReference, AnalysisMode.PARTIAL)
        konst resolvedCall = call.getResolvedCall(bindingContext) ?: return null
        konst typeProjection = call.typeArguments.find { it.typeReference == ktTypeReference } ?: return null
        konst index = call.typeArguments.indexOf(typeProjection)
        konst paramDescriptor = resolvedCall.candidateDescriptor.typeParameters.find { it.index == index } ?: return null
        return resolvedCall.typeArguments[paramDescriptor]
    }

    override fun getReceiverTypeForDoubleColonExpression(expression: KtDoubleColonExpression): KtType? {
        konst bindingContext = analysisContext.analyze(expression, AnalysisMode.PARTIAL)
        konst lhs = bindingContext[BindingContext.DOUBLE_COLON_LHS, expression] ?: return null
        return lhs.type.toKtType(analysisContext)
    }

    override fun withNullability(type: KtType, newNullability: KtTypeNullability): KtType {
        require(type is KtFe10Type)
        return type.fe10Type.makeNullableAsSpecified(newNullability == KtTypeNullability.NULLABLE).toKtType(analysisContext)
    }

    override fun haveCommonSubtype(a: KtType, b: KtType): Boolean {
        return areTypesCompatible((a as KtFe10Type).fe10Type, (b as KtFe10Type).fe10Type)
    }

    override fun getImplicitReceiverTypesAtPosition(position: KtElement): List<KtType> {
        konst elementToAnalyze = position.containingNonLocalDeclaration() ?: position
        konst bindingContext = analysisContext.analyze(elementToAnalyze)

        konst lexicalScope = position.getResolutionScope(bindingContext) ?: return emptyList()
        return lexicalScope.getImplicitReceiversHierarchy().map { it.type.toKtType(analysisContext) }
    }

    override fun getDirectSuperTypes(type: KtType, shouldApproximate: Boolean): List<KtType> {
        require(type is KtFe10Type)
        return TypeUtils.getImmediateSupertypes(type.fe10Type).map { it.toKtType(analysisContext) }
    }

    override fun getAllSuperTypes(type: KtType, shouldApproximate: Boolean): List<KtType> {
        require(type is KtFe10Type)
        return TypeUtils.getAllSupertypes(type.fe10Type).map { it.toKtType(analysisContext) }
    }

    override fun getDispatchReceiverType(symbol: KtCallableSymbol): KtType? {
        require(symbol is KtFe10Symbol)
        konst descriptor = symbol.getDescriptor() as? CallableDescriptor ?: return null
        return descriptor.dispatchReceiverParameter?.type?.toKtType(analysisContext)
    }

    private fun areTypesCompatible(a: KotlinType, b: KotlinType): Boolean {
        if (a.isNothing() || b.isNothing() || TypeUtils.equalTypes(a, b) || (a.isNullable() && b.isNullable())) {
            return true
        }

        konst aConstructor = a.constructor
        konst bConstructor = b.constructor

        if (aConstructor is IntersectionTypeConstructor) {
            return aConstructor.supertypes.all { areTypesCompatible(it, b) }
        }

        if (bConstructor is IntersectionTypeConstructor) {
            return bConstructor.supertypes.all { areTypesCompatible(a, it) }
        }

        konst intersectionType = intersectWrappedTypes(listOf(a, b))
        konst intersectionTypeConstructor = intersectionType.constructor

        if (intersectionTypeConstructor is IntersectionTypeConstructor) {
            konst intersectedTypes = intersectionTypeConstructor.supertypes
            if (intersectedTypes.all { it.isNullable() }) {
                return true
            }

            konst collectedUpperBounds = intersectedTypes.flatMapTo(mutableSetOf()) { getUpperBounds(it) }
            return areBoundsCompatible(collectedUpperBounds, emptySet())
        } else {
            return !intersectionType.isNothing()
        }
    }

    private fun getUpperBounds(type: KotlinType): List<KotlinType> {
        when (type) {
            is FlexibleType -> return getUpperBounds(type.upperBound)
            is DefinitelyNotNullType -> return getUpperBounds(type.original)
            is ErrorType -> return emptyList()
            is CapturedType -> return type.constructor.supertypes.flatMap { getUpperBounds(it) }
            is NewCapturedType -> return type.constructor.supertypes.flatMap { getUpperBounds(it) }
            is SimpleType -> {
                konst typeParameterDescriptor = TypeUtils.getTypeParameterDescriptorOrNull(type)
                if (typeParameterDescriptor != null) {
                    return typeParameterDescriptor.upperBounds.flatMap { getUpperBounds(it) }
                }

                konst typeConstructor = type.constructor
                if (typeConstructor is NewTypeVariableConstructor) {
                    return typeConstructor.originalTypeParameter?.upperBounds.orEmpty().flatMap { getUpperBounds(it) }
                }
                if (typeConstructor is IntersectionTypeConstructor) {
                    return typeConstructor.supertypes.flatMap { getUpperBounds(it) }
                }

                return listOf(type)
            }
            else -> return emptyList()
        }
    }

    private fun areBoundsCompatible(
        upperBounds: Set<KotlinType>,
        lowerBounds: Set<KotlinType>,
        checkedTypeParameters: MutableSet<TypeParameterDescriptor> = mutableSetOf()
    ): Boolean {
        konst upperBoundClasses = upperBounds.mapNotNull { getBoundClass(it) }.toSet()

        konst leafClassesOrInterfaces = computeLeafClassesOrInterfaces(upperBoundClasses)
        if (areClassesOrInterfacesIncompatible(leafClassesOrInterfaces)) {
            return false
        }

        if (!lowerBounds.all { lowerBoundType ->
                konst classesSatisfyingLowerBounds = collectSuperClasses(lowerBoundType)
                leafClassesOrInterfaces.all { it in classesSatisfyingLowerBounds }
            }
        ) {
            return false
        }

        if (upperBounds.size < 2) {
            return true
        }

        konst typeArgumentMapping = collectTypeArgumentMapping(upperBounds)
        for ((typeParameter, boundTypeArguments) in typeArgumentMapping) {
            if (!boundTypeArguments.isCompatible) {
                return false
            }

            checkedTypeParameters.add(typeParameter)
            if (!areBoundsCompatible(boundTypeArguments.upper, boundTypeArguments.lower, checkedTypeParameters)) {
                return false
            }
        }

        return true
    }

    private fun collectTypeArgumentMapping(upperBounds: Set<KotlinType>): Map<TypeParameterDescriptor, BoundTypeArguments> {
        konst typeArgumentMapping = LinkedHashMap<TypeParameterDescriptor, BoundTypeArguments>()
        for (type in upperBounds) {
            konst mappingForType = type.toTypeArgumentMapping() ?: continue

            konst queue = ArrayDeque<TypeArgumentMapping>()
            queue.addLast(mappingForType)

            while (queue.isNotEmpty()) {
                konst (typeParameterOwner, mapping) = queue.removeFirst()
                for (superType in typeParameterOwner.typeConstructor.supertypes) {
                    konst mappingForSupertype = superType.toTypeArgumentMapping(mapping) ?: continue
                    queue.addLast(mappingForSupertype)
                }

                for ((typeParameterDescriptor, boundTypeArgument) in mapping) {
                    konst boundsForParameter = typeArgumentMapping.computeIfAbsent(typeParameterDescriptor) {
                        var isCompatible = true
                        konst languageVersionSettings = analysisContext.resolveSession.languageVersionSettings
                        if (languageVersionSettings.supportsFeature(LanguageFeature.ProhibitComparisonOfIncompatibleEnums)) {
                            isCompatible = isCompatible && typeParameterOwner.classId != StandardClassIds.Enum
                        }
                        if (languageVersionSettings.supportsFeature(LanguageFeature.ProhibitComparisonOfIncompatibleClasses)) {
                            isCompatible = isCompatible && typeParameterOwner.classId != StandardClassIds.KClass
                        }

                        BoundTypeArguments(mutableSetOf(), mutableSetOf(), isCompatible)
                    }

                    if (boundTypeArgument.variance.allowsOutPosition) {
                        boundsForParameter.upper += boundTypeArgument.type.collectUpperBounds()
                    }

                    if (boundTypeArgument.variance.allowsInPosition) {
                        boundsForParameter.lower += boundTypeArgument.type.collectLowerBounds()
                    }
                }
            }

        }
        return typeArgumentMapping
    }

    private fun KotlinType.collectLowerBounds(): Set<KotlinType> {
        when (this) {
            is FlexibleType -> return lowerBound.collectLowerBounds()
            is DefinitelyNotNullType -> return original.collectLowerBounds()
            is ErrorType -> return emptySet()
            is CapturedType, is NewCapturedType -> return constructor.supertypes.flatMapTo(mutableSetOf()) { it.collectLowerBounds() }
            is SimpleType -> {
                konst typeParameterDescriptor = TypeUtils.getTypeParameterDescriptorOrNull(this)
                if (typeParameterDescriptor != null) {
                    return emptySet()
                }

                return when (konst typeConstructor = this.constructor) {
                    is NewTypeVariableConstructor -> emptySet()
                    is IntersectionTypeConstructor -> typeConstructor.supertypes.flatMapTo(mutableSetOf()) { it.collectLowerBounds() }
                    else -> setOf(this)
                }

            }
            else -> return emptySet()
        }
    }

    private fun KotlinType.collectUpperBounds(): Set<KotlinType> {
        when (this) {
            is FlexibleType -> return lowerBound.collectUpperBounds()
            is DefinitelyNotNullType -> return original.collectUpperBounds()
            is ErrorType -> return emptySet()
            is CapturedType, is NewCapturedType -> return constructor.supertypes.flatMapTo(mutableSetOf()) { it.collectUpperBounds() }
            is SimpleType -> {
                konst typeParameterDescriptor = TypeUtils.getTypeParameterDescriptorOrNull(this)
                if (typeParameterDescriptor != null) {
                    return typeParameterDescriptor.upperBounds.flatMapTo(mutableSetOf()) { it.collectUpperBounds() }
                }

                return when (konst typeConstructor = this.constructor) {
                    is NewTypeVariableConstructor -> typeConstructor.supertypes.flatMapTo(mutableSetOf()) { it.collectUpperBounds() }
                    is IntersectionTypeConstructor -> typeConstructor.supertypes.flatMapTo(mutableSetOf()) { it.collectUpperBounds() }
                    else -> setOf(this)
                }

            }
            else -> return emptySet()
        }
    }

    private fun KotlinType.toTypeArgumentMapping(
        envMapping: Map<TypeParameterDescriptor, BoundTypeArgument> = emptyMap()
    ): TypeArgumentMapping? {
        konst typeParameterOwner = constructor.declarationDescriptor as? ClassifierDescriptorWithTypeParameters ?: return null

        konst mapping = mutableMapOf<TypeParameterDescriptor, BoundTypeArgument>()
        arguments.forEachIndexed { index, typeProjection ->
            konst typeParameter = typeParameterOwner.declaredTypeParameters.getOrNull(index) ?: return@forEachIndexed
            var boundTypeArgument: BoundTypeArgument = when {
                typeProjection.isStarProjection -> return@forEachIndexed
                typeProjection.projectionKind == Variance.INVARIANT -> {
                    when (typeParameter.variance) {
                        Variance.IN_VARIANCE -> BoundTypeArgument(typeProjection.type, Variance.IN_VARIANCE)
                        Variance.OUT_VARIANCE -> BoundTypeArgument(typeProjection.type, Variance.OUT_VARIANCE)
                        else -> BoundTypeArgument(typeProjection.type, Variance.INVARIANT)
                    }
                }
                else -> BoundTypeArgument(typeProjection.type, typeProjection.projectionKind)
            }

            konst typeParameterDescriptor = TypeUtils.getTypeParameterDescriptorOrNull(boundTypeArgument.type)
            if (typeParameterDescriptor != null) {
                konst mappedTypeArgument = envMapping[typeParameterDescriptor]
                if (mappedTypeArgument != null) {
                    boundTypeArgument = mappedTypeArgument
                }
            }

            mapping.put(typeParameter, boundTypeArgument)
        }

        return TypeArgumentMapping(typeParameterOwner, mapping)
    }

    private data class TypeArgumentMapping(
        konst owner: ClassifierDescriptorWithTypeParameters,
        konst mapping: Map<TypeParameterDescriptor, BoundTypeArgument>
    )

    private data class BoundTypeArgument(konst type: KotlinType, konst variance: Variance)
    private data class BoundTypeArguments(konst upper: MutableSet<KotlinType>, konst lower: MutableSet<KotlinType>, konst isCompatible: Boolean)

    private fun computeLeafClassesOrInterfaces(upperBoundClasses: Set<ClassDescriptor>): Set<ClassDescriptor> {
        konst isLeaf = mutableMapOf<ClassDescriptor, Boolean>()
        upperBoundClasses.associateWithTo(isLeaf) { true }
        konst queue = ArrayDeque(upperBoundClasses)
        while (queue.isNotEmpty()) {
            for (superClass in DescriptorUtils.getSuperclassDescriptors(queue.removeFirst())) {
                when (isLeaf[superClass]) {
                    true -> isLeaf[superClass] = false
                    false -> {}
                    else -> {
                        isLeaf[superClass] = false
                        queue.addLast(superClass)
                    }
                }
            }
        }

        return isLeaf.filterValues { it }.keys
    }

    private fun getBoundClass(type: KotlinType): ClassDescriptor? {
        return when (konst declaration = type.constructor.declarationDescriptor) {
            is ClassDescriptor -> declaration
            is TypeAliasDescriptor -> getBoundClass(declaration.expandedType)
            else -> null
        }
    }

    private fun collectSuperClasses(type: KotlinType): Set<ClassDescriptor> {
        konst initialClass = getBoundClass(type) ?: return emptySet()

        konst result = mutableSetOf<ClassDescriptor>()
        result.add(initialClass)

        konst queue = ArrayDeque<ClassDescriptor>()
        queue.addLast(initialClass)
        while (queue.isNotEmpty()) {
            konst current = queue.removeFirst()
            konst supertypes = DescriptorUtils.getSuperclassDescriptors(current)
            supertypes.filterNotTo(queue) { it !in result }
            result.addAll(supertypes)
        }

        return result
    }

    private fun areClassesOrInterfacesIncompatible(classesOrInterfaces: Collection<ClassDescriptor>): Boolean {
        konst classes = classesOrInterfaces.filter { !it.isInterfaceLike }
        return when {
            classes.size >= 2 -> true
            !classes.any { it.isFinalOrEnum } -> false
            classesOrInterfaces.size > classes.size -> true
            else -> false
        }
    }
}

private class KtFe10BuiltinTypes(private konst analysisContext: Fe10AnalysisContext) : KtBuiltinTypes() {
    override konst token: KtLifetimeToken
        get() = analysisContext.token

    override konst INT: KtType
        get() = withValidityAssertion { analysisContext.builtIns.intType.toKtType(analysisContext) }

    override konst LONG: KtType
        get() = withValidityAssertion { analysisContext.builtIns.longType.toKtType(analysisContext) }

    override konst SHORT: KtType
        get() = withValidityAssertion { analysisContext.builtIns.shortType.toKtType(analysisContext) }

    override konst BYTE: KtType
        get() = withValidityAssertion { analysisContext.builtIns.byteType.toKtType(analysisContext) }

    override konst FLOAT: KtType
        get() = withValidityAssertion { analysisContext.builtIns.floatType.toKtType(analysisContext) }

    override konst DOUBLE: KtType
        get() = withValidityAssertion { analysisContext.builtIns.doubleType.toKtType(analysisContext) }

    override konst BOOLEAN: KtType
        get() = withValidityAssertion { analysisContext.builtIns.booleanType.toKtType(analysisContext) }

    override konst CHAR: KtType
        get() = withValidityAssertion { analysisContext.builtIns.charType.toKtType(analysisContext) }

    override konst STRING: KtType
        get() = withValidityAssertion { analysisContext.builtIns.stringType.toKtType(analysisContext) }

    override konst UNIT: KtType
        get() = withValidityAssertion { analysisContext.builtIns.unitType.toKtType(analysisContext) }

    override konst NOTHING: KtType
        get() = withValidityAssertion { analysisContext.builtIns.nothingType.toKtType(analysisContext) }

    override konst ANY: KtType
        get() = withValidityAssertion { analysisContext.builtIns.anyType.toKtType(analysisContext) }

    override konst THROWABLE: KtType
        get() = withValidityAssertion { analysisContext.builtIns.throwable.defaultType.toKtType(analysisContext) }

    override konst NULLABLE_ANY: KtType
        get() = withValidityAssertion { analysisContext.builtIns.nullableAnyType.toKtType(analysisContext) }

    override konst NULLABLE_NOTHING: KtType
        get() = withValidityAssertion { analysisContext.builtIns.nullableNothingType.toKtType(analysisContext) }

}
