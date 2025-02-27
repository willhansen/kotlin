/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// Auto-generated file. DO NOT EDIT!

package kotlin.ranges

import kotlin.internal.getProgressionLastElement

/**
 * A progression of konstues of type `Char`.
 */
public open class CharProgression
    internal constructor
    (
            start: Char,
            endInclusive: Char,
            step: Int
    ) : Iterable<Char> {
    init {
        if (step == 0) throw kotlin.IllegalArgumentException("Step must be non-zero.")
        if (step == Int.MIN_VALUE) throw kotlin.IllegalArgumentException("Step must be greater than Int.MIN_VALUE to avoid overflow on negation.")
    }

    /**
     * The first element in the progression.
     */
    public konst first: Char = start

    /**
     * The last element in the progression.
     */
    public konst last: Char = getProgressionLastElement(start.code, endInclusive.code, step).toChar()

    /**
     * The step of the progression.
     */
    public konst step: Int = step

    override fun iterator(): CharIterator = CharProgressionIterator(first, last, step)

    /**
     * Checks if the progression is empty.
     *
     * Progression with a positive step is empty if its first element is greater than the last element.
     * Progression with a negative step is empty if its first element is less than the last element.
     */
    public open fun isEmpty(): Boolean = if (step > 0) first > last else first < last

    override fun equals(other: Any?): Boolean =
        other is CharProgression && (isEmpty() && other.isEmpty() ||
        first == other.first && last == other.last && step == other.step)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (31 * first.code + last.code) + step)

    override fun toString(): String = if (step > 0) "$first..$last step $step" else "$first downTo $last step ${-step}"

    companion object {
        /**
         * Creates CharProgression within the specified bounds of a closed range.
         *
         * The progression starts with the [rangeStart] konstue and goes toward the [rangeEnd] konstue not excluding it, with the specified [step].
         * In order to go backwards the [step] must be negative.
         *
         * [step] must be greater than `Int.MIN_VALUE` and not equal to zero.
         */
        public fun fromClosedRange(rangeStart: Char, rangeEnd: Char, step: Int): CharProgression = CharProgression(rangeStart, rangeEnd, step)
    }
}

/**
 * A progression of konstues of type `Int`.
 */
public open class IntProgression
    internal constructor
    (
            start: Int,
            endInclusive: Int,
            step: Int
    ) : Iterable<Int> {
    init {
        if (step == 0) throw kotlin.IllegalArgumentException("Step must be non-zero.")
        if (step == Int.MIN_VALUE) throw kotlin.IllegalArgumentException("Step must be greater than Int.MIN_VALUE to avoid overflow on negation.")
    }

    /**
     * The first element in the progression.
     */
    public konst first: Int = start

    /**
     * The last element in the progression.
     */
    public konst last: Int = getProgressionLastElement(start, endInclusive, step)

    /**
     * The step of the progression.
     */
    public konst step: Int = step

    override fun iterator(): IntIterator = IntProgressionIterator(first, last, step)

    /**
     * Checks if the progression is empty.
     *
     * Progression with a positive step is empty if its first element is greater than the last element.
     * Progression with a negative step is empty if its first element is less than the last element.
     */
    public open fun isEmpty(): Boolean = if (step > 0) first > last else first < last

    override fun equals(other: Any?): Boolean =
        other is IntProgression && (isEmpty() && other.isEmpty() ||
        first == other.first && last == other.last && step == other.step)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (31 * first + last) + step)

    override fun toString(): String = if (step > 0) "$first..$last step $step" else "$first downTo $last step ${-step}"

    companion object {
        /**
         * Creates IntProgression within the specified bounds of a closed range.
         *
         * The progression starts with the [rangeStart] konstue and goes toward the [rangeEnd] konstue not excluding it, with the specified [step].
         * In order to go backwards the [step] must be negative.
         *
         * [step] must be greater than `Int.MIN_VALUE` and not equal to zero.
         */
        public fun fromClosedRange(rangeStart: Int, rangeEnd: Int, step: Int): IntProgression = IntProgression(rangeStart, rangeEnd, step)
    }
}

/**
 * A progression of konstues of type `Long`.
 */
public open class LongProgression
    internal constructor
    (
            start: Long,
            endInclusive: Long,
            step: Long
    ) : Iterable<Long> {
    init {
        if (step == 0L) throw kotlin.IllegalArgumentException("Step must be non-zero.")
        if (step == Long.MIN_VALUE) throw kotlin.IllegalArgumentException("Step must be greater than Long.MIN_VALUE to avoid overflow on negation.")
    }

    /**
     * The first element in the progression.
     */
    public konst first: Long = start

    /**
     * The last element in the progression.
     */
    public konst last: Long = getProgressionLastElement(start, endInclusive, step)

    /**
     * The step of the progression.
     */
    public konst step: Long = step

    override fun iterator(): LongIterator = LongProgressionIterator(first, last, step)

    /**
     * Checks if the progression is empty.
     *
     * Progression with a positive step is empty if its first element is greater than the last element.
     * Progression with a negative step is empty if its first element is less than the last element.
     */
    public open fun isEmpty(): Boolean = if (step > 0) first > last else first < last

    override fun equals(other: Any?): Boolean =
        other is LongProgression && (isEmpty() && other.isEmpty() ||
        first == other.first && last == other.last && step == other.step)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (31 * (first xor (first ushr 32)) + (last xor (last ushr 32))) + (step xor (step ushr 32))).toInt()

    override fun toString(): String = if (step > 0) "$first..$last step $step" else "$first downTo $last step ${-step}"

    companion object {
        /**
         * Creates LongProgression within the specified bounds of a closed range.
         *
         * The progression starts with the [rangeStart] konstue and goes toward the [rangeEnd] konstue not excluding it, with the specified [step].
         * In order to go backwards the [step] must be negative.
         *
         * [step] must be greater than `Long.MIN_VALUE` and not equal to zero.
         */
        public fun fromClosedRange(rangeStart: Long, rangeEnd: Long, step: Long): LongProgression = LongProgression(rangeStart, rangeEnd, step)
    }
}

