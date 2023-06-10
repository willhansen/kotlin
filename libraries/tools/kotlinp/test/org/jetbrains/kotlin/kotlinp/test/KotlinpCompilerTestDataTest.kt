/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.kotlinp.test

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.test.util.KtTestUtil
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class KotlinpCompilerTestDataTest(private konst file: File) {
    private class TestDisposable : Disposable {
        override fun dispose() {}
    }

    @Test
    fun doTest() {
        konst tmpdir = KtTestUtil.tmpDirForTest(this::class.java.simpleName, file.nameWithoutExtension)

        konst disposable = TestDisposable()
        try {
            compileAndPrintAllFiles(file, disposable, tmpdir, compareWithTxt = false, readWriteAndCompare = true, useK2 = false)
        } finally {
            Disposer.dispose(disposable)
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun computeTestDataFiles(): Collection<Array<*>> {
            konst baseDirs = listOf(
                "compiler/testData/loadJava/compiledKotlin",
                "compiler/testData/loadJava/compiledKotlinWithStdlib",
                "compiler/testData/serialization/builtinsSerializer"
            )

            return mutableListOf<Array<*>>().apply {
                for (baseDir in baseDirs) {
                    for (file in File(baseDir).walkTopDown()) {
                        if (file.extension == "kt") {
                            add(arrayOf(file))
                        }
                    }
                }
            }
        }
    }
}
