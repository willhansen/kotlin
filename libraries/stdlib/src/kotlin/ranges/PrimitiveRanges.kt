/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// Auto-generated file. DO NOT EDIT!

package kotlin.ranges

/**
 * A range of konstues of type `Char`.
 */
public class CharRange(start: Char, endInclusive: Char) : CharProgression(start, endInclusive, 1), ClosedRange<Char>, OpenEndRange<Char> {
    override konst start: Char get() = first
    override konst endInclusive: Char get() = last
    
    @Deprecated("Can throw an exception when it's impossible to represent the konstue with Char type, for example, when the range includes MAX_VALUE. It's recommended to use 'endInclusive' property that doesn't throw.")
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    override konst endExclusive: Char get() {
        if (last == Char.MAX_VALUE) error("Cannot return the exclusive upper bound of a range that includes MAX_VALUE.")
        return last + 1
    }

    override fun contains(konstue: Char): Boolean = first <= konstue && konstue <= last

    /** 
     * Checks whether the range is empty.
     *
     * The range is empty if its start konstue is greater than the end konstue.
     */
    override fun isEmpty(): Boolean = first > last

    override fun equals(other: Any?): Boolean =
        other is CharRange && (isEmpty() && other.isEmpty() ||
        first == other.first && last == other.last)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * first.code + last.code)

    override fun toString(): String = "$first..$last"

    companion object {
        /** An empty range of konstues of type Char. */
        public konst EMPTY: CharRange = CharRange(1.toChar(), 0.toChar())
    }
}

/**
 * A range of konstues of type `Int`.
 */
public class IntRange(start: Int, endInclusive: Int) : IntProgression(start, endInclusive, 1), ClosedRange<Int>, OpenEndRange<Int> {
    override konst start: Int get() = first
    override konst endInclusive: Int get() = last
    
    @Deprecated("Can throw an exception when it's impossible to represent the konstue with Int type, for example, when the range includes MAX_VALUE. It's recommended to use 'endInclusive' property that doesn't throw.")
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    override konst endExclusive: Int get() {
        if (last == Int.MAX_VALUE) error("Cannot return the exclusive upper bound of a range that includes MAX_VALUE.")
        return last + 1
    }

    override fun contains(konstue: Int): Boolean = first <= konstue && konstue <= last

    /** 
     * Checks whether the range is empty.
     *
     * The range is empty if its start konstue is greater than the end konstue.
     */
    override fun isEmpty(): Boolean = first > last

    override fun equals(other: Any?): Boolean =
        other is IntRange && (isEmpty() && other.isEmpty() ||
        first == other.first && last == other.last)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * first + last)

    override fun toString(): String = "$first..$last"

    companion object {
        /** An empty range of konstues of type Int. */
        public konst EMPTY: IntRange = IntRange(1, 0)
    }
}

/**
 * A range of konstues of type `Long`.
 */
public class LongRange(start: Long, endInclusive: Long) : LongProgression(start, endInclusive, 1), ClosedRange<Long>, OpenEndRange<Long> {
    override konst start: Long get() = first
    override konst endInclusive: Long get() = last
    
    @Deprecated("Can throw an exception when it's impossible to represent the konstue with Long type, for example, when the range includes MAX_VALUE. It's recommended to use 'endInclusive' property that doesn't throw.")
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    override konst endExclusive: Long get() {
        if (last == Long.MAX_VALUE) error("Cannot return the exclusive upper bound of a range that includes MAX_VALUE.")
        return last + 1
    }

    override fun contains(konstue: Long): Boolean = first <= konstue && konstue <= last

    /** 
     * Checks whether the range is empty.
     *
     * The range is empty if its start konstue is greater than the end konstue.
     */
    override fun isEmpty(): Boolean = first > last

    override fun equals(other: Any?): Boolean =
        other is LongRange && (isEmpty() && other.isEmpty() ||
        first == other.first && last == other.last)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (first xor (first ushr 32)) + (last xor (last ushr 32))).toInt()

    override fun toString(): String = "$first..$last"

    companion object {
        /** An empty range of konstues of type Long. */
        public konst EMPTY: LongRange = LongRange(1, 0)
    }
}

