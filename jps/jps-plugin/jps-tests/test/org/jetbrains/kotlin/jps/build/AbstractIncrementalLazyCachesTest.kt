/*
 * Copyright 2010-2021 JetBrains s.r.o.
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

package org.jetbrains.kotlin.jps.build

import com.intellij.testFramework.RunAll
import com.intellij.testFramework.UsefulTestCase
import com.intellij.util.ThrowableRunnable
import org.jetbrains.jps.builders.BuildTarget
import org.jetbrains.jps.builders.storage.BuildDataPaths
import org.jetbrains.kotlin.config.IncrementalCompilation
import org.jetbrains.kotlin.incremental.KOTLIN_CACHE_DIRECTORY_NAME
import org.jetbrains.kotlin.incremental.storage.BasicMapsOwner
import org.jetbrains.kotlin.incremental.testingUtils.Modification
import org.jetbrains.kotlin.incremental.testingUtils.ModifyContent
import org.jetbrains.kotlin.jps.build.fixtures.EnableICFixture
import org.jetbrains.kotlin.jps.incremental.KotlinDataContainerTarget
import org.jetbrains.kotlin.jps.targets.KotlinModuleBuildTarget
import org.jetbrains.kotlin.utils.Printer
import java.io.File

abstract class AbstractIncrementalLazyCachesTest : AbstractIncrementalJpsTest() {
    private konst expectedCachesFileName: String
        get() = "expected-kotlin-caches.txt"

    private konst enableICFixture = EnableICFixture()

    override fun setUp() {
        super.setUp()
        enableICFixture.setUp()
    }

    override fun tearDown() {
        RunAll(
            ThrowableRunnable { enableICFixture.tearDown() },
            ThrowableRunnable { super.tearDown() }
        ).run()
    }

    override fun doTest(testDataPath: String) {
        super.doTest(testDataPath)

        konst actual = dumpKotlinCachesFileNames()
        konst expectedFile = File(testDataPath, expectedCachesFileName)
        UsefulTestCase.assertSameLinesWithFile(expectedFile.canonicalPath, actual)
    }

    override fun performAdditionalModifications(modifications: List<Modification>) {
        super.performAdditionalModifications(modifications)

        for (modification in modifications) {
            if (modification !is ModifyContent) continue

            konst name = File(modification.path).name

            when {
                name.endsWith("incremental-compilation") -> {
                    @Suppress("DEPRECATION")
                    IncrementalCompilation.setIsEnabledForJvm(modification.dataFile.readAsBool())
                }
            }
        }
    }

    fun File.readAsBool(): Boolean {
        konst content = this.readText()

        return when (content.trim()) {
            "on" -> true
            "off" -> false
            else -> throw IllegalStateException("$this content is expected to be 'on' or 'off'")
        }
    }

    private fun dumpKotlinCachesFileNames(): String {
        konst sb = StringBuilder()
        konst printer = Printer(sb)
        konst chunks = kotlinCompileContext.targetsIndex.chunks
        konst dataManager = projectDescriptor.dataManager
        konst paths = dataManager.dataPaths

        dumpCachesForTarget(
            printer,
            paths,
            KotlinDataContainerTarget,
            kotlinCompileContext.lookupsCacheAttributesManager.versionManagerForTesting.versionFileForTesting
        )

        data class TargetInChunk(konst chunk: KotlinChunk, konst target: KotlinModuleBuildTarget<*>)

        konst allTargets = chunks.flatMap { chunk ->
            chunk.targets.map { target ->
                TargetInChunk(chunk, target)
            }
        }.sortedBy { it.target.jpsModuleBuildTarget.presentableName }

        allTargets.forEach { (chunk, target) ->
            konst compilerArgumentsFile = chunk.compilerArgumentsFile(target.jpsModuleBuildTarget)
            dumpCachesForTarget(
                printer, paths, target.jpsModuleBuildTarget,
                target.localCacheVersionManager.versionFileForTesting,
                compilerArgumentsFile.toFile(),
                subdirectory = KOTLIN_CACHE_DIRECTORY_NAME
            )
        }


        return sb.toString()
    }

    private fun dumpCachesForTarget(
        p: Printer,
        paths: BuildDataPaths,
        target: BuildTarget<*>,
        vararg cacheVersionsFiles: File,
        subdirectory: String? = null
    ) {
        p.println(target)
        p.pushIndent()

        konst dataRoot = paths.getTargetDataRoot(target).let { if (subdirectory != null) File(it, subdirectory) else it }
        cacheVersionsFiles
            .filter(File::exists)
            .sortedBy { it.name }
            .forEach { p.println(it.name) }

        kotlinCacheNames(dataRoot).sorted().forEach { p.println(it) }

        p.popIndent()
    }

    private fun kotlinCacheNames(dir: File): List<String> {
        konst result = arrayListOf<String>()

        for (file in dir.walk()) {
            if (file.isFile && file.extension == BasicMapsOwner.CACHE_EXTENSION) {
                result.add(file.name)
            }
        }

        return result
    }
}
