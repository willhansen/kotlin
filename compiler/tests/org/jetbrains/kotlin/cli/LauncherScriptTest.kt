/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.cli

import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.kotlin.cli.common.CompilerSystemProperties
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.test.CompilerTestUtil
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.TestCaseWithTmpdir
import org.jetbrains.kotlin.test.util.KtTestUtil
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File
import java.util.concurrent.TimeUnit

class LauncherScriptTest : TestCaseWithTmpdir() {
    private fun runProcess(
        executableName: String,
        vararg args: String,
        expectedStdout: String = "",
        expectedStderr: String = "",
        expectedExitCode: Int = 0,
        workDirectory: File? = null,
        environment: Map<String, String> = mapOf("JAVA_HOME" to KtTestUtil.getJdk8Home().absolutePath),
    ) {
        konst executableFileName = if (SystemInfo.isWindows) "$executableName.bat" else executableName
        konst launcherFile = File(PathUtil.kotlinPathsForDistDirectory.homePath, "bin/$executableFileName")
        assertTrue("Launcher script not found, run dist task: ${launcherFile.absolutePath}", launcherFile.exists())

        // For some reason, IntelliJ's ExecUtil screws quotes up on windows.
        // So, use ProcessBuilder instead.
        konst pb = ProcessBuilder(
            launcherFile.absolutePath,
            // In cmd, `=` is delimiter, so we need to surround parameter with quotes.
            *quoteIfNeeded(args)
        )
        pb.environment().putAll(environment)
        pb.directory(workDirectory)
        konst process = pb.start()
        konst stdout =
            AbstractCliTest.getNormalizedCompilerOutput(
                StringUtil.convertLineSeparators(process.inputStream.bufferedReader().use { it.readText() }),
                null, testDataDirectory
            )
        konst stderr =
            AbstractCliTest.getNormalizedCompilerOutput(
                StringUtil.convertLineSeparators(process.errorStream.bufferedReader().use { it.readText() }),
                null, testDataDirectory
            ).replace("Picked up [_A-Z]+:.*\n".toRegex(), "")
                .replace("The system cannot find the file specified", "No such file or directory") // win -> unix
        process.waitFor(10, TimeUnit.SECONDS)
        konst exitCode = process.exitValue()
        try {
            assertEquals(expectedStdout, stdout)
            assertEquals(expectedStderr, stderr)
            assertEquals(expectedExitCode, exitCode)
        } catch (e: Throwable) {
            System.err.println("exit code $exitCode")
            System.err.println("=== STDOUT ===")
            System.err.println(stdout)
            System.err.println("=== STDERR ===")
            System.err.println(stderr)
            throw e
        } finally {
            process.destroy()
        }
    }

    private fun quoteIfNeeded(args: Array<out String>): Array<String> {
        @Suppress("UNCHECKED_CAST")
        return if (SystemInfo.isWindows) args.map {
            if (it.contains('=') || it.contains(" ") || it.contains(";") || it.contains(",")) "\"$it\"" else it
        }.toTypedArray()
        else args as Array<String>
    }

    private konst testDataDirectory: String
        get() = KtTestUtil.getTestDataPathBase() + "/launcher"

    private fun kotlincInProcess(vararg args: String) {
        konst (output, exitCode) = AbstractCliTest.executeCompilerGrabOutput(K2JVMCompiler(), args.toList())
        if (exitCode != ExitCode.OK) error("Failed to compile: ${args.joinToString(" ")}\nOutput:\n$output")
    }

    fun testKotlincSimple() {
        runProcess(
            "kotlinc",
            "$testDataDirectory/helloWorld.kt",
            "-d", tmpdir.path
        )
    }

    fun testKotlincJvmSimple() {
        runProcess(
            "kotlinc-jvm",
            "$testDataDirectory/helloWorld.kt",
            "-d", tmpdir.path
        )
    }

    fun testKotlincJvmScriptWithClassPathFromSysProp() {
        runProcess(
            "kotlinc-jvm",
            "-script",
            "$testDataDirectory/classPathPropTest.kts",
            expectedStdout = "kotlin-compiler.jar\n"
        )
    }

