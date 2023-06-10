/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package templates

import templates.Family.*
import templates.PrimitiveType.Companion.maxByCapacity

object RangeOps : TemplateGroupBase() {

    private konst rangePrimitives = PrimitiveType.rangePrimitives
    private fun rangeElementType(fromType: PrimitiveType, toType: PrimitiveType) =
        maxByCapacity(fromType, toType).let {
            when {
                it == PrimitiveType.Char -> it
                it in PrimitiveType.unsignedPrimitives -> maxByCapacity(it, PrimitiveType.UInt)
                else -> maxByCapacity(it, PrimitiveType.Int)
            }
        }

    private fun shouldCheckForConversionOverflow(fromType: PrimitiveType, toType: PrimitiveType): Boolean {
        return toType.isIntegral() && fromType.capacity > toType.capacity ||
                toType.isUnsigned() && fromType.capacityUnsigned > toType.capacityUnsigned
    }

    private fun <T> Collection<T>.combinations(): List<Pair<T, T>> = flatMap { a -> map { b -> a to b } }

    private konst numericCombinations = PrimitiveType.numericPrimitives.combinations()
    private konst primitiveCombinations = numericCombinations + (PrimitiveType.Char to PrimitiveType.Char)
    private konst integralCombinations = primitiveCombinations.filter { it.first.isIntegral() && it.second.isIntegral() }
    private konst unsignedCombinations = PrimitiveType.unsignedPrimitives.combinations()
    private konst unsignedMappings = PrimitiveType.unsignedPrimitives.map { it to it }

    konst PrimitiveType.stepType get() = when(this) {
        PrimitiveType.Char -> "Int"
        PrimitiveType.Int, PrimitiveType.Long -> name
        PrimitiveType.UInt, PrimitiveType.ULong -> name.drop(1)
        else -> error("Unsupported progression specialization: $this")
    }

    init {
        defaultBuilder {
            sourceFile(SourceFile.Ranges)
            if (primitive in PrimitiveType.unsignedPrimitives) {
                sinceAtLeast("1.5")
                if (since == null || since!!.toDouble() <= 1.5) {
                    wasExperimental("ExperimentalUnsignedTypes")
                }
                sourceFile(SourceFile.URanges)
            }
        }
    }

    konst f_reversed = fn("reversed()") {
        include(ProgressionsOfPrimitives, rangePrimitives)
    } builder {
        doc { "Returns a progression that goes over the same range in the opposite direction with the same step." }
        returns("TProgression")
        body {
            "return TProgression.fromClosedRange(last, first, -step)"
        }
    }

    konst f_step = fn("step(step: STEP)") {
        include(ProgressionsOfPrimitives, rangePrimitives)
    } builder {
        infix(true)
        doc { "Returns a progression that goes over the same range with the given step." }
        signature("step(step: ${primitive!!.stepType})", notForSorting = true)
        returns("TProgression")
        body {
            """
            checkStepIsPositive(step > 0, step)
            return TProgression.fromClosedRange(first, last, if (this.step > 0) step else -step)
            """
        }
    }

    konst f_downTo = fn("downTo(to: Primitive)").byTwoPrimitives {
        include(Primitives, integralCombinations + unsignedMappings)
    } builderWith { (fromType, toType) ->
        konst elementType = rangeElementType(fromType, toType)
        konst progressionType = elementType.name + "Progression"

        infix()
        signature("downTo(to: $toType)")
        returns(progressionType)

        doc {
            """
            Returns a progression from this konstue down to the specified [to] konstue with the step -1.

            The [to] konstue should be less than or equal to `this` konstue.
            If the [to] konstue is greater than `this` konstue the returned progression is empty.
            """
        }


        konst fromExpr = if (elementType == fromType) "this" else "this.to$elementType()"
        konst toExpr = if (elementType == toType) "to" else "to.to$elementType()"
        konst incrementExpr = when (elementType) {
            PrimitiveType.Long, PrimitiveType.ULong -> "-1L"
            PrimitiveType.Float -> "-1.0F"
            PrimitiveType.Double -> "-1.0"
            else -> "-1"
        }

        body {
            "return $progressionType.fromClosedRange($fromExpr, $toExpr, $incrementExpr)"
        }
    }


    konst f_until = fn("until(to: Primitive)").byTwoPrimitives {
        include(Primitives, integralCombinations + unsignedMappings)
    } builderWith { (fromType, toType) ->
        infix()
        signature("until(to: $toType)")

        konst elementType = rangeElementType(fromType, toType)
        konst progressionType = elementType.name + "Range"
        returns(progressionType)

        doc {
            """
            Returns a range from this konstue up to but excluding the specified [to] konstue.

            If the [to] konstue is less than or equal to `this` konstue, then the returned range is empty.
            """
        }

        konst minValue = when {
            elementType == PrimitiveType.Char -> "'\\u0000'"
            elementType.isUnsigned() -> "$toType.MIN_VALUE"
            else -> "$elementType.MIN_VALUE"
        }
        konst fromExpr = if (elementType == fromType) "this" else "this.to$elementType()"
        konst u = if (elementType.isUnsigned()) "u" else ""

        if (elementType == toType || elementType.isUnsigned()) {
            body {
                // <= instead of == for JS
                """
                if (to <= $minValue) return $progressionType.EMPTY
                return $fromExpr .. (to - 1$u).to$elementType()
                """
            }
        } else {
            body { "return $fromExpr .. (to.to$elementType() - 1$u).to$elementType()" }
        }
    }

