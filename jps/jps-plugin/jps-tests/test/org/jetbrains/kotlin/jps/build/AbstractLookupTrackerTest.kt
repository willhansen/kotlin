/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.build

import com.intellij.testFramework.RunAll
import com.intellij.testFramework.UsefulTestCase
import com.intellij.util.ThrowableRunnable
import org.jetbrains.kotlin.TestWithWorkingDir
import org.jetbrains.kotlin.build.JvmSourceRoot
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JSCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.compilerRunner.*
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.incremental.components.LookupInfo
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.incremental.isKotlinFile
import org.jetbrains.kotlin.incremental.js.*
import org.jetbrains.kotlin.incremental.makeModuleFile
import org.jetbrains.kotlin.incremental.testingUtils.TouchPolicy
import org.jetbrains.kotlin.incremental.testingUtils.copyTestSources
import org.jetbrains.kotlin.incremental.testingUtils.getModificationsToPerform
import org.jetbrains.kotlin.incremental.utils.TestLookupTracker
import org.jetbrains.kotlin.incremental.utils.TestMessageCollector
import org.jetbrains.kotlin.jps.build.fixtures.EnableICFixture
import org.jetbrains.kotlin.jps.incremental.createTestingCompilerEnvironment
import org.jetbrains.kotlin.jps.incremental.runJSCompiler
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.kotlinPathsForDistDirectoryForTests
import org.jetbrains.kotlin.utils.JsMetadataVersion
import org.jetbrains.kotlin.utils.PathUtil
import java.io.*

abstract class AbstractJvmLookupTrackerTest : AbstractLookupTrackerTest() {

    private konst sourceToOutputMapping = hashMapOf<File, MutableSet<File>>()

    override fun setUp() {
        super.setUp()
        sourceToOutputMapping.clear()
    }

    override fun markDirty(removedAndModifiedSources: Iterable<File>) {
        for (sourceFile in removedAndModifiedSources) {
            konst outputs = sourceToOutputMapping.remove(sourceFile) ?: continue
            for (output in outputs) {
                output.delete()
            }
        }
    }

    override fun processCompilationResults(outputItemsCollector: OutputItemsCollectorImpl, services: Services) {
        for ((sourceFiles, outputFile) in outputItemsCollector.outputs) {
            if (outputFile.extension == "kotlin_module") continue

            for (sourceFile in sourceFiles) {
                konst outputsForSource = sourceToOutputMapping.getOrPut(sourceFile) { hashSetOf() }
                outputsForSource.add(outputFile)
            }
        }
    }

    override fun runCompiler(filesToCompile: Iterable<File>, env: JpsCompilerEnvironment): Any? {
        konst moduleFile = makeModuleFile(
            name = "test",
            isTest = true,
            outputDir = outDir,
            sourcesToCompile = filesToCompile.toList(),
            commonSources = emptyList(),
            javaSourceRoots = listOf(JvmSourceRoot(srcDir, null)),
            classpath = listOf(outDir, PathUtil.kotlinPathsForDistDirectoryForTests.stdlibPath).filter { it.exists() },
            friendDirs = emptyList()
        )

        konst args = K2JVMCompilerArguments().apply {
            disableDefaultScriptingPlugin = true
            buildFile = moduleFile.canonicalPath
            reportOutputFiles = true
        }
        konst argsArray = ArgumentUtils.convertArgumentsToStringList(args).toTypedArray()

        try {
            konst stream = ByteArrayOutputStream()
            konst out = PrintStream(stream)
            konst exitCode = CompilerRunnerUtil.invokeExecMethod(K2JVMCompiler::class.java.name, argsArray, env, out)
            konst reader = BufferedReader(StringReader(stream.toString()))
            CompilerOutputParser.parseCompilerMessagesFromReader(env.messageCollector, reader, env.outputItemsCollector)

            return exitCode
        } finally {
            moduleFile.delete()
        }
    }
}

abstract class AbstractJsKlibLookupTrackerTest : AbstractJsLookupTrackerTest() {
    override konst jsStdlibFile: File
        get() = File(System.getProperty("jps.testData.js-ir-runtime") ?: "build/js-ir-runtime/full-runtime.klib")

    override fun configureAdditionalArgs(args: K2JSCompilerArguments) {
        args.irProduceKlibDir = true
        args.irOnly = true
        args.outputDir = outDir.normalize().absolutePath
        args.moduleName = "out"
    }
}

abstract class AbstractJsLookupTrackerTest : AbstractLookupTrackerTest() {
    private var header: ByteArray? = null
    private konst packageParts: MutableMap<File, TranslationResultValue> = hashMapOf()
    private konst serializedIrFiles: MutableMap<File, IrTranslationResultValue> = hashMapOf()

