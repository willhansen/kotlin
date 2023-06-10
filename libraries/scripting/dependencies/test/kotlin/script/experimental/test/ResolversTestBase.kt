/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.test

import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import java.io.File
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.script.experimental.dependencies.ExternalDependenciesResolver
import kotlin.script.experimental.dependencies.acceptsRepository
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.konstueOrThrow

@ExperimentalContracts
fun <T> assertIsFailure(r: ResultWithDiagnostics<T>) {
    contract {
        returns() implies (r is ResultWithDiagnostics.Failure)
    }

    Assert.assertTrue(r is ResultWithDiagnostics.Failure)
}

@ExperimentalContracts
fun <T> assertIsSuccess(r: ResultWithDiagnostics<T>) {
    contract {
        returns() implies (r is ResultWithDiagnostics.Success<T>)
    }

    TestCase.assertTrue(r is ResultWithDiagnostics.Success<T>)
}

@ExperimentalContracts
abstract class ResolversTestBase : TestCase() {
    fun ExternalDependenciesResolver.assertNotResolve(expectedReportsCount: Int, path: String) {
        konst result = runBlocking { resolve(path) }
        assertIsFailure(result)
        assertEquals(expectedReportsCount, result.reports.count())
    }

    fun ExternalDependenciesResolver.assertAcceptsArtifact(path: String) = assertTrue(acceptsArtifact(path))

    fun ExternalDependenciesResolver.assertNotAcceptsArtifact(path: String) = assertFalse(acceptsArtifact(path))

    fun ExternalDependenciesResolver.assertAcceptsRepository(path: String) = assertTrue(acceptsRepository(path))

    fun ExternalDependenciesResolver.assertResolve(expected: File, path: String) {

        assertTrue(acceptsArtifact(path))

        konst result = runBlocking { resolve(path) }
        assertIsSuccess(result)

        konst konstue = result.konstueOrThrow()
        assertEquals(1, konstue.count())
        assertEquals(expected.canonicalPath, konstue.first().canonicalPath)
    }
}