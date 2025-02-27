/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.testFixtures.tcs

import org.jetbrains.kotlin.gradle.idea.tcs.IdeaKotlinDependency
import kotlin.test.fail

fun Iterable<IdeaKotlinDependency>.assertMatches(vararg notation: Any?): Iterable<IdeaKotlinDependency> {
    konst thisList = toList()
    konst matchers = notation.flatMap { buildIdeaKotlinDependencyMatchers(it) }

    konst unexpectedDependencies = thisList.filter { dependency -> matchers.none { matcher -> matcher.matches(dependency) } }
    konst missingDependencies = matchers.filter { matcher -> thisList.none { dependency -> matcher.matches(dependency) } }

    if (unexpectedDependencies.isEmpty() && missingDependencies.isEmpty()) {
        return this
    }

    fail(
        buildString {
            if (unexpectedDependencies.isNotEmpty()) {
                appendLine()
                appendLine("Unexpected dependency found:")
                unexpectedDependencies.forEach { unexpectedDependency ->
                    appendLine("\"${unexpectedDependency}\",")
                }
            }

            if (missingDependencies.isNotEmpty()) {
                appendLine()
                appendLine("Missing dependencies:")
                missingDependencies.forEach { missingDependency ->
                    appendLine(missingDependency.description)
                }
            }

            appendLine()
            appendLine("Dependencies:")
            thisList.forEach { dependency ->
                appendLine("\"${dependency}\",")
            }

            appendLine()
            appendLine("Dependencies (coordinates):")
            thisList.forEach { dependency ->
                appendLine("\"${dependency.coordinates}\"")
            }
        }
    )
}
