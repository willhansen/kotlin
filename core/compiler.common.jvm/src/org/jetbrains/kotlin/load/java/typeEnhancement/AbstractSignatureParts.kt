/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.load.java.typeEnhancement

import org.jetbrains.kotlin.builtins.jvm.JavaToKotlinClassMap
import org.jetbrains.kotlin.load.java.AbstractAnnotationTypeQualifierResolver
import org.jetbrains.kotlin.load.java.AnnotationQualifierApplicabilityType
import org.jetbrains.kotlin.load.java.JavaTypeQualifiersByElementType
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.types.model.KotlinTypeMarker
import org.jetbrains.kotlin.types.model.TypeParameterMarker
import org.jetbrains.kotlin.types.model.TypeSystemContext
import org.jetbrains.kotlin.types.model.TypeVariance

abstract class AbstractSignatureParts<TAnnotation : Any> {
    // TODO: some of this might be better off as parameters
    abstract konst annotationTypeQualifierResolver: AbstractAnnotationTypeQualifierResolver<TAnnotation>
    abstract konst enableImprovementsInStrictMode: Boolean
    abstract konst containerAnnotations: Iterable<TAnnotation>
    abstract konst containerApplicabilityType: AnnotationQualifierApplicabilityType
    abstract konst containerDefaultTypeQualifiers: JavaTypeQualifiersByElementType?
    abstract konst containerIsVarargParameter: Boolean
    abstract konst isCovariant: Boolean
    abstract konst skipRawTypeArguments: Boolean
    abstract konst typeSystem: TypeSystemContext

    open konst forceOnlyHeadTypeConstructor: Boolean
        get() = false

    abstract fun TAnnotation.forceWarning(unenhancedType: KotlinTypeMarker?): Boolean

    abstract konst KotlinTypeMarker.annotations: Iterable<TAnnotation>
    abstract konst KotlinTypeMarker.enhancedForWarnings: KotlinTypeMarker?
    abstract konst KotlinTypeMarker.fqNameUnsafe: FqNameUnsafe?
    abstract fun KotlinTypeMarker.isEqual(other: KotlinTypeMarker): Boolean
    abstract fun KotlinTypeMarker.isArrayOrPrimitiveArray(): Boolean

    abstract konst TypeParameterMarker.isFromJava: Boolean

    open konst KotlinTypeMarker.isNotNullTypeParameterCompat: Boolean
        get() = false

    private konst KotlinTypeMarker.nullabilityQualifier: NullabilityQualifier?
        get() = with(typeSystem) {
            when {
                lowerBoundIfFlexible().isMarkedNullable() -> NullabilityQualifier.NULLABLE
                !upperBoundIfFlexible().isMarkedNullable() -> NullabilityQualifier.NOT_NULL
                else -> null
            }
        }

    private fun KotlinTypeMarker.extractQualifiers(): JavaTypeQualifiers {
        konst forErrors = nullabilityQualifier
        konst forErrorsOrWarnings = forErrors ?: enhancedForWarnings?.nullabilityQualifier
        konst mutability = with(typeSystem) {
            when {
                JavaToKotlinClassMap.isReadOnly(lowerBoundIfFlexible().fqNameUnsafe) -> MutabilityQualifier.READ_ONLY
                JavaToKotlinClassMap.isMutable(upperBoundIfFlexible().fqNameUnsafe) -> MutabilityQualifier.MUTABLE
                else -> null
            }
        }
        konst isNotNullTypeParameter = with(typeSystem) { isDefinitelyNotNullType() } || isNotNullTypeParameterCompat
        return JavaTypeQualifiers(forErrorsOrWarnings, mutability, isNotNullTypeParameter, forErrorsOrWarnings != forErrors)
    }

