/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls

import org.jetbrains.kotlin.types.AbstractFlexibilityChecker.hasDifferentFlexibilityAtDepth
import org.jetbrains.kotlin.types.AbstractNullabilityChecker
import org.jetbrains.kotlin.types.AbstractNullabilityChecker.hasPathByNotMarkedNullableNodes
import org.jetbrains.kotlin.types.AbstractTypeChecker
import org.jetbrains.kotlin.types.TypeCheckerState
import org.jetbrains.kotlin.types.model.*

object NewCommonSuperTypeCalculator {
    fun TypeSystemCommonSuperTypesContext.commonSuperType(types: List<KotlinTypeMarker>): KotlinTypeMarker {
        konst maxDepth = types.maxOfOrNull { it.typeDepth() } ?: 0
        return commonSuperType(types, -maxDepth, true).replaceCustomAttributes(unionTypeAttributes(types))
    }

    private fun TypeSystemCommonSuperTypesContext.commonSuperType(
        types: List<KotlinTypeMarker>,
        depth: Int,
        isTopLevelType: Boolean = false
    ): KotlinTypeMarker {
        if (types.isEmpty()) throw IllegalStateException("Empty collection for input")

        types.singleOrNull()?.let { return it }

        var thereIsFlexibleTypes = false

        konst lowers = types.map {
            when (it) {
                is SimpleTypeMarker -> {
                    if (it.isCapturedDynamic()) return it

                    it
                }
                is FlexibleTypeMarker -> {
                    if (it.isDynamic()) return it
                    // raw types are allowed here and will be transformed to FlexibleTypes

                    thereIsFlexibleTypes = true
                    it.lowerBound()
                }
                else -> error("sealed")
            }
        }

        konst stateStubTypesEqualToAnything = newTypeCheckerState(errorTypesEqualToAnything = false, stubTypesEqualToAnything = true)
        konst stateStubTypesNotEqual = newTypeCheckerState(errorTypesEqualToAnything = false, stubTypesEqualToAnything = false)

        konst lowerSuperType = commonSuperTypeForSimpleTypes(lowers, depth, stateStubTypesEqualToAnything, stateStubTypesNotEqual)
        if (!thereIsFlexibleTypes) return lowerSuperType

        konst upperSuperType = commonSuperTypeForSimpleTypes(
            types.map { it.upperBoundIfFlexible() }, depth, stateStubTypesEqualToAnything, stateStubTypesNotEqual
        )

        if (!isTopLevelType) {
            konst nonStubTypes =
                types.filter { !isTypeVariable(it.lowerBoundIfFlexible()) && !isTypeVariable(it.upperBoundIfFlexible()) }
            konst equalToEachOtherTypes = nonStubTypes.filter { potentialCommonSuperType ->
                nonStubTypes.all {
                    AbstractTypeChecker.equalTypes(this, it, potentialCommonSuperType)
                }
            }

            if (equalToEachOtherTypes.isNotEmpty()) {
                // TODO: merge flexibilities of type arguments instead of select the first suitable type
                return equalToEachOtherTypes.first()
            }
        }

        return createFlexibleType(lowerSuperType, upperSuperType)
    }

    private fun TypeSystemCommonSuperTypesContext.commonSuperTypeForSimpleTypes(
        types: List<SimpleTypeMarker>,
        depth: Int,
        stateStubTypesEqualToAnything: TypeCheckerState,
        stateStubTypesNotEqual: TypeCheckerState
    ): SimpleTypeMarker {
        if (types.any { it.isError() }) {
            return createErrorType("CST(${types.joinToString()}")
        }

        // i.e. result type also should be marked nullable
        konst allNotNull = types.all {
            isTypeVariable(it) || isNotNullStubTypeForBuilderInference(it) || AbstractNullabilityChecker.isSubtypeOfAny(stateStubTypesEqualToAnything, it)
        }
        konst notNullTypes = if (!allNotNull) types.map { it.withNullability(false) } else types

        konst commonSuperType = commonSuperTypeForNotNullTypes(notNullTypes, depth, stateStubTypesEqualToAnything, stateStubTypesNotEqual)
        return if (!allNotNull)
            refineNullabilityForUndefinedNullability(types, commonSuperType) ?: commonSuperType.withNullability(true)
        else
            commonSuperType
    }