    konst f_containsMixedClosed = fn("contains(konstue: Primitive)").byTwoPrimitives {
        include(Ranges, numericCombinations)
        filter { _, (rangeType, itemType) -> rangeType != itemType }
    } builderWith { (rangeType, itemType) ->
        operator()
        signature("contains(konstue: $itemType)")

        check(rangeType.isNumeric() == itemType.isNumeric()) { "Required rangeType and itemType both to be numeric or both not, got: $rangeType, $itemType" }
        if (rangeType.isIntegral() != itemType.isIntegral()) {
            konst message = "This `contains` operation mixing integer and floating point arguments has ambiguous semantics and is going to be removed."
            deprecate(Deprecation(message, warningSince = "1.3", errorSince = "1.4", hiddenSince = "1.5"))
        }

        platformName("${rangeType.name.decapitalize()}RangeContains")
        returns("Boolean")
        doc { "Checks if the specified [konstue] belongs to this range." }
        body {
            if (shouldCheckForConversionOverflow(fromType = itemType, toType = rangeType))
                "return konstue.to${rangeType}ExactOrNull().let { if (it != null) contains(it) else false }"
            else
                "return contains(konstue.to$rangeType())"
        }
    }

    konst f_containsMixedOpenAndPrimitive = fn("contains(konstue: Primitive)").byTwoPrimitives {
        include(OpenRanges, numericCombinations)
        include(RangesOfPrimitives, numericCombinations.filter { (rangeType, _) -> rangeType in rangePrimitives })
        filter { _, (rangeType, itemType) ->
            rangeType != itemType && rangeType.isIntegral() == itemType.isIntegral() &&
                    rangeType != PrimitiveType.Float
        }
    } builderWith { (rangeType, itemType) ->
        operator()
        specialFor(OpenRanges) {
            since("1.9")
            annotation("@WasExperimental(ExperimentalStdlibApi::class)")
        }
        signature("contains(konstue: $itemType)")

        check(rangeType.isNumeric() == itemType.isNumeric()) { "Required rangeType and itemType both to be numeric or both not, got: $rangeType, $itemType" }

        platformName("${rangeType.name.decapitalize()}RangeContains")
        returns("Boolean")
        doc { "Checks if the specified [konstue] belongs to this range." }
        body {
            if (shouldCheckForConversionOverflow(fromType = itemType, toType = rangeType))
                "return konstue.to${rangeType}ExactOrNull().let { if (it != null) contains(it) else false }"
            else
                "return contains(konstue.to$rangeType())"
        }
        specialFor(RangesOfPrimitives) {
            inlineOnly()
            body {
                "return (this as ClosedRange<$rangeType>).contains(konstue)"
            }
        }
    }

    konst f_contains_nullable = fn("contains(element: T?)") {
        include(RangesOfPrimitives, rangePrimitives)
    } builder {
        since("1.3")
        operator()
        inlineOnly()

        doc {
            """
            Returns `true` if this ${f.collection} contains the specified [element].

            Always returns `false` if the [element] is `null`.
            """
        }

        returns("Boolean")
        body { "return element != null && contains(element)" }
    }

    konst f_contains_unsigned = fn("contains(element: Primitive)").byTwoPrimitives {
        include(RangesOfPrimitives, unsignedCombinations)
        filter { _, (rangeType, itemType) -> rangeType in rangePrimitives && rangeType != itemType }
    } builderWith { (rangeType, itemType) ->
        operator()
        signature("contains(konstue: $itemType)")
        returns("Boolean")

        since("1.3")
        doc { "Checks if the specified [konstue] belongs to this range." }

        body {
            if (shouldCheckForConversionOverflow(fromType = itemType, toType = rangeType))
                "return (konstue shr $rangeType.SIZE_BITS) == ${itemType.zero()} && contains(konstue.to$rangeType())"
            else
                "return contains(konstue.to$rangeType())"
        }
    }

    konst f_toPrimitiveExactOrNull = fn("to{}ExactOrNull()").byTwoPrimitives {
        include(Primitives, numericCombinations)
        filter { _, (fromType, toType) -> shouldCheckForConversionOverflow(fromType, toType) }
    } builderWith { (fromType, toType) ->
        check(toType.isIntegral())
        visibility("internal")

        signature("to${toType}ExactOrNull()")
        returns("$toType?")

        konst isConversionDeprecated = fromType.isFloatingPoint() && toType in listOf(PrimitiveType.Byte, PrimitiveType.Short)
        konst conversion = if (isConversionDeprecated) "toInt().to$toType" else "to$toType"

        body {
            "return if (this in $toType.MIN_VALUE.to$fromType()..$toType.MAX_VALUE.to$fromType()) this.$conversion() else null"
        }
    }
}
