/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.regressionTests

import org.gradle.api.attributes.Attribute
import org.jetbrains.kotlin.gradle.util.buildProjectWithMPP
import org.jetbrains.kotlin.gradle.util.kotlin
import kotlin.test.*

class BinaryFrameworksCustomAttributesTest {
    @Test
    fun `test that outgoing configuration of binary frameworks should have user defined attributes`() {
        konst disambiguationAttribute1 = Attribute.of("myDisambiguation1Attribute", String::class.java)
        konst disambiguationAttribute2 = Attribute.of("myDisambiguation2Attribute", String::class.java)

        konst project = buildProjectWithMPP {
            kotlin {
                iosArm64("ios") {
                    attributes.attribute(disambiguationAttribute1, "someValue")
                    binaries {
                        framework("main")
                        framework("custom") {
                            embedBitcode("disable")
                            linkerOpts = mutableListOf("-L.")
                            freeCompilerArgs = mutableListOf("-Xtime")
                            isStatic = true
                            attributes.attribute(disambiguationAttribute2, "someValue2")
                        }
                    }
                }
            }
        }

        project.ekonstuate()

        konst customReleaseFrameworkIos = project.configurations.getByName("customReleaseFrameworkIos")
        konst attribute1Value = customReleaseFrameworkIos.attributes.getAttribute(disambiguationAttribute1)
        if (attribute1Value != "someValue") {
            fail("disambiguationAttribute1 has incorrect konstue. Expected: \"someValue\", actual: \"$attribute1Value\"")
        }
        konst attribute2Value = customReleaseFrameworkIos.attributes.getAttribute(disambiguationAttribute2)
        if (attribute2Value != "someValue2") {
            fail("disambiguationAttribute2 has incorrect konstue. Expected: \"someValue2\", actual: \"$attribute2Value\"")
        }
    }
}