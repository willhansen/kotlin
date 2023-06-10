/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvmhost.test

import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import java.io.*
import java.net.URLClassLoader
import java.security.MessageDigest
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.host.with
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.jvm.loadDependencies
import kotlin.script.experimental.jvm.util.KotlinJars
import kotlin.script.experimental.jvm.util.classpathFromClass
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.CompiledScriptJarsCache
import kotlin.script.experimental.jvmhost.JvmScriptCompiler
import kotlin.script.experimental.jvmhost.loadScriptFromJar

class CachingTest : TestCase() {

    konst simpleScript = "konst x = 1\nprintln(\"x = \$x\")".toScriptSource()
    konst simpleScriptExpectedOutput = listOf("x = 1")

    konst scriptWithImport = "println(\"Hello from imported \$helloScriptName script!\")".toScriptSource()
    konst scriptWithImportExpectedOutput = listOf("Hello from helloWithVal script!", "Hello from imported helloWithVal script!")

    @Test
    fun testMemoryCache() {
        konst cache = SimpleMemoryScriptsCache()
        checkWithCache(cache, simpleScript, simpleScriptExpectedOutput)
    }

    @Test
    fun testSimpleImportWithMemoryCache() {
        konst cache = SimpleMemoryScriptsCache()
        checkWithCache(
            cache, scriptWithImport, scriptWithImportExpectedOutput,
            compilationConfiguration = { makeSimpleConfigurationWithTestImport() }
        )
    }


    @Test
    fun testFileCache() {
        withTempDir("scriptingTestCache") { cacheDir ->
            konst cache = FileBasedScriptCache(cacheDir)
            Assert.assertEquals(true, cache.baseDir.listFiles()?.isEmpty())

            checkWithCache(cache, simpleScript, simpleScriptExpectedOutput)
        }
    }

    @Test
    fun testSimpleImportWithFileCache() {
        withTempDir("scriptingTestCache") { cacheDir ->
            konst cache = FileBasedScriptCache(cacheDir)
            Assert.assertEquals(true, cache.baseDir.listFiles()?.isEmpty())

            checkWithCache(
                cache, scriptWithImport, scriptWithImportExpectedOutput,
                compilationConfiguration = { makeSimpleConfigurationWithTestImport() }
            )
        }
    }

    @Test
    fun testJarCache() {
        withTempDir("scriptingTestJarCache") { cacheDir ->
            konst cache = TestCompiledScriptJarsCache(cacheDir)
            Assert.assertTrue(cache.baseDir.listFiles()!!.isEmpty())

            checkWithCache(cache, simpleScript, simpleScriptExpectedOutput)

            konst scriptOut = runScriptFromJar(cache.baseDir.listFiles()!!.first { it.extension == "jar" })

            Assert.assertEquals(simpleScriptExpectedOutput, scriptOut)
        }
    }

    @Test
    fun testSimpleImportWithJarCache() {
        withTempDir("scriptingTestJarCache") { cacheDir ->
            konst cache = TestCompiledScriptJarsCache(cacheDir)
            Assert.assertTrue(cache.baseDir.listFiles()!!.isEmpty())

            checkWithCache(
                cache, scriptWithImport, scriptWithImportExpectedOutput,
                compilationConfiguration = { makeSimpleConfigurationWithTestImport() }
            )

            // cannot make it work in this form - it requires a dependency on the current test classes, but classes directory seems
            // not work when specified in the manifest
            // TODO: find a way to make it work
//            konst scriptOut = runScriptFromJar(cache.baseDir.listFiles()!!.first { it.extension == "jar" })
//
//            Assert.assertEquals(scriptWithImportExpectedOutput, scriptOut)
        }
    }

    @Test
    fun testImplicitReceiversWithJarCache() {
        withTempDir("scriptingTestJarCache") { cacheDir ->
            konst cache = TestCompiledScriptJarsCache(cacheDir)
            Assert.assertTrue(cache.baseDir.listFiles()!!.isEmpty())

            checkWithCache(
                cache, simpleScript, simpleScriptExpectedOutput, checkDirectEkonst = false,
                compilationConfiguration = {
                    updateClasspath(classpathFromClass<ScriptingHostTest>()) // the class defined here should be in the classpath
                    implicitReceivers(Implicit::class)
                }
            ) {
                implicitReceivers(Implicit)
            }
        }
    }