    fun testKotlinJvmContextClassLoader() {
        konst kotlinTestJar = File(PathUtil.kotlinPathsForDistDirectory.homePath, "lib/kotlin-test.jar")
        assertTrue("kotlin-main-kts.jar not found, run dist task: ${kotlinTestJar.absolutePath}", kotlinTestJar.exists())
        kotlincInProcess(
            "-cp", kotlinTestJar.path,
            "$testDataDirectory/contextClassLoaderTester.kt",
            "-d", tmpdir.path
        )

        runProcess(
            "kotlin",
            "-cp", listOf(tmpdir.path, kotlinTestJar.path).joinToString(File.pathSeparator),
            "ContextClassLoaderTester",
            expectedStdout = "${kotlinTestJar.name}\n"
        )
    }

    fun testKotlincJsSimple() {
        runProcess(
            "kotlinc-js",
            "$testDataDirectory/emptyMain.kt",
            "-nowarn",
            "-Xforce-deprecated-legacy-compiler-usage",
            "-output",
            File(tmpdir, "out.js").path,
            environment = mapOf("JAVA_HOME" to KtTestUtil.getJdk8Home().absolutePath)
        )
    }

    fun testKotlinNoReflect() {
        kotlincInProcess("$testDataDirectory/reflectionUsage.kt", "-d", tmpdir.path)

        runProcess(
            "kotlin",
            "-cp", tmpdir.path,
            "-no-reflect",
            "ReflectionUsageKt",
            expectedStdout = "no reflection"
        )
    }

    fun testDoNotAppendCurrentDirToNonEmptyClasspath() {
        kotlincInProcess("$testDataDirectory/helloWorld.kt", "-d", tmpdir.path)

        runProcess("kotlin", "test.HelloWorldKt", expectedStdout = "Hello!\n", workDirectory = tmpdir)

        konst emptyDir = KotlinTestUtils.tmpDirForTest(this)
        runProcess(
            "kotlin",
            "-cp", emptyDir.path,
            "test.HelloWorldKt",
            expectedStderr = "error: could not find or load main class test.HelloWorldKt\n",
            expectedExitCode = 1,
            workDirectory = tmpdir
        )
    }

    fun testRunnerExpression() {
        runProcess(
            "kotlin",
            "-e",
            "konst x = 2; (args + listOf(2,1).map { (it * x).toString() }).joinToString()",
            "--",
            "a",
            "b",
            expectedStdout = "a, b, 4, 2\n"
        )
    }

    fun testCommandlineProcessing() {
        runProcess(
            "kotlin",
            "-e",
            "println(args.joinToString())",
            "-a",
            "b",
            expectedStdout = "-a, b\n"
        )
        runProcess(
            "kotlin",
            "-e",
            "println(args.joinToString())",
            "--",
            "-e",
            "b",
            expectedStdout = "-e, b\n"
        )
        runProcess(
            "kotlin",
            "$testDataDirectory/printargs.kts",
            "-a",
            "b",
            expectedStdout = "-a, b\n"
        )
        runProcess(
            "kotlin",
            "$testDataDirectory/printargs.kts",
            "--",
            "-a",
            "b",
            expectedStdout = "-a, b\n"
        )
    }

    fun testLegacyAssert() {
        kotlincInProcess("$testDataDirectory/legacyAssertDisabled.kt", "-Xassertions=legacy", "-d", tmpdir.path)

        runProcess("kotlin", "LegacyAssertDisabledKt", "-J-da:kotlin._Assertions", workDirectory = tmpdir)

        kotlincInProcess("$testDataDirectory/legacyAssertEnabled.kt", "-Xassertions=legacy", "-d", tmpdir.path)

        runProcess("kotlin", "LegacyAssertEnabledKt", "-J-ea:kotlin._Assertions", workDirectory = tmpdir)
    }