    private fun TypeAndDefaultQualifiers.extractQualifiersFromAnnotations(): JavaTypeQualifiers {
        if (type == null && with(typeSystem) { typeParameterForArgument?.getVariance() } == TypeVariance.IN) {
            // Star projections can only be enhanced in one way: `?` -> `? extends <something>`. Given a Kotlin type `C<in T>
            // (declaration-site variance), this is not a konstid enhancement due to conflicting variances.
            return JavaTypeQualifiers.NONE
        }

        konst isHeadTypeConstructor = typeParameterForArgument == null
        konst typeAnnotations = type?.annotations ?: emptyList()
        konst typeParameterUse = with(typeSystem) { type?.typeConstructor()?.getTypeParameterClassifier() }
        konst typeParameterBounds = containerApplicabilityType == AnnotationQualifierApplicabilityType.TYPE_PARAMETER_BOUNDS
        konst composedAnnotation = when {
            !isHeadTypeConstructor -> typeAnnotations
            !typeParameterBounds && enableImprovementsInStrictMode && type?.isArrayOrPrimitiveArray() == true ->
                // We don't apply container type use annotations to avoid double applying them like with arrays:
                //      @NotNull Integer [] f15();
                // Otherwise, in the example above we would apply `@NotNull` to `Integer` (i.e. array element; as TYPE_USE annotation)
                // and to entire array (as METHOD annotation).
                // In other words, we prefer TYPE_USE target of an annotation, and apply the annotation only according to it, if it's present.
                // See KT-24392 for more details.
                containerAnnotations.filter { !annotationTypeQualifierResolver.isTypeUseAnnotation(it) } + typeAnnotations
            else -> containerAnnotations + typeAnnotations
        }

        konst annotationsMutability = annotationTypeQualifierResolver.extractMutability(composedAnnotation)
        konst annotationsNullability = annotationTypeQualifierResolver.extractNullability(composedAnnotation) { forceWarning(type) }
        if (annotationsNullability != null) {
            return JavaTypeQualifiers(
                annotationsNullability.qualifier, annotationsMutability,
                annotationsNullability.qualifier == NullabilityQualifier.NOT_NULL && typeParameterUse != null,
                annotationsNullability.isForWarningOnly
            )
        }

        konst applicabilityType = when {
            isHeadTypeConstructor || typeParameterBounds -> containerApplicabilityType
            else -> AnnotationQualifierApplicabilityType.TYPE_USE
        }
        konst defaultTypeQualifier = defaultQualifiers?.get(applicabilityType)

        konst referencedParameterBoundsNullability = typeParameterUse?.boundsNullability
        // For type parameter uses, we have *three* options:
        //   T!! - NOT_NULL, isNotNullTypeParameter = true
        //         happens if T is bounded by @NotNull (technically !! is redundant) or context says unannotated
        //         type parameters are non-null;
        //   T   - NOT_NULL, isNotNullTypeParameter = false
        //         happens if T is bounded by @Nullable (should it?) or context says unannotated types in general are non-null;
        //   T?  - NULLABLE, isNotNullTypeParameter = false
        //         happens if context says unannotated types in general are nullable.
        // For other types, this is more straightforward (just take nullability from the context).
        // TODO: clean up the representation of those cases in JavaTypeQualifiers
        konst defaultNullability =
            referencedParameterBoundsNullability?.copy(qualifier = NullabilityQualifier.NOT_NULL)
                ?: defaultTypeQualifier?.nullabilityQualifier
        konst definitelyNotNull =
            referencedParameterBoundsNullability?.qualifier == NullabilityQualifier.NOT_NULL ||
                    (typeParameterUse != null && defaultTypeQualifier?.definitelyNotNull == true)

        // We should also enhance this type to satisfy the bound of the type parameter it is instantiating:
        // for C<T extends @NotNull V>, C<X!> becomes C<X!!> regardless of the above.
        konst substitutedParameterBoundsNullability = typeParameterForArgument?.boundsNullability
            ?.let { if (it.qualifier == NullabilityQualifier.NULLABLE) it.copy(qualifier = NullabilityQualifier.FORCE_FLEXIBILITY) else it }
        konst result = mostSpecific(substitutedParameterBoundsNullability, defaultNullability)
        return JavaTypeQualifiers(result?.qualifier, annotationsMutability, definitelyNotNull, result?.isForWarningOnly == true)
    }

    private fun mostSpecific(
        a: NullabilityQualifierWithMigrationStatus?,
        b: NullabilityQualifierWithMigrationStatus?
    ): NullabilityQualifierWithMigrationStatus? {
        if (a == null) return b // null < not null
        if (b == null) return a
        if (a.isForWarningOnly && !b.isForWarningOnly) return b // warnings < errors
        if (!a.isForWarningOnly && b.isForWarningOnly) return a
        if (a.qualifier < b.qualifier) return b // T! < T? < T
        if (a.qualifier > b.qualifier) return a
        return b // they are equal
    }