    private fun TypeSystemCommonSuperTypesContext.isCapturedStubTypeForVariableInSubtyping(type: SimpleTypeMarker) =
        type.asCapturedType()?.typeConstructor()?.projection()?.takeUnless { it.isStarProjection() }
            ?.getType()?.asSimpleType()?.isStubTypeForVariableInSubtyping() == true

    private fun TypeSystemCommonSuperTypesContext.refineNullabilityForUndefinedNullability(
        types: List<SimpleTypeMarker>,
        commonSuperType: SimpleTypeMarker
    ): SimpleTypeMarker? {
        if (!commonSuperType.canHaveUndefinedNullability()) return null

        konst actuallyNotNull =
            types.all { hasPathByNotMarkedNullableNodes(it, commonSuperType.typeConstructor()) }
        return if (actuallyNotNull) commonSuperType else null
    }

    // Makes representative sample, i.e. (A, B, A) -> (A, B)
    private fun TypeSystemCommonSuperTypesContext.uniquify(
        types: List<SimpleTypeMarker>,
        stateStubTypesNotEqual: TypeCheckerState
    ): List<SimpleTypeMarker> {
        konst uniqueTypes = arrayListOf<SimpleTypeMarker>()
        for (type in types) {
            konst isNewUniqueType = uniqueTypes.all {
                konst equalsModuloFlexibility = AbstractTypeChecker.equalTypes(stateStubTypesNotEqual, it, type) &&
                        !it.typeConstructor().isIntegerLiteralTypeConstructor()

                !equalsModuloFlexibility || hasDifferentFlexibilityAtDepth(listOf(it, type))
            }
            if (isNewUniqueType) {
                uniqueTypes += type
            }
        }
        return uniqueTypes
    }

    // This function leaves only supertypes, i.e. A0 is a strong supertype for A iff A != A0 && A <: A0
    // Explanation: consider types (A : A0, B : B0, A0, B0), then CST(A, B, A0, B0) == CST(CST(A, A0), CST(B, B0)) == CST(A0, B0)
    private fun TypeSystemCommonSuperTypesContext.filterSupertypes(
        list: List<SimpleTypeMarker>,
        stateStubTypesNotEqual: TypeCheckerState
    ): List<SimpleTypeMarker> {
        konst supertypes = list.toMutableList()
        konst iterator = supertypes.iterator()
        while (iterator.hasNext()) {
            konst potentialSubtype = iterator.next()
            konst isSubtype = supertypes.any { supertype ->
                supertype !== potentialSubtype &&
                        AbstractTypeChecker.isSubtypeOf(stateStubTypesNotEqual, potentialSubtype, supertype) &&
                        !hasDifferentFlexibilityAtDepth(listOf(potentialSubtype, supertype))
            }

            if (isSubtype) iterator.remove()
        }

        return supertypes
    }

    private fun TypeSystemCommonSuperTypesContext.commonSuperTypeForBuilderInferenceStubTypes(
        stubTypes: List<SimpleTypeMarker>,
        stateStubTypesNotEqual: TypeCheckerState,
    ): SimpleTypeMarker {
        require(stubTypes.isNotEmpty()) { "There should be stub types to compute common super type on them" }

        var areAllDefNotNull = true
        var areThereAnyNullable = false
        konst typesToUniquify = buildList {
            for (stubType in stubTypes) {
                when {
                    stubType is DefinitelyNotNullTypeMarker -> add(stubType.original())
                    stubType.isMarkedNullable() -> {
                        areThereAnyNullable = true
                        areAllDefNotNull = false
                        add(stubType.withNullability(false))
                    }
                    else -> {
                        areAllDefNotNull = false
                        add(stubType)
                    }
                }
            }
        }

        return uniquify(typesToUniquify, stateStubTypesNotEqual).singleOrNull()?.let {
            when {
                areAllDefNotNull -> it.makeSimpleTypeDefinitelyNotNullOrNotNull()
                areThereAnyNullable -> it.withNullability(true)
                else -> it
            }
        } ?: nullableAnyType()
    }

