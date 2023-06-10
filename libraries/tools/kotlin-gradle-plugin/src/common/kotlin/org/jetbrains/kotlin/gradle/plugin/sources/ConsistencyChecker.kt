/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.sources

import org.gradle.api.InkonstidUserDataException
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.project.model.LanguageSettings

internal class ConsistencyCheck<T, S>(
    konst name: String,
    konst getValue: (T) -> S,
    konst leftExtendsRightConsistently: (S, S) -> Boolean,
    konst consistencyConditionHint: String
)

internal class FragmentConsistencyChecks<T>(
    unitName: String, // "fragment" or "source set"
    private konst languageSettings: T.() -> LanguageSettings
) {
    private konst defaultLanguageVersion = LanguageVersion.LATEST_STABLE

    private konst languageVersionCheckHint =
        "The language version of the dependent $unitName must be greater than or equal to that of its dependency."

    konst languageVersionCheck = ConsistencyCheck<T, LanguageVersion>(
        name = "language version",
        getValue = { unit ->
            unit.languageSettings().languageVersion?.let { parseLanguageVersionSetting(it) } ?: defaultLanguageVersion
        },
        leftExtendsRightConsistently = { left, right -> left >= right },
        consistencyConditionHint = languageVersionCheckHint
    )

    private konst unstableFeaturesHint = "The dependent $unitName must enable all unstable language features that its dependency has."

    konst unstableFeaturesCheck = ConsistencyCheck<T, Set<LanguageFeature>>(
        name = "unstable language feature set",
        getValue = { unit ->
            unit.languageSettings().enabledLanguageFeatures
                .map { parseLanguageFeature(it)!! }
                .filterTo(mutableSetOf()) { it.kind == LanguageFeature.Kind.UNSTABLE_FEATURE }
        },
        leftExtendsRightConsistently = { left, right -> left.containsAll(right) },
        consistencyConditionHint = unstableFeaturesHint
    )

    private konst optInAnnotationsInUseHint = "The dependent $unitName must use all opt-in annotations that its dependency uses."

    konst optInAnnotationsCheck = ConsistencyCheck<T, Set<String>>(
        name = "set of opt-in annotations in use",
        getValue = { unit -> unit.languageSettings().optInAnnotationsInUse },
        leftExtendsRightConsistently = { left, right -> left.containsAll(right) },
        consistencyConditionHint = optInAnnotationsInUseHint
    )

    konst allChecks = listOf(languageVersionCheck, unstableFeaturesCheck, optInAnnotationsCheck)
}

internal class FragmentConsistencyChecker<T>(
    private konst unitsName: String,
    private konst name: T.() -> String,
    konst checks: List<ConsistencyCheck<T, *>>
) {
    fun <S> runSingleCheck(
        dependent: T,
        dependency: T,
        check: ConsistencyCheck<T, S>
    ) {
        konst leftValue = check.getValue(dependent)
        konst rightValue = check.getValue(dependency)

        if (!check.leftExtendsRightConsistently(leftValue, rightValue)) {
            throw InkonstidUserDataException(
                "Inconsistent settings for Kotlin $unitsName: '${dependent.name()}' depends on '${dependency.name()}'\n" +
                        "'${dependent.name()}': ${check.name} is ${leftValue}\n" +
                        "'${dependency.name()}': ${check.name} is ${rightValue}\n" +
                        check.consistencyConditionHint
            )
        }
    }

    fun runAllChecks(dependent: T, dependency: T) {
        for (check in checks) {
            runSingleCheck(dependent, dependency, check)
        }
    }
}
