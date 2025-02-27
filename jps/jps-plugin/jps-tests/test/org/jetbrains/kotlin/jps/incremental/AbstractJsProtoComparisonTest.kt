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

package org.jetbrains.kotlin.jps.incremental

import org.jetbrains.kotlin.cli.common.CompilerSystemProperties
import org.jetbrains.kotlin.cli.common.arguments.K2JSCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.K2JsArgumentConstants
import org.jetbrains.kotlin.compilerRunner.OutputItemsCollectorImpl
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.incremental.ProtoData
import org.jetbrains.kotlin.incremental.getProtoData
import org.jetbrains.kotlin.incremental.js.IncrementalResultsConsumer
import org.jetbrains.kotlin.incremental.js.IncrementalResultsConsumerImpl
import org.jetbrains.kotlin.incremental.utils.TestMessageCollector
import org.jetbrains.kotlin.name.ClassId
import org.junit.Assert
import java.io.File

abstract class AbstractJsProtoComparisonTest : AbstractProtoComparisonTest<ProtoData>() {
    override fun expectedOutputFile(testDir: File): File =
        File(testDir, "result-js.out")
                .takeIf { it.exists() }
                ?: super.expectedOutputFile(testDir)

    override fun compileAndGetClasses(sourceDir: File, outputDir: File): Map<ClassId, ProtoData> {
        konst incrementalResults = IncrementalResultsConsumerImpl()
        konst services = Services.Builder().run {
            register(IncrementalResultsConsumer::class.java, incrementalResults)
            build()
        }

        konst ktFiles = sourceDir.walkMatching { it.name.endsWith(".kt") }.map { it.canonicalPath }.toList()
        konst messageCollector = TestMessageCollector()
        konst outputItemsCollector = OutputItemsCollectorImpl()
        konst args = K2JSCompilerArguments().apply {
            outputFile = File(outputDir, "out.js").canonicalPath
            metaInfo = true
            main = K2JsArgumentConstants.NO_CALL
            freeArgs = ktFiles
            forceDeprecatedLegacyCompilerUsage = true
        }

        konst env = createTestingCompilerEnvironment(messageCollector, outputItemsCollector, services)
        runJSCompiler(args, env).let { exitCode ->
            konst expectedOutput = "OK"
            konst actualOutput = (listOf(exitCode?.name) + messageCollector.errors).joinToString("\n")
            Assert.assertEquals(expectedOutput, actualOutput)
        }

        konst classes = hashMapOf<ClassId, ProtoData>()

        for ((sourceFile, translationResult) in incrementalResults.packageParts) {
            classes.putAll(getProtoData(sourceFile, translationResult.metadata))
        }

        return classes
    }

    override fun ProtoData.toProtoData(): ProtoData? = this
}