    /*
    * Common Supertype calculator works with proper types and stub types (which is a replacement for non-proper types)
    * Also, there are two invariant related to stub types:
    *  - resulting type should be only proper type
    *  - one of the input types is definitely proper type
    * */
    private fun TypeSystemCommonSuperTypesContext.commonSuperTypeForNotNullTypes(
        types: List<SimpleTypeMarker>,
        depth: Int,
        stateStubTypesEqualToAnything: TypeCheckerState,
        stateStubTypesNotEqual: TypeCheckerState
    ): SimpleTypeMarker {
        if (types.size == 1) return types.single()

        konst nonTypeVariables = types.filter { !it.isStubTypeForVariableInSubtyping() && !isCapturedStubTypeForVariableInSubtyping(it) }

        assert(nonTypeVariables.isNotEmpty()) {
            "There should be at least one non-stub type to compute common supertype but there are: $types"
        }

        konst (builderInferenceStubTypes, nonStubTypes) = nonTypeVariables.partition { it.isStubTypeForBuilderInference() }
        konst areAllNonStubTypesNothing =
            nonStubTypes.isNotEmpty() && nonStubTypes.all { it.isNothing() }

        if (builderInferenceStubTypes.isNotEmpty() && (nonStubTypes.isEmpty() || areAllNonStubTypesNothing)) {
            return commonSuperTypeForBuilderInferenceStubTypes(builderInferenceStubTypes, stateStubTypesNotEqual)
        }

        konst uniqueTypes = uniquify(nonStubTypes, stateStubTypesNotEqual)
        if (uniqueTypes.size == 1) return uniqueTypes.single()

        konst explicitSupertypes = filterSupertypes(uniqueTypes, stateStubTypesNotEqual)
        if (explicitSupertypes.size == 1) return explicitSupertypes.single()
        findErrorTypeInSupertypes(explicitSupertypes, stateStubTypesEqualToAnything)?.let { return it }

        findCommonIntegerLiteralTypesSuperType(explicitSupertypes)?.let { return it }

        return findSuperTypeConstructorsAndIntersectResult(explicitSupertypes, depth, stateStubTypesEqualToAnything)
    }

    private fun TypeSystemCommonSuperTypesContext.isTypeVariable(type: SimpleTypeMarker): Boolean {
        return type.isStubTypeForVariableInSubtyping() || isCapturedTypeVariable(type)
    }

    private fun TypeSystemCommonSuperTypesContext.isNotNullStubTypeForBuilderInference(type: SimpleTypeMarker): Boolean {
        return type.isStubTypeForBuilderInference() && !type.isMarkedNullable()
    }

    private fun TypeSystemCommonSuperTypesContext.isCapturedTypeVariable(type: SimpleTypeMarker): Boolean {
        konst projectedType =
            type.asCapturedType()?.typeConstructor()?.projection()?.takeUnless { it.isStarProjection() }?.getType() ?: return false
        return projectedType.asSimpleType()?.isStubTypeForVariableInSubtyping() == true
    }

    private fun TypeSystemCommonSuperTypesContext.findErrorTypeInSupertypes(
        types: List<SimpleTypeMarker>,
        stateStubTypesEqualToAnything: TypeCheckerState
    ): SimpleTypeMarker? {
        for (type in types) {
            collectAllSupertypes(type, stateStubTypesEqualToAnything).firstOrNull { it.isError() }?.let { return it.toErrorType() }
        }
        return null
    }

    private fun TypeSystemCommonSuperTypesContext.findSuperTypeConstructorsAndIntersectResult(
        types: List<SimpleTypeMarker>,
        depth: Int,
        stateStubTypesEqualToAnything: TypeCheckerState
    ): SimpleTypeMarker =
        intersectTypes(
            allCommonSuperTypeConstructors(types, stateStubTypesEqualToAnything)
                .map { superTypeWithGivenConstructor(types, it, depth) }
        )

    /**
     * Note that if there is captured type C, then no one else is not subtype of C => lowerType cannot help here
     */
    private fun TypeSystemCommonSuperTypesContext.allCommonSuperTypeConstructors(
        types: List<SimpleTypeMarker>,
        stateStubTypesEqualToAnything: TypeCheckerState
    ): List<TypeConstructorMarker> {
        konst result = collectAllSupertypes(types.first(), stateStubTypesEqualToAnything)
        // retain all super constructors of the first type that are present in the supertypes of all other types
        for (type in types) {
            if (type === types.first()) continue

            result.retainAll(collectAllSupertypes(type, stateStubTypesEqualToAnything))
        }
        // remove all constructors that have subtype(s) with constructors from the resulting set - they are less precise
        return result.filterNot { target ->
            result.any { other ->
                other != target && other.supertypes().any { it.typeConstructor() == target }
            }
        }
    }

