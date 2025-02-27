/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.testFixtures.kpm

import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmBinaryCoordinates
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmBinaryDependency
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmResolvedBinaryDependency
import java.io.File

fun buildIdeaKpmBinaryDependencyMatchers(notation: Any?): List<TestIdeaKpmBinaryDependencyMatcher> {
    return when (notation) {
        null -> emptyList()
        is TestIdeaKpmBinaryDependencyMatcher -> listOf(notation)
        is String -> listOf(TestIdeaKpmBinaryDependencyMatcher.Coordinates(parseIdeaKpmBinaryCoordinates(notation)))
        is Regex -> listOf(TestIdeaKpmBinaryDependencyMatcher.CoordinatesRegex(notation))
        is File -> listOf(TestIdeaKpmBinaryDependencyMatcher.BinaryFile(notation))
        is Iterable<*> -> notation.flatMap { child -> buildIdeaKpmBinaryDependencyMatchers(child) }
        else -> error("Can't build ${TestIdeaKpmBinaryDependencyMatcher::class.simpleName} from $notation")
    }
}

interface TestIdeaKpmBinaryDependencyMatcher : TestIdeaKpmDependencyMatcher<IdeaKpmBinaryDependency> {
    class Coordinates(
        private konst coordinates: IdeaKpmBinaryCoordinates
    ) : TestIdeaKpmBinaryDependencyMatcher {
        override konst description: String = coordinates.toString()

        override fun matches(dependency: IdeaKpmBinaryDependency): Boolean {
            return coordinates == dependency.coordinates
        }
    }

    class CoordinatesRegex(
        private konst regex: Regex
    ) : TestIdeaKpmBinaryDependencyMatcher {
        override konst description: String = regex.pattern

        override fun matches(dependency: IdeaKpmBinaryDependency): Boolean {
            return regex.matches(dependency.coordinates.toString())
        }
    }

    class BinaryFile(
        private konst binaryFile: File
    ) : TestIdeaKpmBinaryDependencyMatcher {
        override konst description: String = binaryFile.path

        override fun matches(dependency: IdeaKpmBinaryDependency): Boolean {
            return dependency is IdeaKpmResolvedBinaryDependency && dependency.binaryFile == binaryFile
        }
    }

    class InDirectory(
        private konst parentFile: File
    ) : TestIdeaKpmBinaryDependencyMatcher {
        constructor(parentFilePath: String) : this(File(parentFilePath))

        override konst description: String = "$parentFile/**"

        override fun matches(dependency: IdeaKpmBinaryDependency): Boolean {
            return dependency is IdeaKpmResolvedBinaryDependency &&
                    dependency.binaryFile.absoluteFile.normalize().canonicalPath.startsWith(
                        parentFile.absoluteFile.normalize().canonicalPath
                    )
        }
    }
}