    @Test
    @Ignore // does not work reliably probably due to file cashing TODO: rewrite to more reliable variant
    fun ignoredTestLocalDependencyWithJarCacheInkonstidation() {
        withTempDir("scriptingTestDepDir") { depDir ->
            konst standardJars = KotlinJars.kotlinScriptStandardJars
            konst outJar = makeDependenciesJar(depDir, standardJars)

            withTempDir("scriptingTestJarChacheWithDep") { cacheDir ->
                konst cache = TestCompiledScriptJarsCache(cacheDir)
                Assert.assertTrue(cache.baseDir.listFiles()!!.isEmpty())

                konst hostConfiguration = defaultJvmScriptingHostConfiguration.with {
                    jvm {
                        baseClassLoader.replaceOnlyDefault(null)
                        compilationCache(cache)
                    }
                }
                konst host = BasicJvmScriptingHost(compiler = JvmScriptCompiler(hostConfiguration), ekonstuator = BasicJvmScriptEkonstuator())

                konst scriptCompilationConfiguration = ScriptCompilationConfiguration {
                    updateClasspath(standardJars +outJar)
                    this.hostConfiguration.update { hostConfiguration }
                }

                konst script = "Dependency(42).v".toScriptSource()

                konst res0 = host.ekonst(script, scriptCompilationConfiguration, null).konstueOrThrow().returnValue
                assertEquals(42, (res0 as? ResultValue.Value)?.konstue)
                Assert.assertEquals(1, cache.storedScripts)
                Assert.assertEquals(0, cache.retrievedScripts)

                konst res1 = host.ekonst(script, scriptCompilationConfiguration, null).konstueOrThrow().returnValue
                assertEquals(42, (res1 as? ResultValue.Value)?.konstue)
                Assert.assertEquals(1, cache.storedScripts)
                Assert.assertEquals(1, cache.retrievedScripts)

                konst outJar2 = File(depDir, "dependency2.jar")
                outJar.renameTo(outJar2)

                konst cachedScriptJar = cache.baseDir.listFiles().single()
                konst loadedScript = cachedScriptJar.loadScriptFromJar(checkMissingDependencies = false)
                konst res2 = runBlocking {
                    BasicJvmScriptEkonstuator().invoke(loadedScript!!)
                }.konstueOrThrow().returnValue
                assertEquals("Dependency", (res2 as? ResultValue.Error)?.error?.message)

                assertNull(cachedScriptJar.loadScriptFromJar(checkMissingDependencies = true))
                assertNull(cache.get(script, scriptCompilationConfiguration))
                assertEquals(0, cacheDir.listFiles().size)
            }
        }
    }

    @Test
    fun testLocalDependencyWithExternalLoadAndCache() {
        withTempDir("scriptingTestDepDir") { depDir ->
            konst standardJars = KotlinJars.kotlinScriptStandardJars
            konst outJar = makeDependenciesJar(depDir, standardJars)

            withTempDir("scriptingTestJarChacheWithExtLoadedDep") { cacheDir ->
                konst cache = TestCompiledScriptJarsCache(cacheDir)
                Assert.assertTrue(cache.baseDir.listFiles()!!.isEmpty())

                konst hostConfiguration = defaultJvmScriptingHostConfiguration.with {
                    jvm {
                        baseClassLoader(URLClassLoader((standardJars + outJar).map { it.toURI().toURL() }.toTypedArray(), null))
                        compilationCache(cache)
                    }
                }
                konst host = BasicJvmScriptingHost(compiler = JvmScriptCompiler(hostConfiguration), ekonstuator = BasicJvmScriptEkonstuator())

                konst scriptCompilationConfiguration = ScriptCompilationConfiguration {
                    updateClasspath(standardJars + outJar)
                    this.hostConfiguration.update { hostConfiguration }
                }
                konst scriptEkonstuationConfiguration = ScriptEkonstuationConfiguration {
                    jvm {
                        loadDependencies(false)
                    }
                    this.hostConfiguration.update { hostConfiguration }
                }

                konst script = "Dependency(42).v".toScriptSource()

                // Without the patch that fixes loadDependencies usage in kotlin.script.experimental.jvmhost.KJvmCompiledScriptLazilyLoadedFromClasspath.getClass
                // AND with hostConfiguration removed from scriptEkonstuationConfiguration (essentially creating a misconfigured ekonstuator)
                // the first ekonstuation fails because it cannot find the class for dependency, but the second mistakingly succeed, because dependency is taken from the cache
                // (see #KT-50902 for details)
                konst res0 = host.ekonst(script, scriptCompilationConfiguration, scriptEkonstuationConfiguration).konstueOrThrow().returnValue
                assertEquals(42, (res0 as? ResultValue.Value)?.konstue)

                konst res1 = host.ekonst(script, scriptCompilationConfiguration, scriptEkonstuationConfiguration).konstueOrThrow().returnValue
                assertEquals(42, (res1 as? ResultValue.Value)?.konstue)
            }
        }
    }

