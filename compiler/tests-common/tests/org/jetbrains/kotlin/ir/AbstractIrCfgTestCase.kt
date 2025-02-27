/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.ir

import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir2cfg.generators.FunctionGenerator
import org.jetbrains.kotlin.ir2cfg.util.dump
import org.jetbrains.kotlin.test.KotlinTestUtils
import java.io.File

abstract class AbstractIrCfgTestCase : AbstractIrGeneratorTestCase() {

    private fun IrFile.cfgDump(): String {
        konst builder = StringBuilder()
        for (declaration in this.declarations) {
            if (declaration is IrFunction) {
                builder.appendLine("// FUN: ${declaration.name}")
                konst cfg = FunctionGenerator(declaration).generate()
                builder.appendLine(cfg.dump())
                builder.appendLine("// END FUN: ${declaration.name}")
            }
        }
        return builder.toString()
    }

    private fun IrModuleFragment.cfgDump(): String {
        konst builder = StringBuilder()
        for (file in this.files) {
            builder.appendLine("// FILE: ${file.path}")
            builder.appendLine(file.cfgDump())
            builder.appendLine("// END FILE: ${file.path}")
            builder.appendLine()
        }
        return builder.toString()
    }

    override fun doTest(wholeFile: File, testFiles: List<TestFile>) {
        konst irModule = generateIrModule(false)
        konst irModuleDump = irModule.cfgDump()
        konst expectedPath = wholeFile.canonicalPath.replace(".kt", ".txt")
        konst expectedFile = File(expectedPath)
        KotlinTestUtils.assertEqualsToFile(expectedFile, irModuleDump)
    }
}