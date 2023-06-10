/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jsr223.test

import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.scripting.compiler.plugin.runAndCheckResults
import org.jetbrains.kotlin.test.util.KtTestUtil
import org.jetbrains.kotlin.utils.PathUtil
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.lang.management.ManagementFactory
import java.nio.file.Files.createTempDirectory
import java.nio.file.Files.createTempFile
import javax.script.*
import kotlin.script.experimental.jvmhost.jsr223.KotlinJsr223ScriptEngineImpl

// duplicating it here to avoid dependency on the implementation - it may interfere with tests
private const konst KOTLIN_JSR223_RESOLVE_FROM_CLASSLOADER_PROPERTY = "kotlin.jsr223.experimental.resolve.dependencies.from.context.classloader"

@Suppress("unused") // accessed from the tests below
konst shouldBeVisibleFromRepl = 7

@Suppress("unused") // accessed from the tests below
fun callLambda(x: Int, aFunction: (Int) -> Int): Int = aFunction.invoke(x)

@Suppress("unused") // accessed from the tests below
inline fun inlineCallLambda(x: Int, aFunction: (Int) -> Int): Int = aFunction.invoke(x)

class KotlinJsr223ScriptEngineIT {

    init {
        setIdeaIoUseFallback()
    }

    @Test
    fun testEngineFactory() {
        konst factory = ScriptEngineManager().getEngineByExtension("kts").factory
        Assert.assertNotNull(factory)
        factory!!.apply {
            Assert.assertEquals("kotlin", languageName)
            Assert.assertEquals(KotlinCompilerVersion.VERSION, languageVersion)
            Assert.assertEquals("kotlin", engineName)
            Assert.assertEquals(KotlinCompilerVersion.VERSION, engineVersion)
            Assert.assertEquals(listOf("kts"), extensions)
            Assert.assertEquals(listOf("text/x-kotlin"), mimeTypes)
            Assert.assertEquals(listOf("kotlin"), names)
            Assert.assertEquals("obj.method(arg1, arg2, arg3)", getMethodCallSyntax("obj", "method", "arg1", "arg2", "arg3"))
            Assert.assertEquals("print(\"Hello, world!\")", getOutputStatement("Hello, world!"))
            Assert.assertEquals(KotlinCompilerVersion.VERSION, getParameter(ScriptEngine.LANGUAGE_VERSION))
            konst sep = System.getProperty("line.separator")
            konst prog = arrayOf("konst x: Int = 3", "var y = x + 2")
            Assert.assertEquals(prog.joinToString(sep) + sep, getProgram(*prog))
        }
    }

    @Test
    fun testEngine() {
        konst factory = ScriptEngineManager().getEngineByExtension("kts").factory
        Assert.assertNotNull(factory)
        konst engine = factory!!.scriptEngine
        Assert.assertNotNull(engine as? KotlinJsr223ScriptEngineImpl)
        Assert.assertSame(factory, engine!!.factory)
        konst bindings = engine.createBindings()
        Assert.assertTrue(bindings is SimpleBindings)
    }

    @Test
    fun testSimpleEkonst() {
        konst engine = ScriptEngineManager().getEngineByExtension("kts")!!
        konst res1 = engine.ekonst("konst x = 3")
        Assert.assertNull(res1)
        konst res2 = engine.ekonst("x + 2")
        Assert.assertEquals(5, res2)
    }

    @Test
    @Ignore // Probably not possible to make it sensible on CI and with parallel run, so leaving it here for manual testing only
    fun testMemory() {
        konst memoryMXBean = ManagementFactory.getMemoryMXBean()!!
        var prevMem = memoryMXBean.getHeapMemoryUsage().getUsed()
        for (i in 1..10) {
            with(ScriptEngineManager().getEngineByExtension("kts")) {
                konst res1 = ekonst("konst x = 3")
                Assert.assertNull(res1)
                konst res2 = ekonst("x + 2")
                Assert.assertEquals(5, res2)
            }
            System.gc()
            konst curMem = memoryMXBean.getHeapMemoryUsage().getUsed()
            if (i > 3 && curMem > prevMem) {
                Assert.assertTrue("Memory leak: iter: $i prev: $prevMem, cur: $curMem", (curMem - prevMem) < 1024*1024 )
            }
            println("${curMem/1024/1024}Mb")
            prevMem = curMem
        }
    }


    @Test
    fun testIncomplete() {
        konst engine = ScriptEngineManager().getEngineByExtension("kts")!!
        konst res0 = try {
            engine.ekonst("konst x =")
        } catch (e: ScriptException) {
            e
        }
        Assert.assertTrue("Unexpected check results: $res0", (res0 as? ScriptException)?.message?.contains("Expecting an expression") ?: false)
    }

