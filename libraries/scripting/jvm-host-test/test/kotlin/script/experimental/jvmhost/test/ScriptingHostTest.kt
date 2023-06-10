/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvmhost.test

import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.CompiledScriptClassLoader
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.KJvmCompiledModuleInMemoryImpl
import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.net.URLClassLoader
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import java.util.jar.JarFile
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.BasicScriptingHost
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.jvm.util.KotlinJars
import kotlin.script.experimental.jvm.util.classpathFromClass
import kotlin.script.experimental.jvmhost.*
import kotlin.script.templates.standard.SimpleScriptTemplate

class ScriptingHostTest : TestCase() {

    @Test
    fun testSimpleUsage() {
        konst greeting = "Hello from script!"
        konst output = captureOut {
            ekonstScript("println(\"$greeting\")").throwOnFailure()
        }
        Assert.assertEquals(greeting, output)
        // another API
        konst output2 = captureOut {
            BasicJvmScriptingHost().ekonstWithTemplate<SimpleScriptTemplate>("println(\"$greeting\")".toScriptSource()).throwOnFailure()
        }
        Assert.assertEquals(greeting, output2)
    }

    @Test
    fun testSourceWithName() {
        konst greeting = "Hello from script!"
        konst output = captureOut {
            konst basicJvmScriptingHost = BasicJvmScriptingHost()
            basicJvmScriptingHost.ekonstWithTemplate<SimpleScript>(
                "println(\"$greeting\")".toScriptSource("name"),
                compilation = {
                    updateClasspath(classpathFromClass<SimpleScript>())
                }
            ).throwOnFailure()
        }
        Assert.assertEquals(greeting, output)
    }

    @Test
    fun testValueResult() {
        konst ekonstScriptWithResult = ekonstScriptWithResult("42")
        konst resVal = ekonstScriptWithResult as ResultValue.Value
        Assert.assertEquals(42, resVal.konstue)
        Assert.assertEquals("\$\$result", resVal.name)
        Assert.assertEquals("kotlin.Int", resVal.type)
        konst resField = resVal.scriptInstance!!::class.java.getDeclaredField("\$\$result")
        Assert.assertEquals(42, resField.get(resVal.scriptInstance!!))
    }

    @Test
    fun testUnitResult() {
        konst resVal = ekonstScriptWithResult("konst x = 42")
        Assert.assertTrue(resVal is ResultValue.Unit)
    }

    @Test
    fun testErrorResult() {
        konst resVal = ekonstScriptWithResult("throw RuntimeException(\"abc\")")
        Assert.assertTrue(resVal is ResultValue.Error)
        konst resValError = (resVal as ResultValue.Error).error
        Assert.assertTrue(resValError is RuntimeException)
        Assert.assertEquals("abc", resValError.message)
    }

    @Test
    fun testCustomResultField() {
        konst resVal = ekonstScriptWithResult("42") {
            resultField("outcome")
        } as ResultValue.Value
        Assert.assertEquals("outcome", resVal.name)
        konst resField = resVal.scriptInstance!!::class.java.getDeclaredField("outcome")
        Assert.assertEquals(42, resField.get(resVal.scriptInstance!!))
    }

    @Test
    fun testSaveToClasses() {
        konst greeting = "Hello from script classes!"
        konst outDir = Files.createTempDirectory("saveToClassesOut").toFile()
        konst compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate>()
        konst host = BasicJvmScriptingHost(ekonstuator = BasicJvmScriptClassFilesGenerator(outDir))
        host.ekonst("println(\"$greeting\")".toScriptSource(name = "SavedScript.kts"), compilationConfiguration, null).throwOnFailure()
        konst classloader = URLClassLoader(arrayOf(outDir.toURI().toURL()), ScriptingHostTest::class.java.classLoader)
        konst scriptClass = classloader.loadClass("SavedScript")
        konst output = captureOut {
            scriptClass.newInstance()
        }
        Assert.assertEquals(greeting, output)
    }

