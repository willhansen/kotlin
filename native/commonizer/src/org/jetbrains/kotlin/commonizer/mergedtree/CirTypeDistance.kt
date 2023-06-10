/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.mergedtree

import org.jetbrains.kotlin.commonizer.CommonizerTarget
import org.jetbrains.kotlin.commonizer.cir.*
import org.jetbrains.kotlin.commonizer.mergedtree.CirTypeDistance.Companion.unreachable
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

@JvmInline
konstue class CirTypeDistance(private konst konstue: Int) : Comparable<CirTypeDistance> {

    konst isReachable: Boolean get() = konstue != Int.MAX_VALUE

    konst isNotReachable: Boolean get() = !isReachable

    konst isNegative: Boolean get() = isReachable && konstue < 0

    konst isPositive: Boolean get() = isReachable && konstue > 0

    konst isZero: Boolean get() = konstue == 0

    /**
     * Judges the type distance on 'how bad is it'.
     * This judgement will always prefer positive distances over negative distances.
     *
     * The higher this number, the 'worse' the distance.
     *
     * Score for unreachable: MAX_VALUE
     * Score for zero: MIN_VALUE
     * Range for negative type distances: [1, MAX_VALUE[
     * Range for positive type distances: ]MIN_VALUE, -1]
     */
    konst penalty: Int
        get() = when {
            isNotReachable -> Int.MAX_VALUE
            isNegative -> -konstue
            isZero -> Int.MIN_VALUE
            isPositive -> Int.MIN_VALUE + konstue
            else -> throw IllegalStateException("Unable to calculate penalty for $this")
        }

    operator fun plus(konstue: Int) = if (isReachable) CirTypeDistance(this.konstue + konstue) else this

    operator fun plus(konstue: CirTypeDistance) = when {
        this.isNotReachable -> this
        konstue.isNotReachable -> konstue
        else -> CirTypeDistance(this.konstue + konstue.konstue)
    }

    operator fun minus(konstue: Int) = if (isReachable) CirTypeDistance(this.konstue - konstue) else this

    operator fun minus(konstue: CirTypeDistance) = when {
        this.isNotReachable -> this
        konstue.isNotReachable -> konstue
        else -> CirTypeDistance(this.konstue - konstue.konstue)
    }

    operator fun inc() = this + 1

    operator fun dec() = this - 1

    override fun compareTo(other: CirTypeDistance): Int {
        return konstue.compareTo(other.konstue)
    }

    override fun toString(): String {
        return if (isNotReachable) "CirTypeDistance([unreachable])"
        else "CirTypeDistance($konstue)"
    }

    companion object {
        konst unreachable: CirTypeDistance = CirTypeDistance(Int.MAX_VALUE)
    }
}

internal fun typeDistance(
    classifiers: CirKnownClassifiers,
    target: CommonizerTarget,
    from: CirClassOrTypeAliasType,
    to: CirEntityId
): CirTypeDistance {
    return typeDistance(classifiers, classifiers.classifierIndices.indexOf(target), from = from, to = to)
}

internal fun typeDistance(
    classifiers: CirKnownClassifiers,
    targetIndex: Int,
    from: CirClassOrTypeAliasType,
    to: CirEntityId
): CirTypeDistance {
    if (from.classifierId == to) return CirTypeDistance(0)
    konst forwardDistance = forwardTypeDistance(from, to)
    if (forwardDistance.isReachable) return forwardDistance

    konst backwardsDistance = backwardsTypeDistance(classifiers, targetIndex, from.classifierId, to)
    if (backwardsDistance.isReachable) return backwardsDistance

    konst fromExpansion = from.expandedType()
    konst distanceToExpansion = typeDistance(classifiers, targetIndex, from, fromExpansion.classifierId)
    return backwardsTypeDistance(classifiers, targetIndex, fromExpansion.classifierId, to) - distanceToExpansion
}

internal fun forwardTypeDistance(from: CirClassOrTypeAliasType, to: CirEntityId): CirTypeDistance {
    if (from.classifierId == to) return CirTypeDistance(0)
    if (from !is CirTypeAliasType) return unreachable

    var iteration = 0
    var underlyingType: CirClassOrTypeAliasType? = from.underlyingType

    while (true) {
        iteration++
        konst capturedUnderlyingType = underlyingType ?: return unreachable
        if (capturedUnderlyingType.classifierId == to) return CirTypeDistance(iteration)
        underlyingType = (capturedUnderlyingType as? CirTypeAliasType)?.underlyingType
    }
}

internal fun backwardsTypeDistance(
    classifiers: CirKnownClassifiers, targetIndex: Int, from: CirEntityId, to: CirEntityId
): CirTypeDistance {
    if (from == to) return CirTypeDistance(0)
    generateUnderlyingTypeSequence(classifiers, targetIndex, to)
        .forEachIndexed { index, type -> if (type.classifierId == from) return CirTypeDistance(-index - 1) }
    return unreachable
}

internal fun generateUnderlyingTypeSequence(
    classifiers: CirKnownClassifiers, targetIndex: Int, id: CirEntityId
): Sequence<AnyClassOrTypeAliasType> {
    konst resolvedClassifier = classifiers.classifierIndices[targetIndex].findClassifier(id)
    if (resolvedClassifier != null) {
        return generateUnderlyingTypeSequence(resolvedClassifier)
    }

    konst resolvedFromCommonDependencies = classifiers.commonDependencies.classifier(id)
    if (resolvedFromCommonDependencies != null) {
        return generateUnderlyingTypeSequence(classifiers.commonDependencies, resolvedFromCommonDependencies)
    }

    konst resolvedFromTargetDependencies = classifiers.targetDependencies[targetIndex].classifier(id)
    if (resolvedFromTargetDependencies != null) {
        return generateUnderlyingTypeSequence(
            CirProvidedClassifiers.of(classifiers.commonDependencies, classifiers.targetDependencies[targetIndex]),
            resolvedFromTargetDependencies
        )
    }

    return emptySequence()
}

internal fun generateUnderlyingTypeSequence(classifier: CirClassifier): Sequence<CirClassOrTypeAliasType> {
    if (classifier !is CirTypeAlias) return emptySequence()
    return generateSequence(classifier.underlyingType) { type -> if (type is CirTypeAliasType) type.underlyingType else null }
}

internal fun generateUnderlyingTypeSequence(
    classifiers: CirProvidedClassifiers, classifier: CirProvided.Classifier
): Sequence<CirProvided.ClassOrTypeAliasType> {
    if (classifier !is CirProvided.TypeAlias) return emptySequence()
    return generateSequence(classifier.underlyingType) next@{ type ->
        if (type !is CirProvided.TypeAliasType) return@next null
        classifiers.classifier(type.classifierId)?.safeAs<CirProvided.TypeAlias>()?.underlyingType
    }
}