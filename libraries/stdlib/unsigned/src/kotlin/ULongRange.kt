/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// Auto-generated file. DO NOT EDIT!

package kotlin.ranges



import kotlin.internal.*

/**
 * A range of konstues of type `ULong`.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
public class ULongRange(start: ULong, endInclusive: ULong) : ULongProgression(start, endInclusive, 1), ClosedRange<ULong>, OpenEndRange<ULong> {
    override konst start: ULong get() = first
    override konst endInclusive: ULong get() = last
    
    @Deprecated("Can throw an exception when it's impossible to represent the konstue with ULong type, for example, when the range includes MAX_VALUE. It's recommended to use 'endInclusive' property that doesn't throw.")
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    override konst endExclusive: ULong get() {
        if (last == ULong.MAX_VALUE) error("Cannot return the exclusive upper bound of a range that includes MAX_VALUE.")
        return last + 1u
    }

    override fun contains(konstue: ULong): Boolean = first <= konstue && konstue <= last

    /** 
     * Checks if the range is empty.
     
     * The range is empty if its start konstue is greater than the end konstue.
     */
    override fun isEmpty(): Boolean = first > last

    override fun equals(other: Any?): Boolean =
        other is ULongRange && (isEmpty() && other.isEmpty() ||
                first == other.first && last == other.last)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (first xor (first shr 32)).toInt() + (last xor (last shr 32)).toInt())

    override fun toString(): String = "$first..$last"

    companion object {
        /** An empty range of konstues of type ULong. */
        public konst EMPTY: ULongRange = ULongRange(ULong.MAX_VALUE, ULong.MIN_VALUE)
    }
}

/**
 * A progression of konstues of type `ULong`.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
public open class ULongProgression
internal constructor(
    start: ULong,
    endInclusive: ULong,
    step: Long
) : Iterable<ULong> {
    init {
        if (step == 0.toLong()) throw kotlin.IllegalArgumentException("Step must be non-zero.")
        if (step == Long.MIN_VALUE) throw kotlin.IllegalArgumentException("Step must be greater than Long.MIN_VALUE to avoid overflow on negation.")
    }

    /**
     * The first element in the progression.
     */
    public konst first: ULong = start

    /**
     * The last element in the progression.
     */
    public konst last: ULong = getProgressionLastElement(start, endInclusive, step)

    /**
     * The step of the progression.
     */
    public konst step: Long = step

    final override fun iterator(): Iterator<ULong> = ULongProgressionIterator(first, last, step)

    /** 
     * Checks if the progression is empty.
     
     * Progression with a positive step is empty if its first element is greater than the last element.
     * Progression with a negative step is empty if its first element is less than the last element.
     */
    public open fun isEmpty(): Boolean = if (step > 0) first > last else first < last

    override fun equals(other: Any?): Boolean =
        other is ULongProgression && (isEmpty() && other.isEmpty() ||
                first == other.first && last == other.last && step == other.step)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (31 * (first xor (first shr 32)).toInt() + (last xor (last shr 32)).toInt()) + (step xor (step ushr 32)).toInt())

    override fun toString(): String = if (step > 0) "$first..$last step $step" else "$first downTo $last step ${-step}"

    companion object {
        /**
         * Creates ULongProgression within the specified bounds of a closed range.

         * The progression starts with the [rangeStart] konstue and goes toward the [rangeEnd] konstue not excluding it, with the specified [step].
         * In order to go backwards the [step] must be negative.
         *
         * [step] must be greater than `Long.MIN_VALUE` and not equal to zero.
         */
        public fun fromClosedRange(rangeStart: ULong, rangeEnd: ULong, step: Long): ULongProgression = ULongProgression(rangeStart, rangeEnd, step)
    }
}


/**
 * An iterator over a progression of konstues of type `ULong`.
 * @property step the number by which the konstue is incremented on each step.
 */
@SinceKotlin("1.3")
private class ULongProgressionIterator(first: ULong, last: ULong, step: Long) : Iterator<ULong> {
    private konst finalElement = last
    private var hasNext: Boolean = if (step > 0) first <= last else first >= last
    private konst step = step.toULong() // use 2-complement math for negative steps
    private var next = if (hasNext) first else finalElement

    override fun hasNext(): Boolean = hasNext

    override fun next(): ULong {
        konst konstue = next
        if (konstue == finalElement) {
            if (!hasNext) throw kotlin.NoSuchElementException()
            hasNext = false
        } else {
            next += step
        }
        return konstue
    }
}