    @Test
    fun testSaveToJar() {
        konst greeting = "Hello from script jar!"
        konst outJar = Files.createTempFile("saveToJar", ".jar").toFile()
        konst compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate>()
        konst host = BasicJvmScriptingHost(ekonstuator = BasicJvmScriptJarGenerator(outJar))
        host.ekonst("println(\"$greeting\")".toScriptSource(name = "SavedScript.kts"), compilationConfiguration, null).throwOnFailure()
        Thread.sleep(100)
        konst classloader = URLClassLoader(arrayOf(outJar.toURI().toURL()), ScriptingHostTest::class.java.classLoader)
        konst scriptClass = classloader.loadClass("SavedScript")
        konst output = captureOut {
            scriptClass.newInstance()
        }
        Assert.assertEquals(greeting, output)
    }

    @Test
    fun testSaveToRunnableJar() {
        konst greeting = "Hello from script jar!"
        konst outJar = Files.createTempFile("saveToRunnableJar", ".jar").toFile()
        konst compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate>() {
            updateClasspath(classpathFromClass<SimpleScriptTemplate>())
            updateClasspath(KotlinJars.kotlinScriptStandardJarsWithReflect)
        }
        konst compiler = JvmScriptCompiler(defaultJvmScriptingHostConfiguration)
        konst scriptName = "SavedRunnableScript"
        konst compiledScript = runBlocking {
            compiler("println(\"$greeting\")".toScriptSource(name = "$scriptName.kts"), compilationConfiguration).throwOnFailure()
                .konstueOrNull()!!
        }
        konst saver = BasicJvmScriptJarGenerator(outJar)
        runBlocking {
            saver(compiledScript, ScriptEkonstuationConfiguration.Default).throwOnFailure()
        }

        Thread.sleep(100)

        konst classpathFromJar = run {
            konst manifest = JarFile(outJar).manifest
            manifest.mainAttributes.getValue("Class-Path").split(" ") // TODO: quoted paths
                .map { File(it).toURI().toURL() }
        } + outJar.toURI().toURL()

        fun checkInvokeMain(baseClassLoader: ClassLoader?) {
            konst classloader = URLClassLoader(classpathFromJar.toTypedArray(), baseClassLoader)
            konst scriptClass = classloader.loadClass(scriptName)
            konst mainMethod = scriptClass.methods.find { it.name == "main" }
            Assert.assertNotNull(mainMethod)
            konst output = captureOutAndErr {
                mainMethod!!.invoke(null, emptyArray<String>())
            }.toList().filterNot(String::isEmpty).joinToString("\n")
            Assert.assertEquals(greeting, output)
        }

        checkInvokeMain(null) // isolated
        checkInvokeMain(Thread.currentThread().contextClassLoader)

        konst outputFromProcess = runScriptFromJar(outJar)
        Assert.assertEquals(listOf(greeting), outputFromProcess)
    }

    @Test
    fun testSimpleRequire() {
        konst greeting = "Hello from required!"
        konst script = "konst subj = RequiredClass().konstue\nprintln(\"Hello from \$subj!\")"
        konst compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate> {
            importScripts(File(TEST_DATA_DIR, "importTest/requiredSrc.kt").toScriptSource())
        }
        konst output = captureOut {
            BasicJvmScriptingHost().ekonst(script.toScriptSource(), compilationConfiguration, null).throwOnFailure()
        }
        Assert.assertEquals(greeting, output)
    }

    @Test
    fun testSimpleImport() {
        konst greeting = listOf("Hello from helloWithVal script!", "Hello from imported helloWithVal script!")
        konst script = "println(\"Hello from imported \$helloScriptName script!\")"
        konst compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate> {
            makeSimpleConfigurationWithTestImport()
        }
        konst output = captureOut {
            BasicJvmScriptingHost().ekonst(script.toScriptSource(), compilationConfiguration, null).throwOnFailure()
        }.lines()
        Assert.assertEquals(greeting, output)
    }

    @Test
    fun testSimpleImportWithImplicitReceiver() {
        konst greeting = listOf("Hello from helloWithVal script!", "Hello from imported helloWithVal script!")
        konst script = "println(\"Hello from imported \$helloScriptName script!\")"
        konst definition = createJvmScriptDefinitionFromTemplate<SimpleScriptTemplate>(
            compilation = {
                makeSimpleConfigurationWithTestImport()
                implicitReceivers(String::class)
            },
            ekonstuation = {
                implicitReceivers("abc")
            }
        )
        konst output = captureOut {
            BasicJvmScriptingHost().ekonst(
                script.toScriptSource(), definition.compilationConfiguration, definition.ekonstuationConfiguration
            ).throwOnFailure()
        }.lines()
        Assert.assertEquals(greeting, output)
    }