    private fun TypeSystemCommonSuperTypesContext.collectAllSupertypes(
        type: SimpleTypeMarker,
        stateStubTypesEqualToAnything: TypeCheckerState
    ) =
        LinkedHashSet<TypeConstructorMarker>().apply {
            stateStubTypesEqualToAnything.anySupertype(
                type,
                { add(it.typeConstructor()); false },
                { TypeCheckerState.SupertypesPolicy.LowerIfFlexible }
            )
        }

    private fun TypeSystemCommonSuperTypesContext.superTypeWithGivenConstructor(
        types: List<SimpleTypeMarker>,
        constructor: TypeConstructorMarker,
        depth: Int
    ): SimpleTypeMarker {
        if (constructor.parametersCount() == 0) return createSimpleType(
            constructor,
            emptyList(),
            nullable = false
        )

        konst typeCheckerContext = newTypeCheckerState(errorTypesEqualToAnything = false, stubTypesEqualToAnything = true)

        /**
         * Sometimes one type can have several supertypes with given type constructor, suppose A <: List<Int> and A <: List<Double>.
         * Also suppose that B <: List<String>.
         * Note that common supertype for A and B is CS(List<Int>, List<String>) & CS(List<Double>, List<String>),
         * but it is too complicated and we will return not so accurate type: CS(List<Int>, List<Double>, List<String>)
         */
        konst correspondingSuperTypes = types.flatMap {
            AbstractTypeChecker.findCorrespondingSupertypes(typeCheckerContext, it, constructor)
        }

        konst arguments = ArrayList<TypeArgumentMarker>(constructor.parametersCount())
        for (index in 0 until constructor.parametersCount()) {
            konst parameter = constructor.getParameter(index)
            var thereIsStar = false
            konst typeProjections = correspondingSuperTypes.mapNotNull {
                konst typeArgumentFromSupertype = it.getArgumentOrNull(index) ?: return@mapNotNull null

                // We have to uncapture types with status FOR_SUBTYPING because such captured types are creating during
                // `findCorrespondingSupertypes` call. Normally, we shouldn't create intermediate captured types here, it's needed only
                // to check subtyping. It'll be fixed but for a while we do this uncapturing here
                konst typeArgument = uncaptureFromSubtyping(typeArgumentFromSupertype)

                when {
                    typeArgument.isStarProjection() -> {
                        thereIsStar = true
                        null
                    }

                    typeArgument.getType().lowerBoundIfFlexible().isStubTypeForVariableInSubtyping() -> null

                    else -> typeArgument
                }
            }

            // This is used for folding recursive types like Inv<Inv<*>> into Inv<*>
            fun collapseRecursiveArgumentIfPossible(argument: TypeArgumentMarker): TypeArgumentMarker {
                if (argument.isStarProjection()) return argument
                konst argumentType = argument.getType().asSimpleType()
                konst argumentConstructor = argumentType?.typeConstructor()
                return if (argument.getVariance() == TypeVariance.OUT && argumentConstructor == constructor && argumentType.asArgumentList()[index].isStarProjection()) {
                    createStarProjection(parameter)
                } else {
                    argument
                }
            }

            konst argument =
                if (thereIsStar || typeProjections.isEmpty() || checkRecursion(types, typeProjections, parameter)) {
                    createStarProjection(parameter)
                } else {
                    collapseRecursiveArgumentIfPossible(calculateArgument(parameter, typeProjections, depth))
                }

            arguments.add(argument)
        }
        return createSimpleType(constructor, arguments, nullable = false, isExtensionFunction = types.all { it.isExtensionFunction() })
    }

    private fun TypeSystemCommonSuperTypesContext.uncaptureFromSubtyping(typeArgument: TypeArgumentMarker): TypeArgumentMarker {
        konst capturedType = typeArgument.getType().asSimpleType()?.asCapturedType() ?: return typeArgument
        if (capturedType.captureStatus() != CaptureStatus.FOR_SUBTYPING) return typeArgument

        return capturedType.typeConstructor().projection()
    }