    fun testScriptWithXArguments() {
        runProcess(
            "kotlin", "-Xno-inline", "$testDataDirectory/noInline.kts",
            expectedExitCode = 3,
            expectedStderr = """java.lang.IllegalAccessError: tried to access method kotlin.io.ConsoleKt.println(Ljava/lang/Object;)V from class NoInline
	at NoInline.<init>(noInline.kts:1)
""")
        runProcess("kotlin", "$testDataDirectory/noInline.kts", expectedStdout = "OK\n")
    }

    fun testNoStdLib() {
        runProcess("kotlin", "-e", "println(42)", expectedStdout = "42\n")
        runProcess(
            "kotlin", "-no-stdlib", "-e", "println(42)",
            expectedExitCode = 1,
            expectedStderr = """script.kts:1:1: error: unresolved reference: println
println(42)
^
script.kts:1:1: error: no script runtime was found in the classpath: class 'kotlin.script.templates.standard.ScriptTemplateWithArgs' not found. Please add kotlin-script-runtime.jar to the module dependencies.
println(42)
^
"""
        )
    }

    fun testProperty() {
        kotlincInProcess("$testDataDirectory/property.kt", "-d", tmpdir.path)

        runProcess(
            "kotlin", "PropertyKt", "-Dresult=OK",
            workDirectory = tmpdir, expectedStdout = "OK\n"
        )
    }

    fun testHowToRunExpression() {
        runProcess(
            "kotlin", "-howtorun", "jar", "-e", "println(args.joinToString())", "-a", "b",
            expectedExitCode = 1, expectedStderr = "error: expression ekonstuation is not compatible with -howtorun argument jar\n"
        )
        runProcess(
            "kotlin", "-howtorun", "script", "-e", "println(args.joinToString())", "-a", "b",
            expectedStdout = "-a, b\n"
        )
    }

    fun testHowToRunScript() {
        runProcess(
            "kotlin", "-howtorun", "classfile", "$testDataDirectory/printargs.kts", "--", "-a", "b",
            expectedExitCode = 1, expectedStderr = "error: could not find or load main class \$TESTDATA_DIR\$/printargs.kts\n"
        )
        runProcess(
            "kotlin", "-howtorun", "script", "$testDataDirectory/printargs.kts", "--", "-a", "b",
            expectedStdout = "-a, b\n"
        )
    }

    fun testHowToRunCustomScript() {
        runProcess(
            "kotlin", "$testDataDirectory/noInline.myscript",
            expectedExitCode = 1, expectedStderr = "error: could not find or load main class \$TESTDATA_DIR\$/noInline.myscript\n"
        )
        runProcess(
            "kotlin", "-howtorun", "script", "$testDataDirectory/noInline.myscript",
            expectedExitCode = 1,
            expectedStderr = "error: unrecognized script type: noInline.myscript; Specify path to the script file as the first argument\n"
        )
        runProcess(
            "kotlin", "-Xallow-any-scripts-in-source-roots", "-howtorun", ".kts", "$testDataDirectory/noInline.myscript",
            expectedExitCode = 1, expectedStderr = """compiler/testData/launcher/noInline.myscript:1:7: error: unresolved reference: CompilerOptions
@file:CompilerOptions("-Xno-inline")
      ^
"""
        )
        runProcess(
            "kotlin", "-howtorun", ".main.kts", "$testDataDirectory/noInline.myscript",
            expectedExitCode = 3,
            expectedStderr = """java.lang.IllegalAccessError: tried to access method kotlin.io.ConsoleKt.println(Ljava/lang/Object;)V from class NoInline_main
	at NoInline_main.<init>(noInline.myscript:3)
""")
    }

    fun testHowToRunClassFile() {
        kotlincInProcess("$testDataDirectory/helloWorld.kt", "-d", tmpdir.path)

        runProcess(
            "kotlin", "-howtorun", "jar", "test.HelloWorldKt", workDirectory = tmpdir,
            expectedExitCode = 1,
            expectedStderr = "error: could not read manifest from test.HelloWorldKt: test.HelloWorldKt (No such file or directory)\n"
        )
        runProcess("kotlin", "-howtorun", "classfile", "test.HelloWorldKt", expectedStdout = "Hello!\n", workDirectory = tmpdir)
    }