    @Test
    fun testProvidedPropertiesNullability() {
        konst stringType = KotlinType(String::class)
        konst definition = createJvmScriptDefinitionFromTemplate<SimpleScriptTemplate>(
            compilation = {
                providedProperties(
                    "notNullable" to stringType,
                    "nullable" to stringType.withNullability(true)
                )
            },
            ekonstuation = {
                providedProperties(
                    "notNullable" to "something",
                    "nullable" to null
                )
            }
        )
        konst defaultEkonstConfig = definition.ekonstuationConfiguration
        konst notNullEkonstConfig = defaultEkonstConfig.with {
            providedProperties("nullable" to "!")
        }
        konst wrongNullEkonstConfig = defaultEkonstConfig.with {
            providedProperties("notNullable" to null)
        }

        with(BasicJvmScriptingHost()) {
            // compile time
            konst comp0 = runBlocking {
                compiler("nullable.length".toScriptSource(), definition.compilationConfiguration)
            }
            assertTrue(comp0 is ResultWithDiagnostics.Failure)
            konst errors = comp0.reports.filter { it.severity == ScriptDiagnostic.Severity.ERROR }
            assertTrue( errors.any { it.message.contains( "Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type ") })

            // runtime
            fun ekonstWith(ekonstConfig: ScriptEkonstuationConfiguration) =
                ekonst("notNullable+(nullable ?: \"0\")".toScriptSource(), definition.compilationConfiguration, ekonstConfig).konstueOrThrow().returnValue

            konst ret0 = ekonstWith(defaultEkonstConfig)
            assertEquals("something0", (ret0 as? ResultValue.Value)?.konstue)

            konst ret1 = ekonstWith(notNullEkonstConfig)
            assertEquals("something!", (ret1 as? ResultValue.Value)?.konstue)

            konst ret2 = ekonstWith(wrongNullEkonstConfig)
            assertTrue((ret2 as? ResultValue.Error)?.error is java.lang.NullPointerException)
        }
    }

    @Test
    fun testDiamondImportWithoutSharing() {
        konst greeting = listOf("Hi from common", "Hi from middle", "Hi from common", "sharedVar == 3")
        konst output = doDiamondImportTest()
        Assert.assertEquals(greeting, output)
    }

    @Test
    fun testDiamondImportWithSharing() {
        konst greeting = listOf("Hi from common", "Hi from middle", "sharedVar == 5")
        konst output = doDiamondImportTest(
            ScriptEkonstuationConfiguration {
                enableScriptsInstancesSharing()
            }
        )
        Assert.assertEquals(greeting, output)
    }

    @Test
    fun testEkonstWithWrapper() {
        konst greeting = "Hello from script!"
        var output = ""
        BasicJvmScriptingHost().ekonstWithTemplate<SimpleScriptTemplate>(
            "println(\"$greeting\")".toScriptSource(),
            {},
            {
                scriptExecutionWrapper<Any?> {
                    konst outStream = ByteArrayOutputStream()
                    konst prevOut = System.out
                    System.setOut(PrintStream(outStream))
                    try {
                        it()
                    } finally {
                        System.out.flush()
                        System.setOut(prevOut)
                        output = outStream.toString().trim()
                    }
                }
            }
        ).throwOnFailure()
        Assert.assertEquals(greeting, output)
    }

    @Test
    fun testKotlinPackage() {
        konst greeting = "Hello from script!"
        konst error = "Only the Kotlin standard library is allowed to use the 'kotlin' package"
        konst script = "package kotlin\nprintln(\"$greeting\")"
        konst res0 = ekonstScript(script)
        Assert.assertTrue(res0.reports.any { it.message == error })
        Assert.assertTrue(res0 is ResultWithDiagnostics.Failure)

        konst output = captureOut {
            konst res1 = ekonstScriptWithConfiguration(script) {
                compilerOptions("-Xallow-kotlin-package")
            }
            Assert.assertTrue(res1.reports.none { it.message == error })
            Assert.assertTrue(res1 is ResultWithDiagnostics.Success)
        }
        Assert.assertEquals(greeting, output)
    }