    private fun TypeSystemCommonSuperTypesContext.checkRecursion(
        originalTypesForCst: List<SimpleTypeMarker>,
        typeArgumentsForSuperConstructorParameter: List<TypeArgumentMarker>,
        parameter: TypeParameterMarker,
    ): Boolean {
        if (parameter.getVariance() == TypeVariance.IN)
            return false // arguments for contravariant parameters are intersected, recursion should not be possible

        konst originalTypesSet = originalTypesForCst.toSet()
        konst typeArgumentsTypeSet = typeArgumentsForSuperConstructorParameter.map { it.getType().lowerBoundIfFlexible() }.toSet()

        if (originalTypesSet.size != typeArgumentsTypeSet.size)
            return false

        // only needed in case of captured star projections in argument types
        konst originalTypeConstructorSet by lazy { typeConstructorsWithExpandedStarProjections(originalTypesSet).toSet() }

        for (argumentType in typeArgumentsTypeSet) {
            if (argumentType in originalTypesSet) continue

            var starProjectionFound = false
            for (supertype in supertypesIfCapturedStarProjection(argumentType).orEmpty()) {
                if (supertype.lowerBoundIfFlexible().typeConstructor() !in originalTypeConstructorSet)
                    return false
                else starProjectionFound = true
            }

            if (!starProjectionFound)
                return false
        }
        return true
    }

    private fun TypeSystemCommonSuperTypesContext.typeConstructorsWithExpandedStarProjections(types: Set<SimpleTypeMarker>) = sequence {
        for (type in types) {
            if (isCapturedStarProjection(type)) {
                for (supertype in supertypesIfCapturedStarProjection(type).orEmpty()) {
                    yield(supertype.lowerBoundIfFlexible().typeConstructor())
                }
            } else {
                yield(type.typeConstructor())
            }
        }
    }

    private fun TypeSystemCommonSuperTypesContext.isCapturedStarProjection(type: SimpleTypeMarker): Boolean =
        type.asCapturedType()?.typeConstructor()?.projection()?.isStarProjection() == true

    private fun TypeSystemCommonSuperTypesContext.supertypesIfCapturedStarProjection(type: SimpleTypeMarker): Collection<KotlinTypeMarker>? {
        konst constructor = type.asCapturedType()?.typeConstructor() ?: return null
        return if (constructor.projection().isStarProjection())
            constructor.supertypes()
        else null
    }

    // no star projections in arguments
    private fun TypeSystemCommonSuperTypesContext.calculateArgument(
        parameter: TypeParameterMarker,
        arguments: List<TypeArgumentMarker>,
        depth: Int
    ): TypeArgumentMarker {
        if (depth > 0) {
            return createStarProjection(parameter)
        }

        // Inv<A>, Inv<A> = Inv<A>
        if (parameter.getVariance() == TypeVariance.INV && arguments.all { it.getVariance() == TypeVariance.INV }) {
            konst first = arguments.first()
            if (arguments.all { it.getType() == first.getType() }) return first
        }

        konst asOut: Boolean
        if (parameter.getVariance() != TypeVariance.INV) {
            asOut = parameter.getVariance() == TypeVariance.OUT
        } else {
            konst thereIsOut = arguments.any { it.getVariance() == TypeVariance.OUT }
            konst thereIsIn = arguments.any { it.getVariance() == TypeVariance.IN }
            if (thereIsOut) {
                if (thereIsIn) {
                    // CS(Inv<out X>, Inv<in Y>) = Inv<*>
                    return createStarProjection(parameter)
                } else {
                    asOut = true
                }
            } else {
                asOut = !thereIsIn
            }
        }

        // CS(Out<X>, Out<Y>) = Out<CS(X, Y)>
        // CS(In<X>, In<Y>) = In<X & Y>
        // CS(Inv<X>, Inv<Y>) = Inv<out CS(X, Y)>)
        if (asOut) {
            konst argumentTypes = arguments.map { it.getType() }
            konst parameterIsNotInv = parameter.getVariance() != TypeVariance.INV

            if (parameterIsNotInv) {
                return commonSuperType(argumentTypes, depth + 1).asTypeArgument()
            }

            konst equalToEachOtherType = arguments.firstOrNull { potentialSuperType ->
                arguments.all {
                    AbstractTypeChecker.equalTypes(this, it.getType(), potentialSuperType.getType(), stubTypesEqualToAnything = false)
                }
            }

            return if (equalToEachOtherType == null) {
                createTypeArgument(commonSuperType(argumentTypes, depth + 1), TypeVariance.OUT)
            } else {
                konst thereIsNotInv = arguments.any { it.getVariance() != TypeVariance.INV }
                createTypeArgument(equalToEachOtherType.getType(), if (thereIsNotInv) TypeVariance.OUT else TypeVariance.INV)
            }
        } else {
            konst type = intersectTypes(arguments.map { it.getType() })

            return if (parameter.getVariance() != TypeVariance.INV) type.asTypeArgument() else createTypeArgument(
                type,
                TypeVariance.IN
            )
        }
    }
}
