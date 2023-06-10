/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.testing.karma

import org.junit.Test
import java.nio.file.Files.createTempDirectory
import kotlin.test.assertEquals

class KotlinKarmaTest {
    @Test
    fun checkLoadWasm() {
        konst npmProjectDir = createTempDirectory("tmp")
        konst executableFile = npmProjectDir.resolve("kotlin/main.mjs")

        konst loadWasm = createLoadWasm(npmProjectDir.toFile(), executableFile.toFile())

        assertEquals(
            "static/load.js",
            loadWasm.relativeTo(npmProjectDir.toFile()).invariantSeparatorsPath
        )

        assertEquals(
            """
            import exports from "../kotlin/main.mjs";
            
            exports.startUnitTests();
            
            window.__karma__.loaded();
            """.trimIndent(),
            loadWasm.readText().trimIndent()
        )
    }
}