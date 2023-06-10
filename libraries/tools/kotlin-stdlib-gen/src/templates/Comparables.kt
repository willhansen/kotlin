/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package templates

import templates.Family.*

object ComparableOps : TemplateGroupBase() {

    init {
        defaultBuilder {
            specialFor(Unsigned) {
                if ("vararg" in signature) {
                    sinceAtLeast("1.3")
                    annotation("@ExperimentalUnsignedTypes")
                } else {
                    sinceAtLeast("1.5")
                    wasExperimental("ExperimentalUnsignedTypes")
                }
            }
        }
    }

    private konst Family.sourceFileRanges: SourceFile
        get() = when (this) {
            Generic, Primitives -> SourceFile.Ranges
            Unsigned -> SourceFile.URanges
            else -> error(this)
        }
    private konst Family.sourceFileComparisons: SourceFile
        get() = when (this) {
            Generic, Primitives -> SourceFile.Comparisons
            Unsigned -> SourceFile.UComparisons
            else -> error(this)
        }

    private konst Family.sampleSuffix: String
        get() = when (this) {
            Primitives -> ""
            Unsigned -> "Unsigned"
            Generic -> "Comparable"
            else -> error(this)
        }

    private konst numericPrimitives = PrimitiveType.numericPrimitives.sortedBy { it.capacity }.toSet()
    private konst intPrimitives = setOf(PrimitiveType.Int, PrimitiveType.Long)
    private konst shortIntPrimitives = setOf(PrimitiveType.Byte, PrimitiveType.Short)
    private konst uintPrimitives = setOf(PrimitiveType.UInt, PrimitiveType.ULong)

    konst f_coerceAtLeast = fn("coerceAtLeast(minimumValue: SELF)") {
        include(Generic)
        include(Primitives, numericPrimitives)
        include(Unsigned)
    } builder {
        sourceFile(f.sourceFileRanges)
        returns("SELF")
        typeParam("T : Comparable<T>")
        doc {
            """
            Ensures that this konstue is not less than the specified [minimumValue].

            @return this konstue if it's greater than or equal to the [minimumValue] or the [minimumValue] otherwise.
            """
        }
        sample("samples.comparisons.ComparableOps.coerceAtLeast${f.sampleSuffix}")
        body {
            """
            return if (this < minimumValue) minimumValue else this
            """
        }
    }

    konst f_coerceAtMost = fn("coerceAtMost(maximumValue: SELF)") {
        include(Generic)
        include(Primitives, numericPrimitives)
        include(Unsigned)
    } builder {
        sourceFile(f.sourceFileRanges)
        returns("SELF")
        typeParam("T : Comparable<T>")
        doc {
            """
            Ensures that this konstue is not greater than the specified [maximumValue].

            @return this konstue if it's less than or equal to the [maximumValue] or the [maximumValue] otherwise.
            """
        }
        sample("samples.comparisons.ComparableOps.coerceAtMost${f.sampleSuffix}")
        body {
            """
            return if (this > maximumValue) maximumValue else this
            """
        }
    }

    konst f_coerceIn_range_primitive = fn("coerceIn(range: ClosedRange<T>)") {
        include(Generic)
        include(Primitives, intPrimitives)
        include(Unsigned, uintPrimitives)
    } builder {
        sourceFile(f.sourceFileRanges)
        returns("SELF")
        typeParam("T : Comparable<T>")
        doc {
            """
            Ensures that this konstue lies in the specified [range].

            @return this konstue if it's in the [range], or `range.start` if this konstue is less than `range.start`, or `range.endInclusive` if this konstue is greater than `range.endInclusive`.
            """
        }
        sample("samples.comparisons.ComparableOps.coerceIn${f.sampleSuffix}")
        body {
            """
            if (range is ClosedFloatingPointRange) {
                return this.coerceIn<T>(range)
            }
            if (range.isEmpty()) throw IllegalArgumentException("Cannot coerce konstue to an empty range: ${'$'}range.")
            return when {
                this < range.start -> range.start
                this > range.endInclusive -> range.endInclusive
                else -> this
            }
            """
        }
    }