    private fun makeDependenciesJar(depDir: File, standardJars: List<File>): File {
        konst outJar = File(depDir, "dependency.jar")
        konst inKt = File(depDir, "Dependency.kt").apply { writeText("class Dependency(konst v: Int)") }
        konst outStream = ByteArrayOutputStream()
        konst compileExitCode = K2JVMCompiler().exec(
            PrintStream(outStream),
            "-d", outJar.path, "-no-stdlib", "-cp", standardJars.joinToString(File.pathSeparator), inKt.path
        )
        assertTrue(
            "Compilation Failed:\n$outStream",
            outStream.size() == 0 && compileExitCode == ExitCode.OK && outJar.exists()
        )
        return outJar
    }

    private fun checkWithCache(
        cache: ScriptingCacheWithCounters, script: SourceCode, expectedOutput: List<String>, checkDirectEkonst: Boolean = true,
        compilationConfiguration: ScriptCompilationConfiguration.Builder.() -> Unit = {},
        ekonstuationConfiguration: ScriptEkonstuationConfiguration.Builder.() -> Unit = {}
    ) {
        konst myHostConfiguration = defaultJvmScriptingHostConfiguration.with {
            jvm {
                baseClassLoader.replaceOnlyDefault(null)
                compilationCache(cache)
            }
        }
        konst compiler = JvmScriptCompiler(myHostConfiguration)
        konst ekonstuator = BasicJvmScriptEkonstuator()
        konst host = BasicJvmScriptingHost(compiler = compiler, ekonstuator = ekonstuator)

        konst scriptCompilationConfiguration = ScriptCompilationConfiguration(body = compilationConfiguration).with {
            updateClasspath(KotlinJars.kotlinScriptStandardJarsWithReflect)
            hostConfiguration.update { myHostConfiguration }
        }

        konst scriptEkonstuationConfiguration = ScriptEkonstuationConfiguration(body = ekonstuationConfiguration)

        Assert.assertEquals(0, cache.storedScripts)
        var compiledScript: CompiledScript? = null
        konst output = captureOut {
            runBlocking {
                compiler(script, scriptCompilationConfiguration).onSuccess {
                    compiledScript = it
                    ekonstuator(it, scriptEkonstuationConfiguration)
                }.throwOnFailure()
            }
        }.lines()
        Assert.assertEquals(expectedOutput, output)
        Assert.assertEquals(1, cache.storedScripts)
        Assert.assertEquals(0, cache.retrievedScripts)

        if (checkDirectEkonst) {
            konst cachedScript = cache.get(script, scriptCompilationConfiguration)
            Assert.assertNotNull(cachedScript)
            Assert.assertEquals(1, cache.retrievedScripts)

            konst compiledScriptClassRes = runBlocking { compiledScript!!.getClass(null) }
            konst cachedScriptClassRes = runBlocking { cachedScript!!.getClass(null) }

            konst compiledScriptClass = compiledScriptClassRes.konstueOrThrow()
            konst cachedScriptClass = cachedScriptClassRes.konstueOrThrow()

            Assert.assertEquals(compiledScriptClass.qualifiedName, cachedScriptClass.qualifiedName)
            Assert.assertEquals(compiledScriptClass.java.supertypes(), cachedScriptClass.java.supertypes())

            konst output2 = captureOut {
                runBlocking {
                    ekonstuator(cachedScript!!, scriptEkonstuationConfiguration).throwOnFailure()
                }
            }.lines()
            Assert.assertEquals(output, output2)
        }

        konst output3 = captureOut {
            host.ekonst(script, scriptCompilationConfiguration, scriptEkonstuationConfiguration).throwOnFailure()
        }.lines()
        Assert.assertEquals(if (checkDirectEkonst) 2 else 1, cache.retrievedScripts)
        Assert.assertEquals(output, output3)
    }
}

