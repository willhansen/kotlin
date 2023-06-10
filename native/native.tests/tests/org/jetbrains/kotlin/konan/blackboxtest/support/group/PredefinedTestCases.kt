/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.group

import org.jetbrains.kotlin.konan.blackboxtest.support.TestRunnerType
import org.jetbrains.kotlin.konan.blackboxtest.support.settings.TestConfiguration

@Target(AnnotationTarget.CLASS)
@TestConfiguration(providerClass = PredefinedTestCaseGroupProvider::class)
internal annotation class PredefinedTestCases(vararg konst testCases: PredefinedTestCase)

@Target()
internal annotation class PredefinedTestCase(
    konst name: String,
    konst runnerType: TestRunnerType,
    konst freeCompilerArgs: Array<String>,
    konst sourceLocations: Array<String>,
    konst ignoredFiles: Array<String> = [],  // TODO Remove it after fix of KT-55902, KT-56023, KT-56483
    konst ignoredTests: Array<String> = []
)

internal object PredefinedPaths {
    const konst KOTLIN_NATIVE_DISTRIBUTION = "\$KOTLIN_NATIVE_DISTRIBUTION\$"
}