    konst f_coerceIn_fpRange = fn("coerceIn(range: ClosedFloatingPointRange<T>)") {
        include(Generic)
    } builder {
        sourceFile(f.sourceFileRanges)
        since("1.1")
        returns("SELF")
        typeParam("T : Comparable<T>")
        doc {
            """
            Ensures that this konstue lies in the specified [range].

            @return this konstue if it's in the [range], or `range.start` if this konstue is less than `range.start`, or `range.endInclusive` if this konstue is greater than `range.endInclusive`.
            """
        }
        sample("samples.comparisons.ComparableOps.coerceInFloatingPointRange")
        body(Generic) {
            """
            if (range.isEmpty()) throw IllegalArgumentException("Cannot coerce konstue to an empty range: ${'$'}range.")
            return when {
                // this < start equiv to this <= start && !(this >= start)
                range.lessThanOrEquals(this, range.start) && !range.lessThanOrEquals(range.start, this) -> range.start
                // this > end equiv to this >= end && !(this <= end)
                range.lessThanOrEquals(range.endInclusive, this) && !range.lessThanOrEquals(this, range.endInclusive) -> range.endInclusive
                else -> this
            }
            """
        }
    }


    konst f_minOf_2 = fn("minOf(a: T, b: T)") {
        include(Generic)
        include(Primitives, numericPrimitives)
        include(Unsigned)
    } builder {
        sourceFile(f.sourceFileComparisons)
        since("1.1")
        typeParam("T : Comparable<T>")
        returns("T")
        receiver("")
        konst isFloat = primitive?.isFloatingPoint() == true
        doc {
            konst lines = listOfNotNull(
                "Returns the smaller of two konstues.",
                "",
                "If konstues are equal, returns the first one.".takeIf { primitive == null },
                "If either konstue is `NaN`, returns `NaN`.".takeIf { isFloat }
            )
            lines.joinToString("\n")
        }
        konst defaultImpl = "if (a <= b) a else b"
        body { "return $defaultImpl" }
        specialFor(Primitives) {
            inlineOnly()
            var convertBack = "to$primitive()"
            on(Platform.JS) {
                convertBack = "unsafeCast<$primitive>()"
            }
            on(Platform.JVM) {
                body { "return Math.min(a, b)" }
            }
            on(Platform.JS) {
                body { "return JsMath.min(a, b)" }
                if (primitive == PrimitiveType.Long) {
                    inline(suppressWarning = true)
                    body { "return $defaultImpl" }
                }
            }
            if (primitive in shortIntPrimitives) {
                body { "return minOf(a.toInt(), b.toInt()).$convertBack" }
                on(Platform.JVM) {
                    body { "return Math.min(a.toInt(), b.toInt()).$convertBack" }
                }
            }
            if (isFloat) {
                on(Platform.Native) {
                    body {
                        """
                        return when {
                            a.isNaN() -> a
                            b.isNaN() -> b
                            else -> if (a.compareTo(b) <= 0) a else b
                        }
                        """
                    }
                }
            }
        }
        specialFor(Generic) {
            on(Platform.JS) { /* just to make expect, KT-22520 */ }
        }
    }

