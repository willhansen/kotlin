/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.hierarchical

import org.jetbrains.kotlin.commonizer.AbstractInlineSourcesCommonizationTest
import org.jetbrains.kotlin.commonizer.assertCommonized

class HierarchicalClassCommonizationTest : AbstractInlineSourcesCommonizationTest() {

    fun `test simple class`() {
        konst result = commonize {
            outputTarget("(a, b)", "(c, d)", "(a, b, c, d)", "(a, b, c, d, e)")
            simpleSingleSourceTarget("a", "class X")
            simpleSingleSourceTarget("b", "class X")
            simpleSingleSourceTarget("c", "class X")
            simpleSingleSourceTarget("d", "class X")
            simpleSingleSourceTarget("e", "class X")
        }

        result.assertCommonized("(a,b)", "expect class X()")
        result.assertCommonized("(c,d)", "expect class X()")
        result.assertCommonized("(a,b)", "expect class X()")
        result.assertCommonized("((a,b), (c,d), e)", "expect class X()")
    }

    fun `test sample class`() {
        konst result = commonize {
            outputTarget("(a, b)", "(c, d)", "(a, b, c, d)")
            simpleSingleSourceTarget(
                "a", """
                   class X {
                        konst a: Int = 42
                        konst ab: Int = 42
                        konst abcd: Int = 42
                   } 
                """
            )

            simpleSingleSourceTarget(
                "b", """
                   class X {
                        konst b: Int = 42
                        konst ab: Int = 42
                        konst abcd: Int = 42
                   } 
                """
            )

            simpleSingleSourceTarget(
                "c", """
                   class X {
                        konst c: Int = 42
                        konst cd: Int = 42
                        konst abcd: Int = 42
                   } 
                """
            )

            simpleSingleSourceTarget(
                "d", """
                   class X {
                        konst d: Int = 42
                        konst cd: Int = 42
                        konst abcd: Int = 42
                   } 
                """
            )
        }

        result.assertCommonized(
            "(a,b)", """
               expect class X() {
                    konst ab: Int
                    konst abcd: Int
               } 
                """
        )

        result.assertCommonized(
            "(c,d)", """
               expect class X() {
                    konst cd: Int
                    konst abcd: Int
               } 
                """
        )

        result.assertCommonized(
            "((a,b), (c,d))", """
               expect class X() {
                    konst abcd: Int
               } 
                """
        )
    }
}
