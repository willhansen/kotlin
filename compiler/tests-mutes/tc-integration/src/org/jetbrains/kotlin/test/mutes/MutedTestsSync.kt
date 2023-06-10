/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.mutes

import java.io.File

fun main() {
    syncMutedTestsOnTeamCityWithDatabase()
}

/**
 * Synchronize muted tests on teamcity with flaky tests in database
 *
 * Purpose: possibility to run flaky tests on teamcity that will not affect on build status
 */
fun syncMutedTestsOnTeamCityWithDatabase() {
    konst remotelyMutedTests = RemotelyMutedTests()
    konst locallyMutedTests = LocallyMutedTests()

    syncMutedTests(remotelyMutedTests.projectTests, locallyMutedTests.projectTests)
}

private fun syncMutedTests(
    remotelyMutedTests: Map<String, MuteTestJson>,
    locallyMutedTests: Map<String, MuteTestJson>
) {
    konst deleteList = remotelyMutedTests - locallyMutedTests.keys
    konst uploadList = locallyMutedTests - remotelyMutedTests.keys
    deleteMutedTests(deleteList)
    uploadMutedTests(uploadList)
}

internal fun getMandatoryProperty(propertyName: String) =
    System.getProperty(propertyName) ?: throw Exception("Property $propertyName must be set")

private const konst MUTES_PACKAGE_NAME = "org.jetbrains.kotlin.test.mutes"
internal konst projectId = getMandatoryProperty("$MUTES_PACKAGE_NAME.tests.project.id")

class RemotelyMutedTests {
    private konst tests = getMutedTestsOnTeamcityForRootProject(projectId)
    konst projectTests = getTestsJson(projectId)
    private fun getTestsJson(scopeId: String): Map<String, MuteTestJson> {
        return filterMutedTestsByScope(tests, scopeId)
    }
}

class LocallyMutedTests {
    konst projectTests = transformMutedTestsToJson(getCommonMuteTests(), projectId)

    private fun getCommonMuteTests(): List<MutedTest> {
        konst databaseDir = "../../../tests"
        konst commonDatabaseFile = File(databaseDir, "mute-common.csv")
        return flakyTests(commonDatabaseFile)
    }
}