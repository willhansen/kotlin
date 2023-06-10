/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.hierarchical

import org.jetbrains.kotlin.commonizer.AbstractInlineSourcesCommonizationTest
import org.jetbrains.kotlin.commonizer.assertCommonized

class HierarchicalPropertyCommonizationTest : AbstractInlineSourcesCommonizationTest() {

    fun `test simple property`() {
        konst result = commonize {
            outputTarget("(a, b)", "(c, d)", "(a, b, c, d)")
            simpleSingleSourceTarget("a", "konst x: Int = 42")
            simpleSingleSourceTarget("b", "konst x: Int = 42")
            simpleSingleSourceTarget("c", "konst x: Int = 42")
            simpleSingleSourceTarget("d", "konst x: Int = 42")
        }

        result.assertCommonized("((a,b), (c,d))", "expect konst x: Int")
        result.assertCommonized("(a, b)", "expect konst x: Int")
        result.assertCommonized("(c, d)", "expect konst x: Int")
    }

    fun `test same typeAliased property`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget(
                "a", """
                     typealias TA = Int
                     konst x: TA = 42
            """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                    typealias TA = Int
                    konst x: TA = 42
                """.trimIndent()
            )
        }

        result.assertCommonized(
            "(a, b)", """
            typealias TA = Int
            expect konst x: TA
        """.trimIndent()
        )
    }

    fun `test differently typeAliased property - expanded type from dependencies`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget(
                "a", """
                     typealias TA_A = Int
                     konst x: TA_A = 42
            """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                    typealias TA_B = Int
                    konst x: TA_B = 42
                """.trimIndent()
            )
        }

        result.assertCommonized(
            "(a, b)", """
            expect konst x: Int
        """.trimIndent()
        )
    }

    fun `test differently typeAliased property - expanded type from sources`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget(
                "a", """
                    class AB
                    typealias TA_A = AB
                    konst x: TA_A = TA_A()
            """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                    class AB
                    typealias TA_B = AB
                    konst x: TA_B = TA_B()
                """.trimIndent()
            )
        }

        result.assertCommonized(
            "(a, b)", """
                expect class AB()
                expect konst x: AB
        """.trimIndent()
        )
    }

    fun `test typeAliased property and class typed property`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget(
                "a", """
                    class AB
                    typealias TA = AB
                    konst x: TA = TA()
            """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                    class AB
                    konst x: AB = AB()
                """.trimIndent()
            )
        }

        result.assertCommonized(
            "(a, b)", """
                expect class AB()
                expect konst x: AB
        """.trimIndent()
        )
    }

    fun `test class typed property and typeAliased property`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget(
                "a", """
                    class AB
                    konst x: AB = AB()
            """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                    class AB
                    typealias TA = AB
                    konst x: TA = TA()
                """.trimIndent()
            )
        }

        result.assertCommonized(
            "(a, b)", """
                expect class AB()
                expect konst x: AB
        """.trimIndent()
        )
    }


    fun `test single typeAliased property and double typeAliased property`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget(
                "a", """
                    class AB
                    typealias TA_AB = AB
                    konst x: TA_AB = TA_AB()
            """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                    class AB
                    typealias TA_AB = AB
                    typealias TA_B = TA_AB
                    konst x: TA_B = TA_B()
                """.trimIndent()
            )
        }

        result.assertCommonized(
            "(a, b)", """
                expect class AB()
                typealias TA_AB = AB
                expect konst x: TA_AB
        """.trimIndent()
        )
    }

    fun `test single typeAliased property and double typeAliased property - with reversed order`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget(
                "a", """
                    class AB
                    typealias TA_AB = AB
                    typealias TA_B = TA_AB
                    konst x: TA_B = TA_B()
            """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                    class AB
                    typealias TA_AB = AB
                    konst x: TA_AB = TA_AB()
                """.trimIndent()
            )
        }

        result.assertCommonized(
            "(a, b)", """
                expect class AB()
                typealias TA_AB = AB
                expect konst x: TA_AB
        """.trimIndent()
        )
    }

    fun `test property with and without setter`() {
        konst result = commonize {
            outputTarget("(a, b)")

            simpleSingleSourceTarget(
                "a", """
                    konst x: Int = 42
                """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                    var x: Int = 42
                """.trimIndent()
            )
        }

        result.assertCommonized(
            "(a, b)", """
                expect var x: Int
                    private set
            """.trimIndent()
        )
    }
}