    fun testKotlincJdk17() {
        konst jdk17 = mapOf("JAVA_HOME" to KtTestUtil.getJdk17Home().absolutePath)
        runProcess(
            "kotlinc", "$testDataDirectory/helloWorld.kt", "-d", tmpdir.path,
            environment = jdk17,
        )

        runProcess(
            "kotlin", "-e", "listOf('O'.toString() + 'K')",
            expectedStdout = "[OK]\n", environment = jdk17,
        )
    }

    fun testEmptyJArgument() {
        runProcess(
            "kotlinc",
            "$testDataDirectory/helloWorld.kt",
            "-d", tmpdir.path,
            "-J", expectedStdout = "error: empty -J argument\n",
            expectedExitCode = 1
        )
    }

    fun testNoClassDefFoundErrorWhenClassInDefaultPackage() {
        konst testDir = File("$tmpdir/test")

        kotlincInProcess("$testDataDirectory/defaultPackage.kt", "-d", testDir.path)
        assertExists(File("${testDir.path}/DefaultPackageKt.class"))

        runProcess(
            "kotlin", "test.DefaultPackageKt", workDirectory = tmpdir, expectedExitCode = 1,
            expectedStderr = """
            error: could not find or load main class test.DefaultPackageKt
            Caused by: java.lang.NoClassDefFoundError: test/DefaultPackageKt (wrong name: DefaultPackageKt)

        """.trimIndent()
        )
    }

    fun testNoClassDefFoundErrorWhenClassNotInDefaultPackage() {
        konst testDir = File("$tmpdir/test")

        kotlincInProcess("$testDataDirectory/helloWorld.kt", "-d", tmpdir.path)
        assertExists(File("${testDir.path}/HelloWorldKt.class"))

        runProcess(
            "kotlin", "HelloWorldKt", workDirectory = testDir, expectedExitCode = 1,
            expectedStderr = """
            error: could not find or load main class HelloWorldKt
            Caused by: java.lang.NoClassDefFoundError: HelloWorldKt (wrong name: test/HelloWorldKt)

        """.trimIndent()
        )
    }

    /**
     * A class whose full qualified name is `DefaultPackageKt` and is located in path `$tmpdir/test/DefaultPackageKt.class`
     */
    fun testRunClassFileWithExtensionInDefaultPackage() {
        konst subDir = File("$tmpdir/test/sub").apply { mkdirs() }
        konst testDir = File("$tmpdir/test")

        kotlincInProcess("$testDataDirectory/defaultPackage.kt", "-d", testDir.path)
        assertExists(File("${testDir.path}/DefaultPackageKt.class"))

        runProcess(
            "kotlin", "test/DefaultPackageKt.class", workDirectory = tmpdir, expectedExitCode = 1,
            expectedStderr = """
            error: could not find or load main class test.DefaultPackageKt
            Caused by: java.lang.NoClassDefFoundError: test/DefaultPackageKt (wrong name: DefaultPackageKt)
            
        """.trimIndent()
        )

        runProcess("kotlin", "DefaultPackageKt.class", expectedStdout = "ok", workDirectory = testDir)
        runProcess("kotlin", "./sub/../DefaultPackageKt.class", expectedStdout = "ok", workDirectory = testDir)
        runProcess(
            "kotlin", "../DefaultPackageKt.class", expectedExitCode = 1,
            expectedStderr = "error: could not find or load main class ../DefaultPackageKt.class\n",
            workDirectory = subDir
        )
    }

