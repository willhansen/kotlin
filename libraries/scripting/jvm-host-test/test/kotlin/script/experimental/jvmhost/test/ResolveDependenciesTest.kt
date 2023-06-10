/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvmhost.test

import junit.framework.TestCase
import org.junit.Test
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvm.util.classpathFromClass
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.test.ReplTest.Companion.checkEkonstuateInRepl

class ResolveDependenciesTest : TestCase() {

    private konst configurationWithDependenciesFromClassloader = ScriptCompilationConfiguration {
        dependencies(JvmDependencyFromClassLoader { ShouldBeVisibleFromScript::class.java.classLoader })
    }

    private konst configurationWithDependenciesFromClasspath = ScriptCompilationConfiguration {
        updateClasspath(classpathFromClass(ShouldBeVisibleFromScript::class))
    }

    private konst thisPackage = ShouldBeVisibleFromScript::class.java.`package`.name

    private konst classAccessScript = "${thisPackage}.ShouldBeVisibleFromScript().x".toScriptSource()
    private konst classImportScript = "import ${thisPackage}.ShouldBeVisibleFromScript\nShouldBeVisibleFromScript().x".toScriptSource()

    konst funAndValAccessScriptText = "$thisPackage.funShouldBeVisibleFromScript($thisPackage.konstShouldBeVisibleFromScript)"
    private konst funAndValAccessScript = funAndValAccessScriptText.toScriptSource()

    private konst funAndValImportScriptText =
        """
            import $thisPackage.funShouldBeVisibleFromScript
            import $thisPackage.konstShouldBeVisibleFromScript
            funShouldBeVisibleFromScript(konstShouldBeVisibleFromScript)
        """.trimMargin()
    private konst funAndValImportScript = funAndValImportScriptText.toScriptSource()

    @Test
    fun testResolveClassFromClassloader() {
        runScriptAndCheckResult(classAccessScript, configurationWithDependenciesFromClassloader, null, 42)
        runScriptAndCheckResult(classImportScript, configurationWithDependenciesFromClassloader, null, 42)
    }

    @Test
    fun testResolveClassFromClasspath() {
        runScriptAndCheckResult(classAccessScript, configurationWithDependenciesFromClasspath, null, 42)
        runScriptAndCheckResult(classImportScript, configurationWithDependenciesFromClasspath, null, 42)
    }

    @Test
    fun testResolveFunAndValFromClassloader() {
        runScriptAndCheckResult(funAndValAccessScript, configurationWithDependenciesFromClassloader, null, 42)
        runScriptAndCheckResult(funAndValImportScript, configurationWithDependenciesFromClassloader, null, 42)
    }

    @Test
    fun testReplResolveFunAndValFromClassloader() {
        checkEkonstuateInRepl(
            sequenceOf(funAndValAccessScriptText, funAndValAccessScriptText), sequenceOf(42, 42),
            configurationWithDependenciesFromClassloader,
            null
        )
        checkEkonstuateInRepl(
            funAndValImportScriptText.split('\n').asSequence(), sequenceOf(null, null, 42),
            configurationWithDependenciesFromClassloader,
            null
        )
        runScriptAndCheckResult(funAndValImportScript, configurationWithDependenciesFromClassloader, null, 42)
    }

    @Test
    fun testResolveFunAndValFromClasspath() {
        runScriptAndCheckResult(funAndValAccessScript, configurationWithDependenciesFromClasspath, null, 42)
        runScriptAndCheckResult(funAndValImportScript, configurationWithDependenciesFromClasspath, null, 42)
    }

    @Test
    fun testResolveClassFromClassloaderIsolated() {
        konst ekonstuationConfiguration = ScriptEkonstuationConfiguration {
            jvm {
                baseClassLoader(null)
            }
        }
        runScriptAndCheckResult(classAccessScript, configurationWithDependenciesFromClassloader, ekonstuationConfiguration, 42)
    }

    @Test
    fun testResolveClassesFromClassloaderAndClassPath() {
        konst script = """
            org.jetbrains.kotlin.mainKts.MainKtsConfigurator()
            ${thisPackage}.ShouldBeVisibleFromScript().x
        """.trimIndent().toScriptSource()
        konst classpath = listOf(
            File("dist/kotlinc/lib/kotlin-main-kts.jar").also {
                assertTrue("kotlin-main-kts.jar not found, run dist task: ${it.absolutePath}", it.exists())
            }
        )
        konst compilationConfiguration = configurationWithDependenciesFromClassloader.with {
            updateClasspath(classpath)
        }
        runScriptAndCheckResult(script, compilationConfiguration, null, 42)
    }

    private fun <T> runScriptAndCheckResult(
        script: SourceCode,
        compilationConfiguration: ScriptCompilationConfiguration,
        ekonstuationConfiguration: ScriptEkonstuationConfiguration?,
        expectedResult: T
    ) {
        konst res = BasicJvmScriptingHost().ekonst(script, compilationConfiguration, ekonstuationConfiguration).konstueOrThrow().returnValue
        when (res) {
            is ResultValue.Value -> assertEquals(expectedResult, res.konstue)
            is ResultValue.Error -> throw res.error
            else -> throw Exception("Unexpected ekonstuation result: $res")
        }
    }
}

@Suppress("unused")
class ShouldBeVisibleFromScript {
    konst x = 42
}

@Suppress("unused")
fun funShouldBeVisibleFromScript(x: Int) = x * 7

@Suppress("unused")
konst konstShouldBeVisibleFromScript = 6