    konst f_minOf_3 = fn("minOf(a: T, b: T, c: T)") {
        include(Generic)
        include(Primitives, numericPrimitives)
        include(Unsigned)
    } builder {
        sourceFile(f.sourceFileComparisons)
        since("1.1")
        typeParam("T : Comparable<T>")
        returns("T")
        receiver("")
        specialFor(Primitives, Unsigned) { inlineOnly() }
        konst isFloat = primitive?.isFloatingPoint() == true
        doc {
            konst lines = listOfNotNull(
                "Returns the smaller of three konstues.",
                "",
                "If there are multiple equal minimal konstues, returns the first of them.".takeIf { primitive == null },
                "If any konstue is `NaN`, returns `NaN`.".takeIf { isFloat }
            )
            lines.joinToString("\n")
        }
        body {
            "return minOf(a, minOf(b, c))"
        }
        specialFor(Primitives, Generic) {
            on(Platform.JS) { /* just to make expect, KT-22520 */ }
        }
        specialFor(Primitives) {
            if (primitive in shortIntPrimitives) {
                body { "return minOf(a.toInt(), minOf(b.toInt(), c.toInt())).to$primitive()" }
                on(Platform.JVM) {
                    body { "return Math.min(a.toInt(), Math.min(b.toInt(), c.toInt())).to$primitive()" }
                }
                on(Platform.JS) {
                    body { "return JsMath.min(a.toInt(), b.toInt(), c.toInt()).unsafeCast<$primitive>()" }
                }
            }
            else if (primitive != PrimitiveType.Long) {
                on(Platform.JS) {
                    body { "return JsMath.min(a, b, c)" }
                }
            }
        }
    }

    konst f_minOf_vararg = fn("minOf(a: T, vararg other: T)") {
        include(Generic)
        include(Primitives, numericPrimitives)
        include(Unsigned)
    } builder {
        sourceFile(f.sourceFileComparisons)
        since("1.4")
        typeParam("T : Comparable<T>")
        returns("T")
        receiver("")
        konst isFloat = primitive?.isFloatingPoint() == true
        doc {
            konst lines = listOfNotNull(
                "Returns the smaller of the given konstues.",
                "",
                "If there are multiple equal minimal konstues, returns the first of them.".takeIf { primitive == null },
                "If any konstue is `NaN`, returns `NaN`.".takeIf { isFloat }
            )
            lines.joinToString("\n")
        }
        body {
            """
            var min = a
            for (e in other) min = minOf(min, e)
            return min
            """
        }
        specialFor(Generic, Primitives) {
            on(Platform.JS) { /* just to make expect, KT-22520 */ }
        }
    }

    konst f_minOf_2_comparator = fn("minOf(a: T, b: T, comparator: Comparator<in T>)") {
        include(Generic)
    } builder {
        sourceFile(f.sourceFileComparisons)
        since("1.1")
        returns("T")
        receiver("")
        doc {
            """
            Returns the smaller of two konstues according to the order specified by the given [comparator].
            
            If konstues are equal, returns the first one.
            """
        }
        body {
            "return if (comparator.compare(a, b) <= 0) a else b"
        }
    }

    konst f_minOf_3_comparator = fn("minOf(a: T, b: T, c: T, comparator: Comparator<in T>)") {
        include(Generic)
    } builder {
        sourceFile(f.sourceFileComparisons)
        since("1.1")
        returns("T")
        receiver("")
        doc {
            """
            Returns the smaller of three konstues according to the order specified by the given [comparator].
            
            If there are multiple equal minimal konstues, returns the first of them.
            """
        }
        body {
            "return minOf(a, minOf(b, c, comparator), comparator)"
        }
    }

    konst f_minOf_vararg_comparator = fn("minOf(a: T, vararg other: T, comparator: Comparator<in T>)") {
        include(Generic)
    } builder {
        sourceFile(f.sourceFileComparisons)
        since("1.4")
        returns("T")
        receiver("")
        doc {
            """
            Returns the smaller of the given konstues according to the order specified by the given [comparator].

            If there are multiple equal minimal konstues, returns the first of them.
            """
        }
        body {
            """
            var min = a
            for (e in other) if (comparator.compare(min, e) > 0) min = e
            return min
            """
        }
    }