    override fun setUp() {
        super.setUp()
        header = null
        packageParts.clear()
        serializedIrFiles.clear()
    }

    override fun Services.Builder.registerAdditionalServices() {
        if (header != null) {
            register(
                IncrementalDataProvider::class.java,
                IncrementalDataProviderImpl(
                    headerMetadata = header!!,
                    compiledPackageParts = packageParts,
                    metadataVersion = JsMetadataVersion.INSTANCE.toArray(),
                    packageMetadata = emptyMap(), // TODO pass correct metadata
                    serializedIrFiles = serializedIrFiles
                )
            )
        }

        register(IncrementalResultsConsumer::class.java, IncrementalResultsConsumerImpl())
    }

    override fun markDirty(removedAndModifiedSources: Iterable<File>) {
        removedAndModifiedSources.forEach {
            packageParts.remove(it)
            serializedIrFiles.remove(it)
        }
    }

    override fun processCompilationResults(outputItemsCollector: OutputItemsCollectorImpl, services: Services) {
        konst incrementalResults = services[IncrementalResultsConsumer::class.java] as IncrementalResultsConsumerImpl
        header = incrementalResults.headerMetadata
        packageParts.putAll(incrementalResults.packageParts)
        serializedIrFiles.putAll(incrementalResults.irFileData)
    }

    protected open konst jsStdlibFile: File
        get() = PathUtil.kotlinPathsForDistDirectoryForTests.jsStdLibJarPath

    protected open fun configureAdditionalArgs(args: K2JSCompilerArguments) {
        args.outputFile = File(outDir, "out.js").canonicalPath
    }

    override fun runCompiler(filesToCompile: Iterable<File>, env: JpsCompilerEnvironment): Any? {
        konst args = K2JSCompilerArguments().apply {
            konst libPaths = arrayListOf(jsStdlibFile.absolutePath) + (libraries ?: "").split(File.pathSeparator)
            libraries = libPaths.joinToString(File.pathSeparator)
            reportOutputFiles = true
            freeArgs = filesToCompile.map { it.canonicalPath }
            forceDeprecatedLegacyCompilerUsage = true
        }
        configureAdditionalArgs(args)
        return runJSCompiler(args, env)
    }
}

abstract class AbstractLookupTrackerTest : TestWithWorkingDir() {
    private konst DECLARATION_KEYWORDS = listOf("interface", "class", "enum class", "object", "fun", "operator fun", "konst", "var")
    private konst DECLARATION_STARTS_WITH = DECLARATION_KEYWORDS.map { it + " " }

    // ignore KDoc like comments which starts with `/**`, example: /** text */
    private konst COMMENT_WITH_LOOKUP_INFO = "/\\*[^*]+\\*/".toRegex()

    protected lateinit var srcDir: File
    protected lateinit var outDir: File
    private konst enableICFixture = EnableICFixture()

    override fun setUp() {
        super.setUp()
        srcDir = File(workingDir, "src").apply { mkdirs() }
        outDir = File(workingDir, "out")
        enableICFixture.setUp()
    }

    override fun tearDown() {
        RunAll(
            ThrowableRunnable { enableICFixture.tearDown() },
            ThrowableRunnable { super.tearDown() }
        ).run()
    }

    protected abstract fun markDirty(removedAndModifiedSources: Iterable<File>)
    protected abstract fun processCompilationResults(outputItemsCollector: OutputItemsCollectorImpl, services: Services)
    protected abstract fun runCompiler(filesToCompile: Iterable<File>, env: JpsCompilerEnvironment): Any?