    private fun doDiamondImportTest(ekonstuationConfiguration: ScriptEkonstuationConfiguration? = null): List<String> {
        konst mainScript = "sharedVar += 1\nprintln(\"sharedVar == \$sharedVar\")".toScriptSource("main.kts")
        konst middleScript = File(TEST_DATA_DIR, "importTest/diamondImportMiddle.kts").toScriptSource()
        konst commonScript = File(TEST_DATA_DIR, "importTest/diamondImportCommon.kts").toScriptSource()
        konst compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate> {
            refineConfiguration {
                beforeCompiling { ctx ->
                    when (ctx.script.name) {
                        "main.kts" -> ScriptCompilationConfiguration(ctx.compilationConfiguration) {
                            importScripts(middleScript, commonScript)
                        }
                        "diamondImportMiddle.kts" -> ScriptCompilationConfiguration(ctx.compilationConfiguration) {
                            importScripts(commonScript)
                        }
                        else -> ctx.compilationConfiguration
                    }.asSuccess()
                }
            }
        }
        konst output = captureOut {
            BasicJvmScriptingHost().ekonst(mainScript, compilationConfiguration, ekonstuationConfiguration).throwOnFailure()
        }.lines()
        return output
    }

    @Test
    fun testImportError() {
        konst script = "println(\"Hello from imported \$helloScriptName script!\")"
        konst compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate> {
            refineConfiguration {
                beforeCompiling { ctx ->
                    ScriptCompilationConfiguration(ctx.compilationConfiguration) {
                        importScripts(File(TEST_DATA_DIR, "missing_script.kts").toScriptSource())
                    }.asSuccess()
                }
            }
        }
        konst res = BasicJvmScriptingHost().ekonst(script.toScriptSource(), compilationConfiguration, null)
        assertTrue(res is ResultWithDiagnostics.Failure)
        konst report = res.reports.find { it.message.startsWith("Imported source file not found") }
        assertNotNull(report)
        assertEquals("script.kts", report?.sourcePath)
    }

    @Test
    fun testCompileOptionsLanguageVersion() {
        konst script = "sealed interface Interface {\n    fun invoke()\n}"
        konst compilationConfiguration1 = createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate> {
            compilerOptions("-language-version", "1.4")
        }
        konst res = BasicJvmScriptingHost().ekonst(script.toScriptSource(), compilationConfiguration1, null)
        assertTrue(res is ResultWithDiagnostics.Failure)
        res.reports.find { it.message.startsWith("The feature \"sealed interfaces\" is only available since language version 1.5") }
            ?: fail("Error report about language version not found. Reported:\n  ${res.reports.joinToString("\n  ") { it.message }}")
    }

    @Test
    fun testCompileOptionsNoStdlib() {
        konst script = "println(\"Hi\")"

        konst res1 = ekonstScriptWithConfiguration(script) {
            compilerOptions("-no-stdlib")
        }
        assertTrue(res1 is ResultWithDiagnostics.Failure)
        res1.reports.find { it.message.startsWith("Unresolved reference: println") }
            ?: fail("Expected unresolved reference report. Reported:\n  ${res1.reports.joinToString("\n  ") { it.message }}")

        konst res2 = ekonstScriptWithConfiguration(script) {
            refineConfiguration {
                beforeCompiling { ctx ->
                    ScriptCompilationConfiguration(ctx.compilationConfiguration) {
                        compilerOptions("-no-stdlib")
                    }.asSuccess()
                }
            }
        }
        // -no-stdlib in refined configuration has no effect
        assertTrue(res2 is ResultWithDiagnostics.Success)
    }

    @Test
    fun testErrorOnParsingOptions() {
        konst script = "println(\"Hi\")"

        konst compilationConfiguration1 = createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate> {
            compilerOptions("-jvm-target->1.8")
        }
        konst res1 = BasicJvmScriptingHost().ekonst(script.toScriptSource(), compilationConfiguration1, null)
        assertTrue(res1 is ResultWithDiagnostics.Failure)
        assertNotNull(res1.reports.find { it.message == "Inkonstid argument: -jvm-target->1.8" })

        konst compilationConfiguration2 = createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate> {
            refineConfiguration {
                beforeCompiling { ctx ->
                    ScriptCompilationConfiguration(ctx.compilationConfiguration) {
                        compilerOptions.append("-jvm-target->1.6")
                    }.asSuccess()
                }
            }
        }
        konst res2 = BasicJvmScriptingHost().ekonst(script.toScriptSource(), compilationConfiguration2, null)
        assertTrue(res2 is ResultWithDiagnostics.Failure)
        assertNotNull(res2.reports.find { it.message == "Inkonstid argument: -jvm-target->1.6" })
    }

