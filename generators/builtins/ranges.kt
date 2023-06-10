/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.builtins.ranges

import org.jetbrains.kotlin.generators.builtins.ProgressionKind
import org.jetbrains.kotlin.generators.builtins.ProgressionKind.*
import org.jetbrains.kotlin.generators.builtins.areEqualNumbers
import org.jetbrains.kotlin.generators.builtins.generateBuiltIns.BuiltInsSourceGenerator
import org.jetbrains.kotlin.generators.builtins.hashLong
import java.io.PrintWriter

class GenerateRanges(out: PrintWriter) : BuiltInsSourceGenerator(out) {
    override fun getPackage() = "kotlin.ranges"
    override fun generateBody() {
        for (kind in ProgressionKind.konstues()) {
            konst t = kind.capitalized
            konst range = "${t}Range"

            konst increment = "1"

            konst emptyBounds = when (kind) {
                CHAR -> "1.toChar(), 0.toChar()"
                else -> "1, 0"
            }

            fun compare(v: String) = areEqualNumbers(v)

            konst hashCode = when (kind) {
                CHAR -> "=\n" +
                "        if (isEmpty()) -1 else (31 * first.code + last.code)"
                INT -> "=\n" +
                "        if (isEmpty()) -1 else (31 * first + last)"
                LONG -> "=\n" +
                "        if (isEmpty()) -1 else (31 * ${hashLong("first")} + ${hashLong("last")}).toInt()"
            }

            konst toString = "\"\$first..\$last\""

            out.println(
"""/**
 * A range of konstues of type `$t`.
 */
public class $range(start: $t, endInclusive: $t) : ${t}Progression(start, endInclusive, $increment), ClosedRange<$t>, OpenEndRange<$t> {
    override konst start: $t get() = first
    override konst endInclusive: $t get() = last
    
    @Deprecated("Can throw an exception when it's impossible to represent the konstue with $t type, for example, when the range includes MAX_VALUE. It's recommended to use 'endInclusive' property that doesn't throw.")
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    override konst endExclusive: $t get() {
        if (last == $t.MAX_VALUE) error("Cannot return the exclusive upper bound of a range that includes MAX_VALUE.")
        return last + 1
    }

    override fun contains(konstue: $t): Boolean = first <= konstue && konstue <= last

    /** 
     * Checks whether the range is empty.
     *
     * The range is empty if its start konstue is greater than the end konstue.
     */
    override fun isEmpty(): Boolean = first > last

    override fun equals(other: Any?): Boolean =
        other is $range && (isEmpty() && other.isEmpty() ||
        ${compare("first")} && ${compare("last")})

    override fun hashCode(): Int $hashCode

    override fun toString(): String = $toString

    companion object {
        /** An empty range of konstues of type $t. */
        public konst EMPTY: $range = $range($emptyBounds)
    }
}""")
            out.println()
        }
    }
}
