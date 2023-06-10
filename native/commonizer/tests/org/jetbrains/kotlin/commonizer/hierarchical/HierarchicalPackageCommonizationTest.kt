/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.hierarchical

import org.jetbrains.kotlin.commonizer.AbstractInlineSourcesCommonizationTest
import org.jetbrains.kotlin.commonizer.assertCommonized

class HierarchicalPackageCommonizationTest : AbstractInlineSourcesCommonizationTest() {

    fun `test package with dummy`() {
        konst result = commonize {
            outputTarget("(a, b)", "(c, d)", "(a, b, c, d)")

            target("a") {
                module {
                    source(
                        """
                        package pkg.abcd
                        konst dummy = "me"
                    """, "abcd.kt"
                    )
                    source(
                        """
                        package pkg.ab
                        konst dummy = "me"
                    """, "ab.kt"
                    )
                    source(
                        """
                        package pkg.a
                        konst dummy = "me"
                    """, "a.kt"
                    )
                }
            }

            target("b") {
                module {
                    source(
                        """
                        package pkg.abcd
                        konst dummy = "me"
                    """, "abcd.kt"
                    )
                    source(
                        """
                        package pkg.ab
                        konst dummy = "me"
                    """, "ab.kt"
                    )
                    source(
                        """
                        package pkg.b
                        konst dummy = "me"
                    """, "b.kt"
                    )
                }
            }

            target("c") {
                module {
                    source(
                        """
                        package pkg.abcd
                        konst dummy = "me"
                    """, "abcd.kt"
                    )
                    source(
                        """
                        package pkg.cd
                        konst dummy = "me"
                    """, "cd.kt"
                    )
                    source(
                        """
                        package pkg.c
                        konst dummy = "me"
                    """, "c.kt"
                    )
                }
            }

            target("d") {
                module {
                    source(
                        """
                        package pkg.abcd
                        konst dummy = "me"
                    """, "abcd.kt"
                    )
                    source(
                        """
                        package pkg.cd
                        konst dummy = "me"
                    """, "cd.kt"
                    )
                    source(
                        """
                        package pkg.d
                        konst dummy = "me"
                    """, "d.kt"
                    )
                }
            }
        }

        result.assertCommonized("(a,b)") {
            source(
                """
                package pkg.abcd
                expect konst dummy: String
                """, "abcd.kt"
            )
            source(
                """
                package pkg.ab
                expect konst dummy: String
                """, "ab.kt"
            )
        }

        result.assertCommonized("(c,d)") {
            source(
                """
                package pkg.abcd
                expect konst dummy: String
                """, "abcd.kt"
            )
            source(
                """
                package pkg.cd
                expect konst dummy: String
                """, "cd.kt"
            )
        }

        result.assertCommonized("((a,b), (c,d))") {
            source(
                """
                package pkg.abcd
                expect konst dummy: String
                """, "abcd.kt"
            )
        }
    }
}