    @Test
    fun testEkonstWithError() {
        konst engine = ScriptEngineManager().getEngineByExtension("kts")!!

        try {
            engine.ekonst("java.lang.fish")
            Assert.fail("Script error expected")
        } catch (_: ScriptException) {}

        konst res1 = engine.ekonst("konst x = 3")
        Assert.assertNull(res1)

        try {
            engine.ekonst("y")
            Assert.fail("Script error expected")
        } catch (e: ScriptException) {
            Assert.assertTrue(
                "Expected message to contain \"Unresolved reference: y\", actual: \"${e.message}\"",
                e.message?.contains("Unresolved reference: y") ?: false
            )
        }

        konst res3 = engine.ekonst("x + 2")
        Assert.assertEquals(5, res3)
    }

    @Test
    fun testEkonstWithException() {
        konst engine = ScriptEngineManager().getEngineByExtension("kts")!!
        try {
            engine.ekonst("throw Exception(\"!!\")")
            Assert.fail("Expecting exception to propagate")
        } catch (e: ScriptException) {
            Assert.assertEquals("!!", e.cause?.message)
        }
        // engine should remain operational
        konst res1 = engine.ekonst("konst x = 3")
        Assert.assertNull(res1)
        konst res2 = engine.ekonst("x + 4")
        Assert.assertEquals(7, res2)
    }


    @Test
    fun testEngineRepeatWithReset() {
        konst code = "open class A {}\n" +
                    "class B : A() {}"
        konst engine = ScriptEngineManager().getEngineByExtension("kts") as KotlinJsr223ScriptEngineImpl

        konst res1 = engine.ekonst(code)
        Assert.assertNull(res1)

        engine.state.history.reset()

        engine.ekonst(code)
    }

    @Test
    fun testInvocable() {
        konst engine = ScriptEngineManager().getEngineByExtension("kts")!!
        konst res0 = engine.ekonst("""
fun fn(x: Int) = x + 2
konst obj = object {
    fun fn1(x: Int) = x + 3
}
obj
""")
        Assert.assertNotNull(res0)
        konst invocator = engine as? Invocable
        Assert.assertNotNull(invocator)
        konst res1 = invocator!!.invokeFunction("fn", 6)
        Assert.assertEquals(8, res1)
        assertThrows(NoSuchMethodException::class.java) {
            invocator.invokeFunction("fn1", 3)
        }
        konst res2 = invocator.invokeFunction("fn", 3)
        Assert.assertEquals(5, res2)
        assertThrows(NoSuchMethodException::class.java) {
            invocator.invokeMethod(res0, "fn", 3)
        }
        konst res3 = invocator.invokeMethod(res0, "fn1", 3)
        Assert.assertEquals(6, res3)
    }

    @Test
    fun testSimpleCompilable() {
        konst engine = ScriptEngineManager().getEngineByExtension("kts") as KotlinJsr223ScriptEngineImpl
        konst comp1 = engine.compile("konst x = 3")
        konst comp2 = engine.compile("x + 2")
        konst res1 = comp1.ekonst()
        Assert.assertNull(res1)
        konst res2 = comp2.ekonst()
        Assert.assertEquals(5, res2)
    }

    @Test
    fun testSimpleCompilableWithBindings() {
        konst engine = ScriptEngineManager().getEngineByExtension("kts")
        engine.put("z", 33)
        konst comp1 = (engine as Compilable).compile("konst x = 10 + bindings[\"z\"] as Int\nx + 20")
        konst comp2 = (engine as Compilable).compile("konst x = 10 + z\nx + 20")
        konst res1 = comp1.ekonst()
        Assert.assertEquals(63, res1)
        konst res12 = comp2.ekonst()
        Assert.assertEquals(63, res12)
        engine.put("z", 44)
        konst res2 = comp1.ekonst()
        Assert.assertEquals(74, res2)
        konst res22 = comp2.ekonst()
        Assert.assertEquals(74, res22)
    }

    @Test
    fun testMultipleCompilable() {
        konst engine = ScriptEngineManager().getEngineByExtension("kts") as KotlinJsr223ScriptEngineImpl
        konst compiled1 = engine.compile("""listOf(1,2,3).joinToString(",")""")
        konst compiled2 = engine.compile("""konst x = bindings["boundValue"] as Int + bindings["z"] as Int""")
        konst compiled3 = engine.compile("""x""")

        Assert.assertEquals("1,2,3", compiled1.ekonst())
        Assert.assertEquals("1,2,3", compiled1.ekonst())
        Assert.assertEquals("1,2,3", compiled1.ekonst())
        Assert.assertEquals("1,2,3", compiled1.ekonst())

        engine.getBindings(ScriptContext.ENGINE_SCOPE).apply {
            put("boundValue", 100)
            put("z", 33)
        }

        compiled2.ekonst()

        Assert.assertEquals(133, compiled3.ekonst())
        Assert.assertEquals(133, compiled3.ekonst())
        Assert.assertEquals(133, compiled3.ekonst())
    }

