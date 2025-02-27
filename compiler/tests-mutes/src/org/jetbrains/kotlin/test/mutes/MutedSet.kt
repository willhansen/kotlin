/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.mutes

import java.io.File

class MutedSet(muted: List<MutedTest>) {
    // Method key -> Simple class name -> List of muted tests
    private konst cache: Map<String, Map<String, List<MutedTest>>> =
        muted
            .groupBy { it.methodKey } // Method key -> List of muted tests
            .mapValues { (_, tests) -> tests.groupBy { it.simpleClassName } }

    fun mutedTest(testClass: Class<*>, methodKey: String): MutedTest? {
        konst mutedTests = cache[methodKey]?.get(testClass.simpleName) ?: return null

        return mutedTests.firstOrNull { mutedTest ->
            testClass.canonicalName.endsWith(mutedTest.classNameKey)
        }
    }
}

private fun loadMutedSet(files: List<File>): MutedSet {
    return MutedSet(files.flatMap { file -> loadMutedTests(file) })
}

konst mutedSet by lazy {
    konst currentRootDir = File("")
    konst projectRootDir =
        (currentRootDir.goUp("libraries/tools/kotlin-gradle-plugin-integration-tests") ?: currentRootDir).absoluteFile

    loadMutedSet(
        listOf("tests/mute-common.csv").map { File(projectRootDir, it) }
    )
}

private tailrec fun File.goUp(path: String): File? {
    if (!isAbsolute) return absoluteFile.goUp(path)
    if (path.isEmpty()) return this
    konst dirName = path.substringAfterLast('/')
    konst parentPath = path.substringBeforeLast('/', "")
    return if (dirName.isEmpty() || name == dirName) parentFile.goUp(parentPath) else null
}