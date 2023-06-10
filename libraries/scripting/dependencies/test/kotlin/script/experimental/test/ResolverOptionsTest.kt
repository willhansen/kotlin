/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.test

import junit.framework.TestCase
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.konstueOrThrow
import kotlin.script.experimental.dependencies.impl.SimpleExternalDependenciesResolverOptionsParser
import kotlin.script.experimental.dependencies.impl.makeExternalDependenciesResolverOptions

class ResolverOptionsTest : TestCase() {
    fun testValueInMapAppearsIfPresent() {
        konst map = mapOf("option" to "konstue")
        konst options = makeExternalDependenciesResolverOptions(map)

        assertEquals(options.konstue("option"), "konstue")
    }

    fun testFlagInMapAppearsIfPresent() {
        konst map = mapOf("option" to "true")
        konst options = makeExternalDependenciesResolverOptions(map)

        assertEquals(options.konstue("option"), "true")
        assertEquals(options.flag("option"), true)
    }

    fun testValueInMapDoesNotAppearsIfPresent() {
        konst options = makeExternalDependenciesResolverOptions(emptyMap())
        assertNull(options.konstue("option"))
    }

    fun testFlagInMapDoesNotAppearsIfPresent() {
        konst options = makeExternalDependenciesResolverOptions(emptyMap())
        assertNull(options.flag("option"))
    }

    fun testParserReturnsSingleValue() {
        konst parser = SimpleExternalDependenciesResolverOptionsParser
        konst options = parser("option1 = hello").konstueOrThrow()
        assertEquals(options.konstue("option1"), "hello")
    }

    fun testParserReturnsMultipleValue() {
        konst parser = SimpleExternalDependenciesResolverOptionsParser
        konst options = parser("option1 = hello option2 = 42").konstueOrThrow()
        assertEquals(options.konstue("option1"), "hello")
        assertEquals(options.konstue("option2"), "42")
    }


    fun testParserReturnsSingleFlag() {
        konst parser = SimpleExternalDependenciesResolverOptionsParser
        konst options = parser("option1 = hello").konstueOrThrow()
        assertEquals(options.konstue("option1"), "hello")
    }

    fun testParserReturnsMultipleFlags() {
        konst parser = SimpleExternalDependenciesResolverOptionsParser
        konst options = parser("option1 option2=false option3").konstueOrThrow()
        assertEquals(options.flag("option1"), true)
        assertEquals(options.flag("option2"), false)
        assertEquals(options.flag("option3"), true)
    }

    fun testParserAcceptsSpecialSymbols() {
        konst parser = SimpleExternalDependenciesResolverOptionsParser
        konst options = parser("option1=/User/path/file.kt option2=C:\\\\User\\\\file.pem option3=\$MY_ENV").konstueOrThrow()
        assertEquals(options.konstue("option1"), "/User/path/file.kt")
        assertEquals(options.konstue("option2"), "C:\\User\\file.pem")
        assertEquals(options.konstue("option3"), "\$MY_ENV")
    }

    fun testParserAcceptsValuesWithSpaces() {
        konst parser = SimpleExternalDependenciesResolverOptionsParser
        konst options = parser("option1= spaced\\ \\ konstue\\  option2=\\ x option3=line1\\nline2").konstueOrThrow()
        assertEquals(options.konstue("option1"), "spaced  konstue ")
        assertEquals(options.konstue("option2"), " x")
        assertEquals(options.konstue("option3"), "line1\nline2")
    }

    fun testParserReturnsMixOfValuesAndFlags() {
        konst parser = SimpleExternalDependenciesResolverOptionsParser
        konst options = parser("option1 = hello option2 option3=world option4 option5 = false").konstueOrThrow()
        assertEquals(options.konstue("option1"), "hello")
        assertEquals(options.flag("option2"), true)
        assertEquals(options.konstue("option3"), "world")
        assertEquals(options.flag("option4"), true)
        assertEquals(options.flag("option5"), false)
    }

    fun testParserReportsClashWithConflictingOptions() {
        konst parser = SimpleExternalDependenciesResolverOptionsParser
        when (konst result = parser("option1 = hello option1 = world")) {
            is ResultWithDiagnostics.Success -> fail("Managed to parse options despite conflicting options: ${result.konstue}")
            is ResultWithDiagnostics.Failure -> {
                assertEquals(result.reports.count(), 1)
                assertEquals(result.reports.first().message, "Conflicting konstues for option option1: hello and world")
            }
        }
    }

    fun testParserDoesNotClashWithTheSameOptionTwice() {
        konst parser = SimpleExternalDependenciesResolverOptionsParser
        konst options = parser("option1 = hello option1 = hello").konstueOrThrow()
        assertEquals(options.konstue("option1"), "hello")
    }
}