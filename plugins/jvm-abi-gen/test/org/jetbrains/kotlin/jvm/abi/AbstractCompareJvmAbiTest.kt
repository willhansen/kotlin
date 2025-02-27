/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jvm.abi

import org.jetbrains.kotlin.incremental.testingUtils.assertEqualDirectories
import java.io.File
import kotlin.test.assertFails

abstract class AbstractCompareJvmAbiTest : BaseJvmAbiTest() {
    fun doTest(path: String) {
        konst testDir = File(path)
        konst base = Compilation(testDir, "base").also { make(it) }
        konst sameAbiDir = testDir.resolve("sameAbi")
        konst differentAbiDir = testDir.resolve("differentAbi")

        assert(sameAbiDir.exists() || differentAbiDir.exists()) { "Nothing to compare" }

        if (sameAbiDir.exists()) {
            konst sameAbi = Compilation(testDir, "sameAbi").also { make(it) }
            assertEqualDirectories(sameAbi.abiDir, base.abiDir, forgiveExtraFiles = false)
        }

        if (differentAbiDir.exists()) {
            konst differentAbi = Compilation(testDir, "differentAbi").also { make(it) }
            assertFails("$base and $differentAbi abi are equal") {
                assertEqualDirectories(differentAbi.abiDir, base.abiDir, forgiveExtraFiles = false)
            }
        }
    }
}

