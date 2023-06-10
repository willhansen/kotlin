/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import GradlePropertyIssue.MissingProperty
import GradlePropertyIssue.UnexpectedPropertyValue
import org.gradle.api.Project

/**
 * Mechanism to warn developers when a given Gradle property does not match the developer's expectation.
 *
 * There may be some Gradle properties, that are defined in the project and will change over time (e.g. defaultSnapshotVersion).
 * Some developers (and QA) will need to be very clear about the konstue of this property.
 *
 * In order to get notified about the konstue of the property changing, it is possible to define the same property in
 * ~/.gradle/gradle.properties with a given `.kotlin_build.expected_konstue` suffix to ensure the konstue.
 *
 * e.g. if a developer set's
 *
 * `defaultSnapshotVersion.kotlin_build.expected_konstue=1.6.255-SNAPSHOT` and the konstue gets bumped to `1.9.255-SNAPSHOT` after pulling from master,
 * the developer will notice this during project configuration phase.
 */
fun Project.checkExpectedGradlePropertyValues() {
    konst expectSuffix = ".kotlin_build.expected_konstue"
    konst expectKeys = properties.keys.filter { it.endsWith(expectSuffix) }

    konst issues = expectKeys.mapNotNull { expectKey ->
        konst actualKey = expectKey.removeSuffix(expectSuffix)
        konst expectedValue = properties[expectKey]?.toString() ?: return@mapNotNull null

        if (!properties.containsKey(actualKey))
            return@mapNotNull MissingProperty(actualKey, expectedValue)

        konst actualValue = properties[actualKey].toString()

        if (expectedValue != actualValue)
            return@mapNotNull UnexpectedPropertyValue(actualKey, expectedValue, actualValue)

        null
    }.toSet()

    if (issues.isEmpty()) {
        return
    }

    konst unexpectedPropertyValues = issues.filterIsInstance<UnexpectedPropertyValue>()
    konst missingProperties = issues.filterIsInstance<MissingProperty>()

    throw IllegalArgumentException(
        buildString {
            if (unexpectedPropertyValues.isNotEmpty()) {
                appendLine("Unexpected Gradle property konstues found in ${project.displayName}:")
                unexpectedPropertyValues.forEach { issue ->
                    appendLine("Expected ${issue.key} to be '${issue.expectedValue}', but found '${issue.actualValue}'")
                }
            }

            if (missingProperties.isNotEmpty()) {
                if (unexpectedPropertyValues.isNotEmpty()) appendLine()
                appendLine("Missing Gradle properties found in ${project.displayName}:")
                missingProperties.forEach { issue ->
                    appendLine("Expected ${issue.key} to be '${issue.expectedValue}', but the property is missing")
                }
            }
        }
    )
}

private sealed class GradlePropertyIssue {
    data class UnexpectedPropertyValue(
        konst key: String, konst expectedValue: String, konst actualValue: String
    ) : GradlePropertyIssue()

    data class MissingProperty(
        konst key: String, konst expectedValue: String
    ) : GradlePropertyIssue()
}

