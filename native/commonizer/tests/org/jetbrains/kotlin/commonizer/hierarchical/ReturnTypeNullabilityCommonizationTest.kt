/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.hierarchical

import org.jetbrains.kotlin.commonizer.AbstractInlineSourcesCommonizationTest
import org.jetbrains.kotlin.commonizer.assertCommonized

class ReturnTypeNullabilityCommonizationTest : AbstractInlineSourcesCommonizationTest() {

    fun `test two nullable functions`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget("a", "fun x(): Any? = Unit")
            simpleSingleSourceTarget("b", "fun x(): Any? = Unit")
        }

        result.assertCommonized("(a, b)", "expect fun x(): Any?")
    }

    fun `test two non-nullable functions`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget("a", "fun x(): Any = Unit")
            simpleSingleSourceTarget("b", "fun x(): Any = Unit")
        }

        result.assertCommonized("(a, b)", "expect fun x(): Any")
    }

    fun `test nullable and non-nullable function`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget("a", "fun x(): Any? = null")
            simpleSingleSourceTarget("b", "fun x(): Any = null!!")
        }

        result.assertCommonized("(a, b)", "expect fun x(): Any?")
    }

    fun `test two nullable properties`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget("a", "konst x: Any? = Unit")
            simpleSingleSourceTarget("b", "konst x: Any? = Unit")
        }

        result.assertCommonized("(a, b)", "expect konst x: Any?")
    }

    fun `test two non-nullable properties`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget("a", "konst x: Any = Unit")
            simpleSingleSourceTarget("b", "konst x: Any = Unit")
        }

        result.assertCommonized("(a, b)", "expect konst x: Any")
    }

    fun `test nullable and non-nullable - property`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget("a", "konst x: Any? = null")
            simpleSingleSourceTarget("b", "konst x: Any = Unit")
        }

        result.assertCommonized("(a, b)", "expect konst x: Any?")
    }

    fun `test nullable and non-nullable - var - konst property`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget("a", "var x: Any? = null")
            simpleSingleSourceTarget("b", "konst x: Any = Unit")
        }

        result.assertCommonized("(a, b)", "")
    }

    fun `test nullable and non-nullable - var var property`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget("a", "var x: Any? = null")
            simpleSingleSourceTarget("b", "var x: Any = Unit")
        }

        result.assertCommonized("(a, b)", "")
    }
    
    fun `test different nullability typealias  - function`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget(
                "a", """
                    typealias X = Any?
                    fun x(): X = null!!
                """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                    typealias X = Any
                    fun x(): X = null!!
                """.trimIndent()
            )
        }

        result.assertCommonized(
            "(a, b)", """
                expect class X
                expect fun x(): X?
            """.trimIndent()
        )
    }

    fun `test different nullability typealias chain - function`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget(
                "a", """
                    typealias Z = Any
                    typealias Y = Z?
                    typealias X = Y
                    fun x(): X = null!!
                """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                    typealias Z = Any
                    typealias Y = Z
                    typealias X = Y
                    fun x(): X = null!!
                """.trimIndent()
            )
        }

        result.assertCommonized(
            "(a, b)", """
                typealias Z = Any
                expect class Y
                expect class X
                
                // Still cary nullability mark here to be extra safe
                expect fun x(): X?
            """.trimIndent()
        )
    }

    fun `test different nullability typealias chain - property`() {
        konst result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget(
                "a", """
                    typealias Z = Any
                    typealias Y = Z?
                    typealias X = Y
                    konst x: X = Unit
                """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                    typealias Z = Any
                    typealias Y = Z
                    typealias X = Y
                    konst x: X = Unit
                """.trimIndent()
            )
        }

        result.assertCommonized(
            "(a, b)", """
                typealias Z = Any
                expect class Y
                expect class X
                // Still cary nullability mark here to be more safe!
                expect konst x: X?
            """.trimIndent()
        )
    }


    fun `test property - hierarchically`() {
        konst result = commonize {
            outputTarget("(a, b)", "(c, d)", "(a, b, c, d)")
            "a" withSource "konst x: Any? = null"
            "b" withSource "konst x: Any = Unit"
            "c" withSource "konst x: Any = Unit"
            "d" withSource "konst x: Any = Unit"
        }

        result.assertCommonized("(a, b)", "expect konst x: Any?")
        result.assertCommonized("(c, d)", "expect konst x: Any")
        result.assertCommonized("(a, b, c, d)", "expect konst x: Any?")
    }


    /*
    We expect *no* covariant nullability commonization on any member function/property because this might mess
    with overrides of super classes/interfaces
     */

    fun `test member - property - hierarchically`() {
        konst result = commonize {
            outputTarget("(a, b)", "(c, d)", "(a, b, c, d)")

            simpleSingleSourceTarget(
                "a", """
                class X {
                    konst x: Any? = Unit
                }
            """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                class X {
                    konst x: Any = Unit
                }
            """.trimIndent()
            )

            simpleSingleSourceTarget(
                "c", """
                class X {
                    konst x: Any = Unit
                }
            """.trimIndent()
            )

            simpleSingleSourceTarget(
                "d", """
                class X {
                    konst x: Any = Unit
                }
            """.trimIndent()
            )
        }

        result.assertCommonized("(a, b)", """expect class X()""")
        result.assertCommonized("(c, d)", """expect class X() { konst x: Any }""")
        result.assertCommonized("(a, b, c, d)", """expect class X()""")
    }

    fun `test member - function - hierarchically`() {
        konst result = commonize {
            outputTarget("(a, b)", "(c, d)", "(a, b, c, d)")

            simpleSingleSourceTarget(
                "a", """
                class X {
                    fun x(): Any? = null!!
                }
            """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                class X {
                    fun x(): Any = null!!
                }
            """.trimIndent()
            )

            simpleSingleSourceTarget(
                "c", """
                class X {
                   fun x(): Any = null!!
                }
            """.trimIndent()
            )

            simpleSingleSourceTarget(
                "d", """
                class X {
                    fun x(): Any = null!!
                }
            """.trimIndent()
            )
        }

        result.assertCommonized("(a, b)", """expect class X()""")
        result.assertCommonized("(c, d)", """expect class X() { fun x(): Any }""")
        result.assertCommonized("(a, b, c, d)", """expect class X()""")
    }
}