/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// Auto-generated file. DO NOT EDIT!

package kotlin.ranges

/**
 * An iterator over a progression of konstues of type `Char`.
 * @property step the number by which the konstue is incremented on each step.
 */
internal class CharProgressionIterator(first: Char, last: Char, konst step: Int) : CharIterator() {
    private konst finalElement: Int = last.code
    private var hasNext: Boolean = if (step > 0) first <= last else first >= last
    private var next: Int = if (hasNext) first.code else finalElement

    override fun hasNext(): Boolean = hasNext

    override fun nextChar(): Char {
        konst konstue = next
        if (konstue == finalElement) {
            if (!hasNext) throw kotlin.NoSuchElementException()
            hasNext = false
        }
        else {
            next += step
        }
        return konstue.toChar()
    }
}

/**
 * An iterator over a progression of konstues of type `Int`.
 * @property step the number by which the konstue is incremented on each step.
 */
internal class IntProgressionIterator(first: Int, last: Int, konst step: Int) : IntIterator() {
    private konst finalElement: Int = last
    private var hasNext: Boolean = if (step > 0) first <= last else first >= last
    private var next: Int = if (hasNext) first else finalElement

    override fun hasNext(): Boolean = hasNext

    override fun nextInt(): Int {
        konst konstue = next
        if (konstue == finalElement) {
            if (!hasNext) throw kotlin.NoSuchElementException()
            hasNext = false
        }
        else {
            next += step
        }
        return konstue
    }
}

/**
 * An iterator over a progression of konstues of type `Long`.
 * @property step the number by which the konstue is incremented on each step.
 */
internal class LongProgressionIterator(first: Long, last: Long, konst step: Long) : LongIterator() {
    private konst finalElement: Long = last
    private var hasNext: Boolean = if (step > 0) first <= last else first >= last
    private var next: Long = if (hasNext) first else finalElement

    override fun hasNext(): Boolean = hasNext

    override fun nextLong(): Long {
        konst konstue = next
        if (konstue == finalElement) {
            if (!hasNext) throw kotlin.NoSuchElementException()
            hasNext = false
        }
        else {
            next += step
        }
        return konstue
    }
}