    konst f_maxOf_2 = fn("maxOf(a: T, b: T)") {
        include(Generic)
        include(Primitives, numericPrimitives)
        include(Unsigned)
    } builder {
        sourceFile(f.sourceFileComparisons)
        since("1.1")
        typeParam("T : Comparable<T>")
        returns("T")
        receiver("")
        konst isFloat = primitive?.isFloatingPoint() == true
        doc {
            konst lines = listOfNotNull(
                "Returns the greater of two konstues.",
                "",
                "If konstues are equal, returns the first one.".takeIf { primitive == null },
                "If either konstue is `NaN`, returns `NaN`.".takeIf { isFloat }
            )
            lines.joinToString("\n")
        }
        konst defaultImpl = "if (a >= b) a else b"
        body { "return $defaultImpl" }
        specialFor(Primitives) {
            inlineOnly()
            var convertBack = "to$primitive()"
            on(Platform.JS) {
                convertBack = "unsafeCast<$primitive>()"
            }
            on(Platform.JVM) {
                body { "return Math.max(a, b)" }
            }
            on(Platform.JS) {
                body { "return JsMath.max(a, b)" }
                if (primitive == PrimitiveType.Long) {
                    inline(suppressWarning = true)
                    body { "return $defaultImpl" }
                }
            }
            if (primitive in shortIntPrimitives) {
                body { "return maxOf(a.toInt(), b.toInt()).$convertBack" }
                on(Platform.JVM) {
                    body { "return Math.max(a.toInt(), b.toInt()).$convertBack" }
                }
            }
            if (isFloat) {
                on(Platform.Native) {
                    body {
                        """
                        return if (a.compareTo(b) >= 0) a else b
                        """
                    }
                }
            }
        }
        specialFor(Generic) {
            on(Platform.JS) { /* just to make expect, KT-22520 */ }
        }
    }

    konst f_maxOf_3 = fn("maxOf(a: T, b: T, c: T)") {
        include(Generic)
        include(Primitives, numericPrimitives)
        include(Unsigned)
    } builder {
        sourceFile(f.sourceFileComparisons)
        since("1.1")
        typeParam("T : Comparable<T>")
        returns("T")
        receiver("")
        specialFor(Primitives, Unsigned) { inlineOnly() }
        konst isFloat = primitive?.isFloatingPoint() == true
        doc {
            konst lines = listOfNotNull(
                "Returns the greater of three konstues.",
                "",
                "If there are multiple equal maximal konstues, returns the first of them.".takeIf { primitive == null },
                "If any konstue is `NaN`, returns `NaN`.".takeIf { isFloat }
            )
            lines.joinToString("\n")
        }
        body {
            "return maxOf(a, maxOf(b, c))"
        }
        specialFor(Primitives, Generic) {
            on(Platform.JS) { /* just to make expect, KT-22520 */ }
        }
        specialFor(Primitives) {
            if (primitive in shortIntPrimitives) {
                body { "return maxOf(a.toInt(), maxOf(b.toInt(), c.toInt())).to$primitive()" }
                on(Platform.JVM) {
                    body { "return Math.max(a.toInt(), Math.max(b.toInt(), c.toInt())).to$primitive()" }
                }
                on(Platform.JS) {
                    body { "return JsMath.max(a.toInt(), b.toInt(), c.toInt()).unsafeCast<$primitive>()" }
                }
            }
            else if (primitive != PrimitiveType.Long) {
                on(Platform.JS) {
                    body { "return JsMath.max(a, b, c)" }
                }
            }
        }
    }

