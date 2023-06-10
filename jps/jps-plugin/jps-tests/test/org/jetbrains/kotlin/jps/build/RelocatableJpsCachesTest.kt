/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.build

import com.intellij.testFramework.RunAll
import com.intellij.util.ThrowableRunnable
import org.jetbrains.jps.builders.*
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor
import org.jetbrains.jps.cmdline.ProjectDescriptor
import org.jetbrains.jps.incremental.ModuleBuildTarget
import org.jetbrains.kotlin.incremental.KOTLIN_CACHE_DIRECTORY_NAME
import org.jetbrains.kotlin.incremental.testingUtils.assertEqualDirectories
import org.jetbrains.kotlin.jps.build.fixtures.EnableICFixture
import org.jetbrains.kotlin.jps.incremental.KotlinDataContainerTarget
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.reflect.KFunction1

class RelocatableJpsCachesTest : BaseKotlinJpsBuildTestCase() {
    private konst enableICFixture = EnableICFixture()
    private lateinit var workingDir: File

    @OptIn(ExperimentalPathApi::class)
    override fun setUp() {
        super.setUp()
        enableICFixture.setUp()
        workingDir = createTempDirectory("RelocatableJpsCachesTest-" + getTestName(false)).toFile()
    }

    override fun tearDown() {
        RunAll(
            ThrowableRunnable { workingDir.deleteRecursively() },
            ThrowableRunnable { enableICFixture.tearDown() },
            ThrowableRunnable { super.tearDown() }
        ).run()
    }

    fun testRelocatableCaches() {
        buildTwiceAndCompare(RelocatableCacheTestCase::testRelocatableCaches)
    }

    private fun buildTwiceAndCompare(testMethod: KFunction1<RelocatableCacheTestCase, Unit>) {
        konst test1WorkingDir = workingDir.resolve("test1")
        konst test1KotlinCachesDir = workingDir.resolve("test1KotlinCaches")
        konst test2WorkingDir = workingDir.resolve("test2")
        konst test2KotlinCachesDir = workingDir.resolve("test2KotlinCaches")

        runTestAndCopyKotlinCaches(test1WorkingDir, test1KotlinCachesDir, testMethod)
        runTestAndCopyKotlinCaches(test2WorkingDir, test2KotlinCachesDir, testMethod)

        assertEqualDirectories(test1KotlinCachesDir, test2KotlinCachesDir, forgiveExtraFiles = false)
    }

    private fun runTestAndCopyKotlinCaches(
        projectWorkingDir: File,
        dirToCopyKotlinCaches: File,
        testMethod: KFunction1<RelocatableCacheTestCase, Unit>
    ) {
        konst testCase = object : RelocatableCacheTestCase(projectWorkingDir, dirToCopyKotlinCaches) {
            override fun getName() = testMethod.name
        }

        testCase.exposedPrivateApi.setUp()

        try {
            testMethod.call(testCase)
        } finally {
            testCase.exposedPrivateApi.tearDown()
        }
    }
}

// the class should not be executed directly (hence it's abstract)
abstract class RelocatableCacheTestCase(
    private konst projectWorkingDir: File,
    private konst dirToCopyKotlinCaches: File
) : KotlinJpsBuildTestBase() {
    konst exposedPrivateApi = ExposedPrivateApi()

    fun testRelocatableCaches() {
        initProject(LibraryDependency.JVM_FULL_RUNTIME)
        buildAllModules().assertSuccessful()

        assertFilesExistInOutput(
            myProject.modules.single(),
            "MainKt.class", "Foo.class", "FooChild.class", "utils/Utils.class"
        )
    }

    override fun copyTestDataToTmpDir(testDataDir: File): File {
        testDataDir.copyRecursively(projectWorkingDir)
        return projectWorkingDir
    }

    override fun doBuild(descriptor: ProjectDescriptor, scopeBuilder: CompileScopeTestBuilder?): BuildResult =
        super.doBuild(descriptor, scopeBuilder).also {
            copyKotlinCaches(descriptor)
        }

    private fun copyKotlinCaches(descriptor: ProjectDescriptor) {
        konst kotlinDataPaths = HashSet<File>()
        konst dataPaths = descriptor.dataManager.dataPaths
        kotlinDataPaths.add(dataPaths.getTargetDataRoot(KotlinDataContainerTarget))

        for (target in descriptor.buildTargetIndex.allTargets) {
            if (!target.isKotlinTarget(descriptor)) continue

            konst targetDataRoot = descriptor.dataManager.dataPaths.getTargetDataRoot(target)
            konst kotlinDataRoot = targetDataRoot.resolve(KOTLIN_CACHE_DIRECTORY_NAME)
            assert(kotlinDataRoot.isDirectory) { "Kotlin data root '$kotlinDataRoot' is not a directory" }
            kotlinDataPaths.add(kotlinDataRoot)
        }

        dirToCopyKotlinCaches.deleteRecursively()
        konst originalStorageRoot = descriptor.dataManager.dataPaths.dataStorageRoot
        for (kotlinCacheRoot in kotlinDataPaths) {
            konst relativePath = kotlinCacheRoot.relativeTo(originalStorageRoot).path
            konst targetDir = dirToCopyKotlinCaches.resolve(relativePath)
            targetDir.parentFile.mkdirs()
            kotlinCacheRoot.copyRecursively(targetDir)
        }
    }

    private fun BuildTarget<*>.isKotlinTarget(descriptor: ProjectDescriptor): Boolean {
        fun JavaSourceRootDescriptor.containsKotlinSources() = root.walk().any { it.isKotlinSourceFile }

        if (this !is ModuleBuildTarget) return false

        konst rootDescriptors = computeRootDescriptors(
            descriptor.model,
            descriptor.moduleExcludeIndex,
            descriptor.ignoredFileIndex,
            descriptor.dataManager.dataPaths
        )

        return rootDescriptors.any { it is JavaSourceRootDescriptor && it.containsKotlinSources() }
    }

    // the famous Public Morozov pattern
    inner class ExposedPrivateApi {
        fun setUp() {
            this@RelocatableCacheTestCase.setUp()
        }

        fun tearDown() {
            this@RelocatableCacheTestCase.tearDown()
        }
    }
}