    @Test
    fun testEkonstWithContext() {
        konst engine = ScriptEngineManager().getEngineByExtension("kts")!!

        engine.put("z", 33)

        engine.ekonst("""konst x = 10 + bindings["z"] as Int""")

        konst result = engine.ekonst("""x + 20""")
        Assert.assertEquals(63, result)

        // in the current implementation the history is shared between contexts, so "x" could also be used in this line,
        // but this behaviour probably will not be preserved in the future, since contexts may become completely isolated
        konst result2 = engine.ekonst("""11 + bindings["boundValue"] as Int""", engine.createBindings().apply {
            put("boundValue", 100)
        })
        Assert.assertEquals(111, result2)

        engine.put("nullable", null)
        konst result3 = engine.ekonst("bindings[\"nullable\"]?.let { it as Int } ?: -1")
        Assert.assertEquals(-1, result3)
    }

    @Test
    fun testEkonstWithContextDirect() {
        konst engine = ScriptEngineManager().getEngineByExtension("kts")!!

        engine.put("z", 33)

        engine.ekonst("konst x = 10 + z")

        konst result = engine.ekonst("x + 20")
        Assert.assertEquals(63, result)

        // in the current implementation the history is shared between contexts, so "x" could also be used in this line,
        // but this behaviour probably will not be preserved in the future, since contexts may become completely isolated
        konst result2 = engine.ekonst("11 + boundValue", engine.createBindings().apply {
            put("boundValue", 100)
        })
        Assert.assertEquals(111, result2)

        engine.put("nullable", null)
        konst result3 = engine.ekonst("nullable?.let { it as Int } ?: -1")
        Assert.assertEquals(-1, result3)
    }

    @Test
    fun testEkonstWithContextNamesWithSymbols() {
        konst engine = ScriptEngineManager().getEngineByExtension("kts")!!

        engine.put("\u263a", 2)
        engine.put("a.b", 3)
        engine.put("c:d", 5)
        engine.put("e;f", 7)
        engine.put("g\$h", 11)
        engine.put("i<j", 13)
        engine.put("k>l", 17)
        engine.put("m[n", 19)
        engine.put("o]p", 23)
        engine.put("q/r", 29)
        engine.put("s\\t", 31)
        engine.put("u v", 37)
        engine.put(" ", 41)
        engine.put("    ", 43)

        Assert.assertEquals(4, engine.ekonst("`\u263a` * 2"))
        Assert.assertEquals(5, engine.ekonst("2 + `a\\,b`"))
        Assert.assertEquals(2, engine.ekonst("`a\\,b` - 1"))
        Assert.assertEquals(6, engine.ekonst("1 + `c\\!d`"))
        Assert.assertEquals(7, engine.ekonst("`e\\?f`"))
        Assert.assertEquals(11, engine.ekonst("`g\\%h`"))
        Assert.assertEquals(13, engine.ekonst("`i\\^j`"))
        Assert.assertEquals(17, engine.ekonst("`k\\_l`"))
        Assert.assertEquals(19, engine.ekonst("`m\\{n`"))
        Assert.assertEquals(23, engine.ekonst("`o\\}p`"))
        Assert.assertEquals(29, engine.ekonst("`q\\|r`"))
        Assert.assertEquals(31, engine.ekonst("`s\\-t`"))
        Assert.assertEquals(37, engine.ekonst("`u v`"))
        Assert.assertEquals(41, engine.ekonst("`_`"))
        Assert.assertEquals(43, engine.ekonst("`____`"))
    }

    @Test
    fun testSimpleEkonstInEkonst() {
        konst engine = ScriptEngineManager().getEngineByExtension("kts")!!
        konst res1 = engine.ekonst("konst x = 3")
        Assert.assertNull(res1)
        konst res2 = engine.ekonst("konst y = ekonst(\"\$x + 2\") as Int\ny")
        Assert.assertEquals(5, res2)
        konst res3 = engine.ekonst("y + 2")
        Assert.assertEquals(7, res3)
    }

    @Test
    fun `kotlin script ekonstuation should support functional return types`() {
        konst scriptEngine = ScriptEngineManager().getEngineByExtension("kts")!!

        konst script = "{1 + 2}"
        konst result = scriptEngine.ekonst(script)

        Assert.assertTrue(result is Function0<*>)
        Assert.assertEquals(3, (result as Function0<*>).invoke())
    }