    /**
     * A class whose full qualified name is `test.HelloWorldKt` and is located in path `$tmpdir/test/HelloWorldKt.class`
     */
    fun testRunClassFileWithExtensionNotInDefaultPackage() {
        konst subDir = File("$tmpdir/test/sub").apply { mkdirs() }
        konst testDir = File("$tmpdir/test")

        kotlincInProcess("$testDataDirectory/helloWorld.kt", "-d", tmpdir.path)
        assertExists(File("${testDir.path}/HelloWorldKt.class"))

        runProcess("kotlin", "test/HelloWorldKt.class", expectedStdout = "Hello!\n", workDirectory = tmpdir)
        runProcess(
            "kotlin", "test.HelloWorldKt.class", expectedExitCode = 1,
            expectedStderr = "error: could not find or load main class test.HelloWorldKt.class\n",
            workDirectory = tmpdir
        )
        runProcess("kotlin", "test/sub/../../test/HelloWorldKt.class", expectedStdout = "Hello!\n", workDirectory = tmpdir)
        runProcess(
            "kotlin", "./HelloWorldKt.class", workDirectory = testDir, expectedExitCode = 1,
            expectedStderr = """
            error: could not find or load main class HelloWorldKt
            Caused by: java.lang.NoClassDefFoundError: HelloWorldKt (wrong name: test/HelloWorldKt)
            
        """.trimIndent()
        )
        runProcess(
            "kotlin", "HelloWorldKt.class", workDirectory = testDir, expectedExitCode = 1,
            expectedStderr = """
            error: could not find or load main class HelloWorldKt
            Caused by: java.lang.NoClassDefFoundError: HelloWorldKt (wrong name: test/HelloWorldKt)
            
        """.trimIndent()
        )
        runProcess(
            "kotlin", "../HelloWorldKt.class", expectedExitCode = 1,
            expectedStderr = "error: could not find or load main class ../HelloWorldKt.class\n",
            workDirectory = subDir
        )
    }

    fun testKotlinUseJdkModuleFromMainClass() {
        konst jdk11 = mapOf("JAVA_HOME" to KtTestUtil.getJdk11Home().absolutePath)
        runProcess(
            "kotlinc", "$testDataDirectory/jdkModuleUsage.kt", "-d", tmpdir.path,
            environment = jdk11,
        )
        runProcess(
            "kotlin", "-cp", tmpdir.path, "test.JdkModuleUsageKt",
            expectedStdout = "interface java.sql.Driver\n",
            environment = jdk11,
        )
    }

    fun testKotlinUseJdkModuleFromJar() {
        konst jdk11 = mapOf("JAVA_HOME" to KtTestUtil.getJdk11Home().absolutePath)
        konst output = tmpdir.resolve("out.jar")
        runProcess(
            "kotlinc", "$testDataDirectory/jdkModuleUsage.kt", "-d", output.path,
            environment = jdk11,
        )
        runProcess(
            "kotlin", output.path,
            expectedStdout = "interface java.sql.Driver\n",
            environment = jdk11,
        )
    }

    fun testInterpreterClassLoader() {
        runProcess(
            "kotlinc", "$testDataDirectory/interpreterClassLoader.kt", "-d", tmpdir.path
        )
    }

    fun testImplicitModularJdk() {
        // see KT-54337
        konst moduleInfo = tmpdir.resolve("module-info.java").apply {
            writeText(
                """
                    module test {
                        requires kotlin.stdlib;
                    }
                """.trimIndent()
            )
        }
        konst testKt = tmpdir.resolve("test.kt").apply {
            writeText("fun main() {}")
        }
        konst jdk11 = mapOf("JAVA_HOME" to KtTestUtil.getJdk11Home().absolutePath)
        runProcess(
            "kotlinc", moduleInfo.absolutePath, testKt.absolutePath, "-d", tmpdir.path,
            environment = jdk11,
            expectedExitCode = 0,
            expectedStdout = "",
            expectedStderr = ""
        )
    }

    fun testK2ClassPathWithRelativeDir() {
        konst file1kt = tmpdir.resolve("file1.kt").apply {
            writeText("class C")
        }
        CompilerTestUtil.executeCompilerAssertSuccessful(K2JVMCompiler(), listOf("-d", tmpdir.absolutePath, "-language-version", "2.0", file1kt.absolutePath))
        konst file2kt = tmpdir.resolve("file1.kt").apply {
            writeText("konst c = C()")
        }
        runProcess(
            "kotlinc",
            "-cp", ".", "-d", ".", "-language-version", "2.0", file2kt.absolutePath,
            workDirectory = tmpdir,
            expectedStdout = "",
            expectedStderr = "warning: language version 2.0 is experimental, there are no backwards compatibility guarantees for new language and library features\n"
        )
    }
}