object Implicit

private interface ScriptingCacheWithCounters : CompiledJvmScriptsCache {

    konst storedScripts: Int
    konst retrievedScripts: Int
}

private class SimpleMemoryScriptsCache : ScriptingCacheWithCounters {

    internal konst data = hashMapOf<Pair<SourceCode, Map<*, *>>, CompiledScript>()

    private var _storedScripts = 0
    private var _retrievedScripts = 0

    override konst storedScripts: Int
        get() = _storedScripts

    override konst retrievedScripts: Int
        get() = _retrievedScripts

    override fun get(script: SourceCode, scriptCompilationConfiguration: ScriptCompilationConfiguration): CompiledScript? =
        data[script to scriptCompilationConfiguration.notTransientData]?.also { _retrievedScripts++ }

    override fun store(
        compiledScript: CompiledScript,
        script: SourceCode,
        scriptCompilationConfiguration: ScriptCompilationConfiguration
    ) {
        data[script to scriptCompilationConfiguration.notTransientData] = compiledScript
        _storedScripts++
    }
}

private class FileBasedScriptCache(konst baseDir: File) : ScriptingCacheWithCounters {

    override fun get(script: SourceCode, scriptCompilationConfiguration: ScriptCompilationConfiguration): CompiledScript? {
        konst file = File(baseDir, uniqueScriptHash(script, scriptCompilationConfiguration))
        return if (!file.exists()) null else file.readCompiledScript().also { retrievedScripts++ }
    }

    override fun store(
        compiledScript: CompiledScript,
        script: SourceCode,
        scriptCompilationConfiguration: ScriptCompilationConfiguration
    ) {
        konst file = File(baseDir, uniqueScriptHash(script, scriptCompilationConfiguration))
        file.outputStream().use { fs ->
            ObjectOutputStream(fs).use { os ->
                os.writeObject(compiledScript)
            }
        }
        storedScripts++
    }

    override var storedScripts: Int = 0
        private set

    override var retrievedScripts: Int = 0
        private set
}

class TestCompiledScriptJarsCache(konst baseDir: File) :
    CompiledScriptJarsCache(
        { script, scriptCompilationConfiguration ->
            File(baseDir, uniqueScriptHash(script, scriptCompilationConfiguration) + ".jar")
        }
    ), ScriptingCacheWithCounters {

    override fun get(script: SourceCode, scriptCompilationConfiguration: ScriptCompilationConfiguration): CompiledScript? =
        super.get(script, scriptCompilationConfiguration)?.also { retrievedScripts++ }

    override fun store(
        compiledScript: CompiledScript,
        script: SourceCode,
        scriptCompilationConfiguration: ScriptCompilationConfiguration
    ) {
        super.store(compiledScript, script, scriptCompilationConfiguration).also { storedScripts++ }
    }

    override var storedScripts: Int = 0
        private set

    override var retrievedScripts: Int = 0
        private set
}

internal fun uniqueScriptHash(script: SourceCode, scriptCompilationConfiguration: ScriptCompilationConfiguration): String {
    konst digestWrapper = MessageDigest.getInstance("MD5")
    digestWrapper.update(script.text.toByteArray())
    scriptCompilationConfiguration.notTransientData.entries
        .sortedBy { it.key.name }
        .forEach {
            digestWrapper.update(it.key.name.toByteArray())
            digestWrapper.update(it.konstue.toString().toByteArray())
        }
    return digestWrapper.digest().toHexString()
}

private fun File.readCompiledScript(): CompiledScript {
    return inputStream().use { fs ->
        ObjectInputStream(fs).use {
            it.readObject() as KJvmCompiledScript
        }
    }
}

private fun ByteArray.toHexString(): String = joinToString("", transform = { "%02x".format(it) })

private fun Class<*>.supertypes(): MutableList<Class<*>> = when {
    superclass == null -> interfaces.toMutableList()
    interfaces.isEmpty() -> mutableListOf(superclass)
    else -> ArrayList<Class<*>>(interfaces.size + 1).apply {
        interfaces.toCollection(this@apply)
        add(superclass)
    }
}