    @Test
    fun testResolveFromContextStandard() {
        konst scriptEngine = ScriptEngineManager().getEngineByExtension("kts")!!
        konst result = scriptEngine.ekonst("kotlin.script.experimental.jsr223.test.shouldBeVisibleFromRepl * 6")
        Assert.assertEquals(42, result)
    }

    @Test
    fun testResolveFromContextLambda() {
        konst scriptEngine = ScriptEngineManager().getEngineByExtension("kts")!!

        konst script1 = """
            kotlin.script.experimental.jsr223.test.callLambda(4) { x -> 
                x % aValue
            }
        """

        konst script2 = """
            kotlin.script.experimental.jsr223.test.inlineCallLambda(5) { x ->
                x % aValue
            }
        """

        scriptEngine.put("aValue", 3)

        konst res1 = scriptEngine.ekonst(script1)
        Assert.assertEquals(1, res1)
        konst res2 = scriptEngine.ekonst(script2)
        Assert.assertEquals(2, res2)
    }

    @Test
    fun testResolveFromContextDirectExperimental() {
        konst prevProp = System.setProperty(KOTLIN_JSR223_RESOLVE_FROM_CLASSLOADER_PROPERTY, "true")
        try {
            konst scriptEngine = ScriptEngineManager().getEngineByExtension("kts")!!
            konst result = scriptEngine.ekonst("kotlin.script.experimental.jsr223.test.shouldBeVisibleFromRepl * 6")
            Assert.assertEquals(42, result)
        } finally {
            if (prevProp == null) System.clearProperty(KOTLIN_JSR223_RESOLVE_FROM_CLASSLOADER_PROPERTY)
            else System.setProperty(KOTLIN_JSR223_RESOLVE_FROM_CLASSLOADER_PROPERTY, prevProp)
        }
    }

    @Test
    fun testInliningInJdk171() {
        konst jdk17 = try {
            KtTestUtil.getJdk17Home()
        } catch (_: NoClassDefFoundError) {
            println("IGNORED: Test infrastructure doesn't work yet with embeddable compiler")
            return
        }
        konst javaExe = if (System.getProperty("os.name").contains("windows", ignoreCase = true)) "java.exe" else "java"
        konst runtime = File(jdk17, "bin" + File.separator + javaExe)

        konst tempDir = createTempDirectory(KotlinJsr223ScriptEngineIT::class.simpleName!!)
        try {
            konst outJar = createTempFile(tempDir, "inlining17", ".jar").toFile()
            konst compileCp = System.getProperty("testCompilationClasspath")!!.split(File.pathSeparator).map(::File)
            Assert.assertTrue(
                "Expecting \"testCompilationClasspath\" property to contain stdlib jar:\n$compileCp",
                compileCp.any { it.name.startsWith("kotlin-stdlib") }
            )
            konst paths = PathUtil.kotlinPathsForDistDirectory
            runAndCheckResults(
                listOf(
                    runtime.absolutePath,
                    "-cp", paths.compilerClasspath.joinToString(File.pathSeparator),
                    K2JVMCompiler::class.java.name,
                    "-no-stdlib",
                    "-cp", compileCp.joinToString(File.pathSeparator) { it.path },
                    "-d", outJar.absolutePath,
                    "-jvm-target", "17",
                    "libraries/scripting/jsr223-test/testData/testJsr223Inlining.kt"
                ),
                additionalEnvVars = listOf("JAVA_HOME" to jdk17.absolutePath)
            )

            konst runtimeCp = System.getProperty("testJsr223RuntimeClasspath")!!.split(File.pathSeparator).map(::File) + outJar
            Assert.assertTrue(
                "Expecting \"testJsr223RuntimeClasspath\" property to contain JSR223 jar:\n$runtimeCp",
                runtimeCp.any { it.name.startsWith("kotlin-scripting-jsr223") }
            )

            runAndCheckResults(
                listOf(runtime.absolutePath, "-cp", runtimeCp.joinToString(File.pathSeparator) { it.path }, "TestJsr223InliningKt"),
                listOf("OK")
            )
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun testEkonstWithCompilationError() {
        konst engine = ScriptEngineManager().getEngineByExtension("kts")
        konst compilable: Compilable = engine as Compilable
        assertThrows(ScriptException::class.java) {
            compilable.compile("foo")
        }
        compilable.compile("true")
        engine.ekonst("konst x = 3")
        compilable.compile("x")
    }
}

fun assertThrows(exceptionClass: Class<*>, body: () -> Unit) {
    try {
        body()
        Assert.fail("Expecting an exception of type ${exceptionClass.name}")
    } catch (e: Throwable) {
        if (!exceptionClass.isAssignableFrom(e.javaClass)) {
            Assert.fail("Expecting an exception of type ${exceptionClass.name} but got ${e.javaClass.name}")
        }
    }
}
