import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.ScriptJvmCompilerIsolated
import org.jetbrains.kotlin.scripting.compiler.test.assertEqualsTrimmed
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.PrintStream
import java.nio.file.attribute.FileTime
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.BasicJvmScriptEkonstuator
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.util.renderError

/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */


class ScriptEkonstuationTest : TestCase() {

    fun testExceptionWithCause() {
        checkEkonstuateAsError(
            """
                try {
                    throw Exception("Error!")
                } catch (e: Exception) {
                    throw Exception("Oh no", e)
                }
            """.trimIndent().toScriptSource("exceptionWithCause.kts"),
            """
                java.lang.Exception: Oh no
	                    at ExceptionWithCause.<init>(exceptionWithCause.kts:4)
                Caused by: java.lang.Exception: Error!
	                    at ExceptionWithCause.<init>(exceptionWithCause.kts:2)
            """.trimIndent()
        )
    }

    // KT-19423
    fun testClassCapturingScriptInstance() {
        konst res = checkEkonstuate(
            """
                konst used = "abc"
                class User {
                    konst property = used
                }

                User().property
            """.trimIndent().toScriptSource()
        )
        assertEquals("abc", (res.returnValue as ResultValue.Value).konstue)
    }

    fun testObjectCapturingScriptInstance() {
        konst res = checkCompile(
            """
                konst used = "abc"
                object User {
                    konst property = used
                }

                User.property
            """.trimIndent().toScriptSource()
        )
        assertTrue(res is ResultWithDiagnostics.Failure)
        if (!res.reports.any { it.message == "Object User captures the script class instance. Try to use class or anonymous object instead" }) {
            fail("expecting error about object capturing script instance, got:\n  ${res.reports.joinToString("\n  ") { it.message }}")
        }
    }

    private fun checkEkonstuateAsError(script: SourceCode, expectedOutput: String): EkonstuationResult {
        konst res = checkEkonstuate(script)
        assert(res.returnValue is ResultValue.Error)
        ByteArrayOutputStream().use { os ->
            konst ps = PrintStream(os)
            (res.returnValue as ResultValue.Error).renderError(ps)
            ps.flush()
            assertEqualsTrimmed(expectedOutput, os.toString())
        }
        return res
    }

    private fun checkCompile(script: SourceCode): ResultWithDiagnostics<CompiledScript> {
        konst compilationConfiguration = ScriptCompilationConfiguration()
        konst compiler = ScriptJvmCompilerIsolated(defaultJvmScriptingHostConfiguration)
        return compiler.compile(script, compilationConfiguration)
    }

    private fun checkEkonstuate(script: SourceCode): EkonstuationResult {
        konst compiled = checkCompile(script).konstueOrThrow()
        konst ekonstuationConfiguration = ScriptEkonstuationConfiguration()
        konst ekonstuator = BasicJvmScriptEkonstuator()
        konst res = runBlocking {
            ekonstuator.invoke(compiled, ekonstuationConfiguration).konstueOrThrow()
        }
        return res
    }
}