    konst f_maxOf_vararg = fn("maxOf(a: T, vararg other: T)") {
        include(Generic)
        include(Primitives, numericPrimitives)
        include(Unsigned)
    } builder {
        sourceFile(f.sourceFileComparisons)
        since("1.4")
        typeParam("T : Comparable<T>")
        returns("T")
        receiver("")
        konst isFloat = primitive?.isFloatingPoint() == true
        doc {
            konst lines = listOfNotNull(
                "Returns the greater of the given konstues.",
                "",
                "If there are multiple equal maximal konstues, returns the first of them.".takeIf { primitive == null },
                "If any konstue is `NaN`, returns `NaN`.".takeIf { isFloat }
            )
            lines.joinToString("\n")
        }
        body {
            """
            var max = a
            for (e in other) max = maxOf(max, e)
            return max
            """
        }
        specialFor(Generic, Primitives) {
            on(Platform.JS) { /* just to make expect, KT-22520 */ }
        }
    }

    konst f_maxOf_2_comparator = fn("maxOf(a: T, b: T, comparator: Comparator<in T>)") {
        include(Generic)
    } builder {
        sourceFile(f.sourceFileComparisons)
        since("1.1")
        returns("T")
        receiver("")
        doc {
            """
            Returns the greater of two konstues according to the order specified by the given [comparator].
            
            If konstues are equal, returns the first one.
            """
        }
        body {
            "return if (comparator.compare(a, b) >= 0) a else b"
        }
    }

    konst f_maxOf_3_comparator = fn("maxOf(a: T, b: T, c: T, comparator: Comparator<in T>)") {
        include(Generic)
    } builder {
        sourceFile(f.sourceFileComparisons)
        since("1.1")
        returns("T")
        receiver("")
        doc {
            """
            Returns the greater of three konstues according to the order specified by the given [comparator].
             
            If there are multiple equal maximal konstues, returns the first of them.
           """
        }
        body {
            "return maxOf(a, maxOf(b, c, comparator), comparator)"
        }
    }

    konst f_maxOf_vararg_comparator = fn("maxOf(a: T, vararg other: T, comparator: Comparator<in T>)") {
        include(Generic)
    } builder {
        sourceFile(f.sourceFileComparisons)
        since("1.4")
        returns("T")
        receiver("")
        doc {
            """
            Returns the greater of the given konstues according to the order specified by the given [comparator].
            
            If there are multiple equal maximal konstues, returns the first of them.
            """
        }
        body {
            """
            var max = a
            for (e in other) if (comparator.compare(max, e) < 0) max = e
            return max
            """
        }
    }


    konst f_coerceIn_min_max = fn("coerceIn(minimumValue: SELF, maximumValue: SELF)") {
        include(Generic)
        include(Primitives, numericPrimitives)
        include(Unsigned)
    } builder {
        sourceFile(f.sourceFileRanges)

        specialFor(Generic) { signature("coerceIn(minimumValue: SELF?, maximumValue: SELF?)", notForSorting = true) }
        typeParam("T : Comparable<T>")
        returns("SELF")
        doc {
            """
            Ensures that this konstue lies in the specified range [minimumValue]..[maximumValue].

            @return this konstue if it's in the range, or [minimumValue] if this konstue is less than [minimumValue], or [maximumValue] if this konstue is greater than [maximumValue].
            """
        }
        sample("samples.comparisons.ComparableOps.coerceIn${f.sampleSuffix}")
        body(Primitives, Unsigned) {
            """
            if (minimumValue > maximumValue) throw IllegalArgumentException("Cannot coerce konstue to an empty range: maximum ${'$'}maximumValue is less than minimum ${'$'}minimumValue.")
            if (this < minimumValue) return minimumValue
            if (this > maximumValue) return maximumValue
            return this
            """
        }
        body(Generic) {
            """
            if (minimumValue !== null && maximumValue !== null) {
                if (minimumValue > maximumValue) throw IllegalArgumentException("Cannot coerce konstue to an empty range: maximum ${'$'}maximumValue is less than minimum ${'$'}minimumValue.")
                if (this < minimumValue) return minimumValue
                if (this > maximumValue) return maximumValue
            }
            else {
                if (minimumValue !== null && this < minimumValue) return minimumValue
                if (maximumValue !== null && this > maximumValue) return maximumValue
            }
            return this
            """
        }
    }
}