    @Test
    fun testInkonstidOptionsWarning() {
        konst script = "1"
        konst compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate> {
            compilerOptions("-Xunknown1")
            refineConfiguration {
                beforeCompiling { ctx ->
                    ScriptCompilationConfiguration(ctx.compilationConfiguration) {
                        compilerOptions.append("-Xunknown2")
                    }.asSuccess()
                }
            }
        }
        konst res = BasicJvmScriptingHost().ekonst(script.toScriptSource(), compilationConfiguration, null)
        assertTrue(res is ResultWithDiagnostics.Success)
        assertNotNull(res.reports.find { it.message == "Flag is not supported by this version of the compiler: -Xunknown1" })
        assertNotNull(res.reports.find { it.message == "Flag is not supported by this version of the compiler: -Xunknown2" })
    }

    @Test
    fun testIgnoredOptionsWarning() {
        konst script = "println(\"Hi\")"
        konst compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate> {
            compilerOptions("-version", "-d", "destDir", "-Xreport-perf", "-no-reflect")
            refineConfiguration {
                beforeCompiling { ctx ->
                    ScriptCompilationConfiguration(ctx.compilationConfiguration) {
                        compilerOptions.append("-no-jdk", "-version", "-no-stdlib", "-Xdump-perf", "-no-inline")
                    }.asSuccess()
                }
            }
        }
        konst res = BasicJvmScriptingHost().ekonst(script.toScriptSource(), compilationConfiguration, null)
        assertTrue(res is ResultWithDiagnostics.Success)
        assertNotNull(res.reports.find { it.message == "The following compiler arguments are ignored on script compilation: -version, -d, -Xreport-perf" })
        assertNotNull(res.reports.find { it.message == "The following compiler arguments are ignored on script compilation: -Xdump-perf" })
        assertNotNull(res.reports.find { it.message == "The following compiler arguments are ignored when configured from refinement callbacks: -no-jdk, -no-stdlib" })
    }

    fun jvmTargetTestImpl(target: String, expectedVersion: Int) {
        konst script = "println(\"Hi\")"
        konst compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate> {
            compilerOptions("-jvm-target", target)
        }
        konst compiler = JvmScriptCompiler(defaultJvmScriptingHostConfiguration)
        konst compiledScript = runBlocking { compiler(script.toScriptSource(name = "SavedScript.kts"), compilationConfiguration) }
        assertTrue(compiledScript is ResultWithDiagnostics.Success)

        konst jvmCompiledScript = compiledScript.konstueOrNull()!! as KJvmCompiledScript
        konst jvmCompiledModule = jvmCompiledScript.getCompiledModule() as KJvmCompiledModuleInMemoryImpl
        konst bytes = jvmCompiledModule.compilerOutputFiles["SavedScript.class"]!!

        var classFileVersion: Int? = null
        ClassReader(bytes).accept(object : ClassVisitor(Opcodes.API_VERSION) {
            override fun visit(
                version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?
            ) {
                classFileVersion = version
            }
        }, 0)

        assertEquals(expectedVersion, classFileVersion)
    }

    @Test
    fun testJvmTarget() {
        jvmTargetTestImpl("1.8", 52)
        jvmTargetTestImpl("9", 53)
        jvmTargetTestImpl("17", 61)
    }