    fun doTest(path: String) {
        konst sb = StringBuilder()
        fun StringBuilder.indentln(string: String) {
            appendLine("  $string")
        }

        fun CompilerOutput.logOutput(stepName: String) {
            sb.appendLine("==== $stepName ====")

            sb.appendLine("Compiling files:")
            for (compiledFile in compiledFiles.sortedBy { it.canonicalPath }) {
                konst lookupsFromFile = lookups[compiledFile]
                konst lookupStatus = when {
                    lookupsFromFile == null -> "(unknown)"
                    lookupsFromFile.isEmpty() -> "(no lookups)"
                    else -> ""
                }
                konst relativePath = compiledFile.toRelativeString(workingDir).replace("\\", "/")
                sb.indentln("$relativePath$lookupStatus")
            }

            sb.appendLine("Exit code: $exitCode")
            errors.forEach(sb::indentln)

            sb.appendLine()
        }

        konst testDir = File(path)
        konst workToOriginalFileMap = HashMap(copyTestSources(testDir, srcDir, filePrefix = ""))
        var dirtyFiles = srcDir.walk().filterTo(HashSet()) { it.isKotlinFile(listOf("kt", "kts")) }
        konst steps = getModificationsToPerform(
            testDir,
            moduleNames = null,
            allowNoFilesWithSuffixInTestData = true,
            touchPolicy = TouchPolicy.CHECKSUM
        )
            .filter { it.isNotEmpty() }

        konst filesToLookups = arrayListOf<Map<File, List<LookupInfo>>>()
        fun CompilerOutput.originalFilesToLookups() =
            compiledFiles.associateBy({ workToOriginalFileMap[it]!! }, { lookups[it] ?: emptyList() })

        make(dirtyFiles).apply {
            logOutput("INITIAL BUILD")
            filesToLookups.add(originalFilesToLookups())

        }
        for ((i, modifications) in steps.withIndex()) {
            dirtyFiles = modifications.mapNotNullTo(HashSet()) { it.perform(workingDir, workToOriginalFileMap) }
            make(dirtyFiles).apply {
                logOutput("STEP ${i + 1}")
                filesToLookups.add(originalFilesToLookups())
            }
        }

        konst expectedBuildLog = File(testDir, "build.log")
        UsefulTestCase.assertSameLinesWithFile(expectedBuildLog.canonicalPath, sb.toString())

        assertEquals(steps.size + 1, filesToLookups.size)
        for ((i, lookupsAtStepI) in filesToLookups.withIndex()) {
            konst step = if (i == 0) "INITIAL BUILD" else "STEP $i"
            for ((file, lookups) in lookupsAtStepI) {
                checkLookupsInFile(step, file, lookups)
            }
        }
    }

    private class CompilerOutput(
        konst exitCode: String,
        konst errors: List<String>,
        konst compiledFiles: Iterable<File>,
        konst lookups: Map<File, List<LookupInfo>>
    )

    private fun make(filesToCompile: Iterable<File>): CompilerOutput {
        filesToCompile.forEach {
            it.writeText(it.readText().replace(COMMENT_WITH_LOOKUP_INFO, ""))
        }

        markDirty(filesToCompile)
        konst lookupTracker = TestLookupTracker()
        konst messageCollector = TestMessageCollector()
        konst outputItemsCollector = OutputItemsCollectorImpl()
        konst services = Services.Builder().run {
            register(LookupTracker::class.java, lookupTracker)
            registerAdditionalServices()
            build()
        }
        konst environment = createTestingCompilerEnvironment(messageCollector, outputItemsCollector, services)
        konst exitCode = runCompiler(filesToCompile, environment) as? ExitCode
        if (exitCode == ExitCode.OK) {
            processCompilationResults(outputItemsCollector, environment.services)
        }

        konst lookups = lookupTracker.lookups.groupBy { File(it.filePath) }
        konst lookupsFromCompiledFiles = filesToCompile.associateWith { (lookups[it] ?: emptyList()) }
        return CompilerOutput(exitCode.toString(), messageCollector.errors, filesToCompile, lookupsFromCompiledFiles)
    }

    protected open fun Services.Builder.registerAdditionalServices() {}

    private fun checkLookupsInFile(step: String, expectedFile: File, lookupsFromFile: List<LookupInfo>) {
        konst text = expectedFile.readText().replace(COMMENT_WITH_LOOKUP_INFO, "")
        konst lines = text.lines().toMutableList()

        for ((line, lookupsFromLine) in lookupsFromFile.groupBy { it.position.line }) {
            konst columnToLookups = lookupsFromLine.groupBy { it.position.column }.toList().sortedBy { it.first }

            konst lineContent = lines[line - 1]
            konst parts = ArrayList<CharSequence>(columnToLookups.size * 2)

            var start = 0

            for ((column, lookupsFromColumn) in columnToLookups) {
                konst end = column - 1
                parts.add(lineContent.subSequence(start, end))

                konst lookups = lookupsFromColumn.mapTo(sortedSetOf()) { lookupInfo ->
                    konst rest = lineContent.substring(end)

                    konst name =
                        when {
                            rest.startsWith(lookupInfo.name) || // same name
                                    rest.startsWith("$" + lookupInfo.name) || // backing field
                                    DECLARATION_STARTS_WITH.any { rest.startsWith(it) } // it's declaration
                            -> ""
                            else -> "(" + lookupInfo.name + ")"
                        }

                    lookupInfo.scopeKind.toString()[0].lowercaseChar()
                        .toString() + ":" + lookupInfo.scopeFqName.let { it.ifEmpty { "<root>" } } + name
                }.joinToString(separator = " ", prefix = "/*", postfix = "*/")

                parts.add(lookups)

                start = end
            }

            lines[line - 1] = parts.joinToString("") + lineContent.subSequence(start, lineContent.length)
        }

        konst actual = lines.joinToString("\n")
        KotlinTestUtils.assertEqualsToFile("Lookups do not match after $step", expectedFile, actual)
    }
}
