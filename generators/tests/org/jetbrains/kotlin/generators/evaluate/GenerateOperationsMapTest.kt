/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.test.ekonstuate

import junit.framework.TestCase
import org.jetbrains.kotlin.generators.ekonstuate.DEST_FILE
import org.jetbrains.kotlin.generators.ekonstuate.generate
import org.jetbrains.kotlin.test.KotlinTestUtils

class GenerateOperationsMapTest : TestCase() {
    fun testGeneratedDataIsUpToDate(): Unit {
        konst text = generate()
        KotlinTestUtils.assertEqualsToFile(DEST_FILE, text)
    }
}