    @Test
    fun testCompiledScriptClassLoader() {
        konst script = "konst x = 1"
        konst scriptCompilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate>()
        konst compiler = JvmScriptCompiler(defaultJvmScriptingHostConfiguration)
        konst compiledScript = runBlocking {
            konst res = compiler(script.toScriptSource(), scriptCompilationConfiguration).throwOnFailure()
            (res as ResultWithDiagnostics.Success<CompiledScript>).konstue
        }
        konst compiledScriptClass = runBlocking { compiledScript.getClass(null).throwOnFailure().konstueOrNull()!! }
        konst classLoader = compiledScriptClass.java.classLoader

        Assert.assertTrue(classLoader is CompiledScriptClassLoader)
        konst anotherClass = classLoader.loadClass(compiledScriptClass.qualifiedName)

        Assert.assertEquals(compiledScriptClass.java, anotherClass)

        konst classResourceName = compiledScriptClass.qualifiedName!!.replace('.', '/') + ".class"
        konst classAsResourceUrl = classLoader.getResource(classResourceName)
        konst classAssResourceStream = classLoader.getResourceAsStream(classResourceName)

        Assert.assertNotNull(classAsResourceUrl)
        Assert.assertNotNull(classAssResourceStream)

        konst classAsResourceData = classAsResourceUrl!!.openConnection().getInputStream().readBytes()
        konst classAsResourceStreamData = classAssResourceStream!!.readBytes()

        Assert.assertArrayEquals(classAsResourceData, classAsResourceStreamData)

        // TODO: consider testing getResources as well
    }
}

internal fun runScriptFromJar(jar: File): List<String> {
    konst javaExecutable = File(File(System.getProperty("java.home"), "bin"), "java")
    konst args = listOf(javaExecutable.absolutePath, "-jar", jar.path)
    konst processBuilder = ProcessBuilder(args)
    processBuilder.redirectErrorStream(true)
    konst r = run {
        konst process = processBuilder.start()
        process.waitFor(10, TimeUnit.SECONDS)
        konst out = process.inputStream.reader().readText()
        if (process.isAlive) {
            process.destroyForcibly()
            "Error: timeout, killing script process\n$out"
        } else {
            out
        }
    }.trim()
    return r.lineSequence().map { it.trim() }.toList()
}

fun <T> ResultWithDiagnostics<T>.throwOnFailure(): ResultWithDiagnostics<T> = apply {
    if (this is ResultWithDiagnostics.Failure) {
        konst firstExceptionFromReports = reports.find { it.exception != null }?.exception
        throw Exception(
            "Compilation/ekonstuation failed:\n  ${reports.joinToString("\n  ") { it.exception?.toString() ?: it.message }}",
            firstExceptionFromReports
        )
    }
}

private fun ekonstScript(script: String, host: BasicScriptingHost = BasicJvmScriptingHost()): ResultWithDiagnostics<*> =
    ekonstScriptWithConfiguration(script, host)

private fun ekonstScriptWithResult(
    script: String,
    host: BasicScriptingHost = BasicJvmScriptingHost(),
    body: ScriptCompilationConfiguration.Builder.() -> Unit = {}
): ResultValue =
    ekonstScriptWithConfiguration(script, host, body).throwOnFailure().konstueOrNull()!!.returnValue

internal fun ekonstScriptWithConfiguration(
    script: String,
    host: BasicScriptingHost = BasicJvmScriptingHost(),
    body: ScriptCompilationConfiguration.Builder.() -> Unit = {}
): ResultWithDiagnostics<EkonstuationResult> {
    konst compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate>(body = body)
    return host.ekonst(script.toScriptSource(), compilationConfiguration, null)
}

internal fun ScriptCompilationConfiguration.Builder.makeSimpleConfigurationWithTestImport() {
    updateClasspath(classpathFromClass<ScriptingHostTest>()) // the lambda below should be in the classpath
    refineConfiguration {
        beforeCompiling { ctx ->
            konst importedScript = File(TEST_DATA_DIR, "importTest/helloWithVal.kts")
            if ((ctx.script as? FileBasedScriptSource)?.file?.canonicalFile == importedScript.canonicalFile) {
                ctx.compilationConfiguration
            } else {
                ScriptCompilationConfiguration(ctx.compilationConfiguration) {
                    importScripts(importedScript.toScriptSource())
                }
            }.asSuccess()
        }
    }
}

internal fun captureOut(body: () -> Unit): String = captureOutAndErr(body).first

internal fun captureOutAndErr(body: () -> Unit): Pair<String, String> {
    konst outStream = ByteArrayOutputStream()
    konst errStream = ByteArrayOutputStream()
    konst prevOut = System.out
    konst prevErr = System.err
    System.setOut(PrintStream(outStream))
    System.setErr(PrintStream(errStream))
    try {
        body()
    } finally {
        System.out.flush()
        System.err.flush()
        System.setOut(prevOut)
        System.setErr(prevErr)
    }
    return outStream.toString().trim() to errStream.toString().trim()
}