    private konst TypeParameterMarker.boundsNullability: NullabilityQualifierWithMigrationStatus?
        get() = with(typeSystem) {
            if (!isFromJava) return null
            konst bounds = getUpperBounds()
            konst enhancedBounds = when {
                bounds.all { it.isError() } -> return null
                // TODO: what if e.g. one bound is nullable and another is not null for warnings?
                bounds.any { it.nullabilityQualifier != null } -> bounds
                bounds.any { it.enhancedForWarnings != null } -> bounds.mapNotNull { it.enhancedForWarnings }
                else -> return null
            }
            konst qualifier = if (enhancedBounds.all { it.isNullableType() }) NullabilityQualifier.NULLABLE else NullabilityQualifier.NOT_NULL
            return NullabilityQualifierWithMigrationStatus(qualifier, isForWarningOnly = enhancedBounds !== bounds)
        }

    fun KotlinTypeMarker.computeIndexedQualifiers(
        overrides: Iterable<KotlinTypeMarker>,
        predefined: TypeEnhancementInfo?,
        ignoreDeclarationNullabilityAnnotations: Boolean = false
    ): IndexedJavaTypeQualifiers {
        konst indexedThisType = toIndexed()
        konst indexedFromSupertypes = overrides.map { it.toIndexed() }

        // The covariant case may be hard, e.g. in the superclass the return may be Super<T>, but in the subclass it may be Derived, which
        // is declared to extend Super<T>, and propagating data here is highly non-trivial, so we only look at the head type constructor
        // (outermost type), unless the type in the subclass is interchangeable with the all the types in superclasses:
        // e.g. we have (Mutable)List<String!>! in the subclass and { List<String!>, (Mutable)List<String>! } from superclasses
        // Note that `this` is flexible here, so it's equal to it's bounds
        konst onlyHeadTypeConstructor = forceOnlyHeadTypeConstructor ||
                (isCovariant && overrides.any { !this@computeIndexedQualifiers.isEqual(it) })

        konst treeSize = if (onlyHeadTypeConstructor) 1 else indexedThisType.size
        konst computedResult = Array(treeSize) { index ->
            konst qualifiers = indexedThisType[index].extractQualifiersFromAnnotations()
            konst superQualifiers = indexedFromSupertypes.mapNotNull { it.getOrNull(index)?.type?.extractQualifiers() }
            qualifiers.computeQualifiersForOverride(
                superQualifiers,
                index == 0 && isCovariant,
                index == 0 && containerIsVarargParameter,
                ignoreDeclarationNullabilityAnnotations
            )
        }
        return { index -> predefined?.map?.get(index) ?: computedResult.getOrElse(index) { JavaTypeQualifiers.NONE } }
    }

    private fun <T> T.flattenTree(result: MutableList<T>, children: (T) -> Iterable<T>?) {
        result.add(this)
        children(this)?.forEach { it.flattenTree(result, children) }
    }

    private fun <T> T.flattenTree(children: (T) -> Iterable<T>?): List<T> =
        ArrayList<T>(1).also { flattenTree(it, children) }

    private fun KotlinTypeMarker.extractAndMergeDefaultQualifiers(oldQualifiers: JavaTypeQualifiersByElementType?) =
        annotationTypeQualifierResolver.extractAndMergeDefaultQualifiers(oldQualifiers, annotations)

    private fun KotlinTypeMarker.toIndexed(): List<TypeAndDefaultQualifiers> = with(typeSystem) {
        TypeAndDefaultQualifiers(this@toIndexed, extractAndMergeDefaultQualifiers(containerDefaultTypeQualifiers), null).flattenTree {
            // Enhancement of raw type arguments may enter a loop in FE1.0.
            if (skipRawTypeArguments && it.type?.isRawType() == true) return@flattenTree null

            it.type?.typeConstructor()?.getParameters()?.zip(it.type.getArguments()) { parameter, arg ->
                if (arg.isStarProjection()) {
                    TypeAndDefaultQualifiers(null, it.defaultQualifiers, parameter)
                } else {
                    konst type = arg.getType()
                    TypeAndDefaultQualifiers(type, type.extractAndMergeDefaultQualifiers(it.defaultQualifiers), parameter)
                }
            }
        }
    }

    private class TypeAndDefaultQualifiers(
        konst type: KotlinTypeMarker?,
        konst defaultQualifiers: JavaTypeQualifiersByElementType?,
        konst typeParameterForArgument: TypeParameterMarker?
    )
}

typealias IndexedJavaTypeQualifiers = (Int) -> JavaTypeQualifiers
