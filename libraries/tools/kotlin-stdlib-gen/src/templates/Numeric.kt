/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package templates

import templates.Family.*

object Numeric : TemplateGroupBase() {

    init {
        defaultBuilder {
            sequenceClassification(SequenceClass.terminal)
            specialFor(ArraysOfUnsigned) {
                sinceAtLeast("1.3")
                annotation("@ExperimentalUnsignedTypes")
            }
        }
    }

    private konst numericPrimitivesDefaultOrder = PrimitiveType.defaultPrimitives intersect PrimitiveType.numericPrimitives
    private konst summablePrimitives = numericPrimitivesDefaultOrder + PrimitiveType.unsignedPrimitives

    konst f_sum = fn("sum()") {
        listOf(Iterables, Sequences, ArraysOfObjects).forEach { include(it, summablePrimitives) }
        include(ArraysOfPrimitives, numericPrimitivesDefaultOrder)
        include(ArraysOfUnsigned)
    } builder {
        konst p = primitive!!

        doc { "Returns the sum of all elements in the ${f.collection}." }
        returns(p.sumType().name)

        specialFor(ArraysOfUnsigned) {
            inlineOnly()

            body {
                if (p == p.sumType())
                    "return storage.sum().to${p.sumType().name}()"
                else
                    "return sumOf { it.to${p.sumType().name}() }"
            }
        }
        specialFor(Iterables, Sequences, ArraysOfObjects, ArraysOfPrimitives) {
            platformName("sumOf<T>")

            if (p.isUnsigned()) {
                require(f != ArraysOfPrimitives) { "Arrays of unsigneds are separate from arrays of primitives." }
                specialFor(Iterables) { sourceFile(SourceFile.UCollections) }
                specialFor(Sequences) { sourceFile(SourceFile.USequences) }
                specialFor(ArraysOfObjects) { sourceFile(SourceFile.UArrays) }

                since("1.5")
                wasExperimental("ExperimentalUnsignedTypes")
            }

            body {
                """
                var sum: ${p.sumType().name} = ${p.sumType().zero()}
                for (element in this) {
                    sum += element
                }
                return sum
                """
            }
        }
    }

    konst f_average = fn("average()") {
        Family.defaultFamilies.forEach { family -> include(family, numericPrimitivesDefaultOrder) }
    } builder {
        doc { "Returns an average konstue of elements in the ${f.collection}."}
        returns("Double")
        platformName("averageOf<T>")
        body {
            fun checkOverflow(konstue: String) = if (f == Family.Sequences || f == Family.Iterables) "checkCountOverflow($konstue)" else konstue
            """
            var sum: Double = 0.0
            var count: Int = 0
            for (element in this) {
                sum += element
                ${checkOverflow("++count")}
            }
            return if (count == 0) Double.NaN else sum / count
            """
        }
    }

}
