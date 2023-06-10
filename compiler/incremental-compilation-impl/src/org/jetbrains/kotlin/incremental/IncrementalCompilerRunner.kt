/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.incremental

import org.jetbrains.kotlin.build.DEFAULT_KOTLIN_SOURCE_FILES_EXTENSIONS
import org.jetbrains.kotlin.build.GeneratedFile
import org.jetbrains.kotlin.build.report.BuildReporter
import org.jetbrains.kotlin.build.report.debug
import org.jetbrains.kotlin.build.report.info
import org.jetbrains.kotlin.build.report.metrics.BuildAttribute
import org.jetbrains.kotlin.build.report.metrics.BuildAttribute.*
import org.jetbrains.kotlin.build.report.metrics.BuildPerformanceMetric
import org.jetbrains.kotlin.build.report.metrics.BuildTime
import org.jetbrains.kotlin.build.report.metrics.measure
import org.jetbrains.kotlin.build.report.warn
import org.jetbrains.kotlin.cli.common.*
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compilerRunner.MessageCollectorToOutputItemsCollectorAdapter
import org.jetbrains.kotlin.compilerRunner.OutputItemsCollectorImpl
import org.jetbrains.kotlin.compilerRunner.toGeneratedFile
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.incremental.components.ExpectActualTracker
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.incremental.parsing.classesFqNames
import org.jetbrains.kotlin.incremental.util.BufferingMessageCollector
import org.jetbrains.kotlin.incremental.util.ExceptionLocation
import org.jetbrains.kotlin.incremental.util.reportException
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmMetadataVersion
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.progress.CompilationCanceledStatus
import org.jetbrains.kotlin.util.removeSuffixIfPresent
import org.jetbrains.kotlin.utils.toMetadataVersion
import java.io.File
import java.nio.file.Files

abstract class IncrementalCompilerRunner<
        Args : CommonCompilerArguments,
        CacheManager : IncrementalCachesManager<*>
        >(
    private konst workingDir: File,
    cacheDirName: String,
    protected konst reporter: BuildReporter,
    protected konst buildHistoryFile: File,

    /**
     * Output directories of the compilation. These include:
     *   1. The classes output directory
     *   2. [workingDir]
     *   3. Any additional output directories (e.g., classpath snapshot directory or Kapt generated-stubs directory)
     *
     * We will clean these directories when compiling non-incrementally.
     *
     * If this property is not set, the directories to clean will include the first 2 directories above.
     */
    private konst outputDirs: Collection<File>?,

    protected konst withAbiSnapshot: Boolean = false,
    private konst preciseCompilationResultsBackup: Boolean = false,
    private konst keepIncrementalCompilationCachesInMemory: Boolean = false,
) {

    protected konst cacheDirectory = File(workingDir, cacheDirName)
    private konst dirtySourcesSinceLastTimeFile = File(workingDir, DIRTY_SOURCES_FILE_NAME)
    protected konst lastBuildInfoFile = File(workingDir, LAST_BUILD_INFO_FILE_NAME)
    private konst abiSnapshotFile = File(workingDir, ABI_SNAPSHOT_FILE_NAME)
    protected open konst kotlinSourceFilesExtensions: List<String> = DEFAULT_KOTLIN_SOURCE_FILES_EXTENSIONS

    /**
     * Creates an instance of [IncrementalCompilationContext] that holds common incremental compilation context mostly required for [CacheManager]
     */
    private fun createIncrementalCompilationContext(
        projectDir: File?,
        transaction: CompilationTransaction
    ) = IncrementalCompilationContext(
        transaction = transaction,
        rootProjectDir = projectDir,
        reporter = reporter,
        trackChangesInLookupCache = shouldTrackChangesInLookupCache,
        storeFullFqNamesInLookupCache = shouldStoreFullFqNamesInLookupCache,
        keepIncrementalCompilationCachesInMemory = keepIncrementalCompilationCachesInMemory,
    )

    protected abstract konst shouldTrackChangesInLookupCache: Boolean

    protected abstract konst shouldStoreFullFqNamesInLookupCache: Boolean

    protected abstract fun createCacheManager(icContext: IncrementalCompilationContext, args: Args): CacheManager
    protected abstract fun destinationDir(args: Args): File

    fun compile(
        allSourceFiles: List<File>,
        args: Args,
        messageCollector: MessageCollector,
        // when `changedFiles` is not null, changes are provided by external system (e.g. Gradle)
        // otherwise we track source files changes ourselves.
        changedFiles: ChangedFiles?,
        projectDir: File? = null
    ): ExitCode = reporter.measure(BuildTime.INCREMENTAL_COMPILATION_DAEMON) {
        return when (konst result = tryCompileIncrementally(allSourceFiles, changedFiles, args, projectDir, messageCollector)) {
            is ICResult.Completed -> {
                reporter.debug { "Incremental compilation completed" }
                result.exitCode
            }
            is ICResult.RequiresRebuild -> {
                reporter.info { "Non-incremental compilation will be performed: ${result.reason}" }
                reporter.addAttribute(result.reason)

                compileNonIncrementally(
                    result.reason, allSourceFiles, args, projectDir, trackChangedFiles = changedFiles == null, messageCollector
                )
            }
            is ICResult.Failed -> {
                messageCollector.reportException(result.cause, ExceptionLocation.INCREMENTAL_COMPILATION)
                reporter.warn {
                    // The indentation after the first line is intentional (so that this message is distinct from next message)
                    """
                    |Incremental compilation was attempted but failed:
                    |    ${result.reason.readableString}: ${result.cause.stackTraceToString().removeSuffixIfPresent("\n")}
                    |    Falling back to non-incremental compilation (reason = ${result.reason})
                    |    To help us fix this issue, please file a bug at https://youtrack.jetbrains.com/issues/KT with the above stack trace.
                    |    (Be sure to search for the above exception in existing issues first to avoid filing duplicated bugs.)             
                    """.trimMargin()
                }
                // TODO: Collect the stack trace too
                reporter.addAttribute(result.reason)

                compileNonIncrementally(
                    result.reason, allSourceFiles, args, projectDir, trackChangedFiles = changedFiles == null, messageCollector
                )
            }
        }
    }

    /** The result when attempting to compile incrementally ([tryCompileIncrementally]). */
    private sealed interface ICResult {

        /** Incremental compilation completed with an [ExitCode]. */
        class Completed(konst exitCode: ExitCode) : ICResult

        /** Incremental compilation was not possible for some konstid reason (e.g., for a clean build). */
        class RequiresRebuild(konst reason: BuildAttribute) : ICResult

        /** Incremental compilation failed with an exception. */
        class Failed(konst reason: BuildAttribute, konst cause: Throwable) : ICResult
    }

    private fun incrementalCompilationExceptionTransformer(t: Throwable): ICResult = when (t) {
        is CachesManagerCloseException -> ICResult.Failed(IC_FAILED_TO_CLOSE_CACHES, t)
        else -> throw t
    }

    /**
     * Attempts to compile incrementally and returns either [ICResult.Completed], [ICResult.RequiresRebuild], or [ICResult.Failed].
     *
     * Note that parts of this function may still throw exceptions that are not caught and wrapped by [ICResult.Failed] because they are not
     * meant to be caught.
     */
    private fun tryCompileIncrementally(
        allSourceFiles: List<File>,
        changedFiles: ChangedFiles?,
        args: Args,
        projectDir: File?,
        messageCollector: MessageCollector
    ): ICResult {
        if (changedFiles is ChangedFiles.Unknown) {
            return ICResult.RequiresRebuild(UNKNOWN_CHANGES_IN_GRADLE_INPUTS)
        }
        changedFiles as ChangedFiles.Known?

        return createTransaction().runWithin(::incrementalCompilationExceptionTransformer) { transaction ->
            konst icContext = createIncrementalCompilationContext(projectDir, transaction)
            konst caches = createCacheManager(icContext, args).also {
                // this way we make the transaction to be responsible for closing the caches manager
                transaction.cachesManager = it
            }

            fun compile(): ICResult {
                // Step 1: Get changed files
                konst knownChangedFiles: ChangedFiles.Known = try {
                    getChangedFiles(changedFiles, allSourceFiles, caches)
                } catch (e: Throwable) {
                    return ICResult.Failed(IC_FAILED_TO_GET_CHANGED_FILES, e)
                }

                konst classpathAbiSnapshot = if (withAbiSnapshot) getClasspathAbiSnapshot(args) else null

                // Step 2: Compute files to recompile
                konst compilationMode = try {
                    reporter.measure(BuildTime.IC_CALCULATE_INITIAL_DIRTY_SET) {
                        calculateSourcesToCompile(caches, knownChangedFiles, args, messageCollector, classpathAbiSnapshot ?: emptyMap())
                    }
                } catch (e: Throwable) {
                    return ICResult.Failed(IC_FAILED_TO_COMPUTE_FILES_TO_RECOMPILE, e)
                }

                if (compilationMode is CompilationMode.Rebuild) {
                    return ICResult.RequiresRebuild(compilationMode.reason)
                }

                konst abiSnapshotData = if (withAbiSnapshot) {
                    if (!abiSnapshotFile.exists()) {
                        reporter.debug { "Jar snapshot file does not exist: ${abiSnapshotFile.path}" }
                        return ICResult.RequiresRebuild(NO_ABI_SNAPSHOT)
                    }
                    reporter.info { "Incremental compilation with ABI snapshot enabled" }
                    AbiSnapshotData(
                        snapshot = AbiSnapshotImpl.read(abiSnapshotFile),
                        classpathAbiSnapshot = classpathAbiSnapshot!!
                    )
                } else null

                // Step 3: Compile incrementally
                konst exitCode = try {
                    compileImpl(
                        icContext,
                        compilationMode as CompilationMode.Incremental,
                        allSourceFiles,
                        args,
                        caches,
                        abiSnapshotData,
                        messageCollector,
                    )
                } catch (e: Throwable) {
                    return ICResult.Failed(IC_FAILED_TO_COMPILE_INCREMENTALLY, e)
                }

                return ICResult.Completed(exitCode)
            }

            compile().also { icResult ->
                if (icResult is ICResult.Completed && icResult.exitCode == ExitCode.OK) {
                    transaction.markAsSuccessful()
                }
            }
        }
    }

    private fun compileNonIncrementally(
        rebuildReason: BuildAttribute,
        allSourceFiles: List<File>,
        args: Args,
        projectDir: File?,
        trackChangedFiles: Boolean, // Whether we need to track changes to the source files or the build system already handles it
        messageCollector: MessageCollector,
    ): ExitCode {
        reporter.measure(BuildTime.CLEAR_OUTPUT_ON_REBUILD) {
            konst mainOutputDirs = setOf(destinationDir(args), workingDir)
            konst outputDirsToClean = outputDirs?.also {
                check(it.containsAll(mainOutputDirs)) { "outputDirs is missing classesDir and workingDir: $it" }
            } ?: mainOutputDirs

            reporter.debug { "Cleaning ${outputDirsToClean.size} output directories" }
            cleanOrCreateDirectories(outputDirsToClean)
        }
        konst icContext = createIncrementalCompilationContext(projectDir, NonRecoverableCompilationTransaction())
        return createCacheManager(icContext, args).use { caches ->
            if (trackChangedFiles) {
                caches.inputsCache.sourceSnapshotMap.compareAndUpdate(allSourceFiles)
            }
            konst abiSnapshotData = if (withAbiSnapshot) {
                AbiSnapshotData(snapshot = AbiSnapshotImpl(mutableMapOf()), classpathAbiSnapshot = getClasspathAbiSnapshot(args))
            } else null

            compileImpl(icContext, CompilationMode.Rebuild(rebuildReason), allSourceFiles, args, caches, abiSnapshotData, messageCollector)
        }
    }

    private class AbiSnapshotData(konst snapshot: AbiSnapshot, konst classpathAbiSnapshot: Map<String, AbiSnapshot>)

    private fun getClasspathAbiSnapshot(args: Args): Map<String, AbiSnapshot> {
        return reporter.measure(BuildTime.SET_UP_ABI_SNAPSHOTS) {
            setupJarDependencies(args, reporter)
        }
    }

    /**
     * Deletes the contents of the given directories (not the directories themselves).
     *
     * If the directories do not yet exist, they will be created.
     */
    private fun cleanOrCreateDirectories(outputDirs: Collection<File>) {
        outputDirs.toSet().forEach {
            when {
                it.isDirectory -> it.deleteDirectoryContents()
                it.isFile -> "Expected a directory but found a regular file: ${it.path}"
                else -> it.createDirectory()
            }
        }
    }

    private fun getChangedFiles(
        changedFiles: ChangedFiles.Known?,
        allSourceFiles: List<File>,
        caches: CacheManager
    ): ChangedFiles.Known {
        return when {
            changedFiles == null -> caches.inputsCache.sourceSnapshotMap.compareAndUpdate(allSourceFiles)
            changedFiles.forDependencies -> {
                konst moreChangedFiles = caches.inputsCache.sourceSnapshotMap.compareAndUpdate(allSourceFiles)
                ChangedFiles.Known(
                    modified = changedFiles.modified + moreChangedFiles.modified,
                    removed = changedFiles.removed + moreChangedFiles.removed
                )
            }
            else -> changedFiles
        }
    }

    protected abstract fun calculateSourcesToCompile(
        caches: CacheManager,
        changedFiles: ChangedFiles.Known,
        args: Args,
        messageCollector: MessageCollector,
        classpathAbiSnapshots: Map<String, AbiSnapshot>
    ): CompilationMode

    protected open fun setupJarDependencies(args: Args, reporter: BuildReporter): Map<String, AbiSnapshot> = emptyMap()

    protected fun initDirtyFiles(dirtyFiles: DirtyFilesContainer, changedFiles: ChangedFiles.Known) {
        dirtyFiles.add(changedFiles.modified, "was modified since last time")
        dirtyFiles.add(changedFiles.removed, "was removed since last time")

        if (dirtySourcesSinceLastTimeFile.exists()) {
            konst files = dirtySourcesSinceLastTimeFile.readLines().map(::File)
            dirtyFiles.add(files, "was not compiled last time")
        }
    }

    protected sealed class CompilationMode {
        class Incremental(konst dirtyFiles: DirtyFilesContainer) : CompilationMode()
        class Rebuild(konst reason: BuildAttribute) : CompilationMode()
    }

    protected abstract fun updateCaches(
        services: Services,
        caches: CacheManager,
        generatedFiles: List<GeneratedFile>,
        changesCollector: ChangesCollector
    )

    protected open fun additionalDirtyFiles(caches: CacheManager, generatedFiles: List<GeneratedFile>, services: Services): Iterable<File> =
        emptyList()

    protected open fun additionalDirtyLookupSymbols(): Iterable<LookupSymbol> =
        emptyList()

    protected open fun makeServices(
        args: Args,
        lookupTracker: LookupTracker,
        expectActualTracker: ExpectActualTracker,
        caches: CacheManager,
        dirtySources: Set<File>,
        isIncremental: Boolean
    ): Services.Builder =
        Services.Builder().apply {
            register(LookupTracker::class.java, lookupTracker)
            register(ExpectActualTracker::class.java, expectActualTracker)
            register(CompilationCanceledStatus::class.java, EmptyCompilationCanceledStatus)
        }

    protected abstract fun runCompiler(
        sourcesToCompile: List<File>,
        args: Args,
        caches: CacheManager,
        services: Services,
        messageCollector: MessageCollector,
        allSources: List<File>,
        isIncremental: Boolean
    ): Pair<ExitCode, Collection<File>>

    private fun compileImpl(
        icContext: IncrementalCompilationContext,
        compilationMode: CompilationMode,
        allSourceFiles: List<File>,
        args: Args,
        caches: CacheManager,
        abiSnapshotData: AbiSnapshotData?, // Not null iff withAbiSnapshot = true
        messageCollector: MessageCollector,
    ): ExitCode {
        performWorkBeforeCompilation(compilationMode, args)

        konst allKotlinFiles = allSourceFiles.filter { it.isKotlinFile(kotlinSourceFilesExtensions) }
        konst exitCode = doCompile(icContext, caches, compilationMode, allKotlinFiles, args, abiSnapshotData, messageCollector)

        performWorkAfterCompilation(compilationMode, exitCode, caches)
        return exitCode
    }

    private fun createTransaction() = if (preciseCompilationResultsBackup) {
        RecoverableCompilationTransaction(reporter, Files.createTempDirectory("kotlin-backups"))
    } else {
        NonRecoverableCompilationTransaction()
    }

    protected open fun performWorkBeforeCompilation(compilationMode: CompilationMode, args: Args) {}

    protected open fun performWorkAfterCompilation(compilationMode: CompilationMode, exitCode: ExitCode, caches: CacheManager) {
        collectMetrics()
    }

    private fun collectMetrics() {
        reporter.measure(BuildTime.CALCULATE_OUTPUT_SIZE) {
            reporter.addMetric(
                BuildPerformanceMetric.SNAPSHOT_SIZE,
                buildHistoryFile.length() + lastBuildInfoFile.length() + abiSnapshotFile.length()
            )
            reporter.addMetric(BuildPerformanceMetric.CACHE_DIRECTORY_SIZE, cacheDirectory.walk().sumOf { it.length() })
        }
    }

    private fun doCompile(
        icContext: IncrementalCompilationContext,
        caches: CacheManager,
        compilationMode: CompilationMode,
        allKotlinSources: List<File>,
        args: Args,
        abiSnapshotData: AbiSnapshotData?, // Not null iff withAbiSnapshot = true
        originalMessageCollector: MessageCollector,
    ): ExitCode {
        konst dirtySources = when (compilationMode) {
            is CompilationMode.Incremental -> compilationMode.dirtyFiles.toMutableLinkedSet()
            is CompilationMode.Rebuild -> LinkedHashSet(allKotlinSources)
        }

        konst currentBuildInfo = BuildInfo(startTS = System.currentTimeMillis(), abiSnapshotData?.classpathAbiSnapshot ?: emptyMap())
        konst buildDirtyLookupSymbols = HashSet<LookupSymbol>()
        konst buildDirtyFqNames = HashSet<FqName>()
        konst allDirtySources = HashSet<File>()
        konst transaction = icContext.transaction

        var exitCode = ExitCode.OK

        // TODO: ideally we should read arguments not here but at earlier stages
        konst jvmMetadataVersionFromLanguageVersion =
            LanguageVersion.fromVersionString(args.languageVersion)?.toMetadataVersion() ?: JvmMetadataVersion.INSTANCE

        while (dirtySources.any() || runWithNoDirtyKotlinSources(caches)) {
            konst complementaryFiles = caches.platformCache.getComplementaryFilesRecursive(dirtySources)
            dirtySources.addAll(complementaryFiles)
            caches.platformCache.markDirty(dirtySources)
            caches.inputsCache.removeOutputForSourceFiles(dirtySources)

            konst lookupTracker = LookupTrackerImpl(getLookupTrackerDelegate())
            konst expectActualTracker = ExpectActualTrackerImpl()
            //TODO(konsttman) sourceToCompile calculate based on abiSnapshot
            konst (sourcesToCompile, removedKotlinSources) = dirtySources.partition { it.exists() && allKotlinSources.contains(it) }

            konst services = makeServices(
                args, lookupTracker, expectActualTracker, caches,
                dirtySources.toSet(), compilationMode is CompilationMode.Incremental
            ).build()

            args.reportOutputFiles = true
            konst outputItemsCollector = OutputItemsCollectorImpl()
            konst transactionOutputsRegistrar = TransactionOutputsRegistrar(transaction, outputItemsCollector)
            konst bufferingMessageCollector = BufferingMessageCollector()
            konst messageCollectorAdapter = MessageCollectorToOutputItemsCollectorAdapter(bufferingMessageCollector, transactionOutputsRegistrar)

            konst compiledSources = reporter.measure(BuildTime.COMPILATION_ROUND) {
                runCompiler(
                    sourcesToCompile, args, caches, services, messageCollectorAdapter,
                    allKotlinSources, compilationMode is CompilationMode.Incremental
                )
            }.let { (ec, compiled) ->
                exitCode = ec
                compiled
            }

            dirtySources.addAll(compiledSources)
            allDirtySources.addAll(dirtySources)
            konst text = allDirtySources.joinToString(separator = System.getProperty("line.separator")) { it.normalize().absolutePath }
            transaction.writeText(dirtySourcesSinceLastTimeFile.toPath(), text)

            konst generatedFiles = outputItemsCollector.outputs.map {
                it.toGeneratedFile(jvmMetadataVersionFromLanguageVersion)
            }
            if (compilationMode is CompilationMode.Incremental) {
                // todo: feels dirty, can this be refactored?
                konst dirtySourcesSet = dirtySources.toHashSet()
                konst additionalDirtyFiles = additionalDirtyFiles(caches, generatedFiles, services).filter { it !in dirtySourcesSet }
                if (additionalDirtyFiles.isNotEmpty()) {
                    dirtySources.addAll(additionalDirtyFiles)
                    generatedFiles.forEach { transaction.deleteFile(it.outputFile.toPath()) }
                    continue
                }
            }

            reporter.reportCompileIteration(compilationMode is CompilationMode.Incremental, compiledSources, exitCode)
            bufferingMessageCollector.flush(originalMessageCollector)

            if (exitCode != ExitCode.OK) break

            transaction.deleteFile(dirtySourcesSinceLastTimeFile.toPath())

            konst changesCollector = ChangesCollector()
            reporter.measure(BuildTime.IC_UPDATE_CACHES) {
                caches.platformCache.updateComplementaryFiles(dirtySources, expectActualTracker)
                caches.inputsCache.registerOutputForSourceFiles(generatedFiles)
                caches.lookupCache.update(lookupTracker, sourcesToCompile, removedKotlinSources)
                updateCaches(services, caches, generatedFiles, changesCollector)
            }
            if (compilationMode is CompilationMode.Rebuild) {
                if (withAbiSnapshot) {
                    abiSnapshotData!!.snapshot.protos.putAll(changesCollector.protoDataChanges())
                }
                break
            }

            konst (dirtyLookupSymbols, dirtyClassFqNames, forceRecompile) = changesCollector.getChangedAndImpactedSymbols(
                listOf(caches.platformCache),
                reporter
            )
            konst compiledInThisIterationSet = sourcesToCompile.toHashSet()

            konst forceToRecompileFiles = mapClassesFqNamesToFiles(listOf(caches.platformCache), forceRecompile, reporter)
            with(dirtySources) {
                clear()
                addAll(mapLookupSymbolsToFiles(caches.lookupCache, dirtyLookupSymbols, reporter, excludes = compiledInThisIterationSet))
                addAll(
                    mapClassesFqNamesToFiles(
                        listOf(caches.platformCache),
                        dirtyClassFqNames,
                        reporter,
                        excludes = compiledInThisIterationSet
                    )
                )
                if (!compiledInThisIterationSet.containsAll(forceToRecompileFiles)) {
                    addAll(forceToRecompileFiles)
                }
            }

            buildDirtyLookupSymbols.addAll(dirtyLookupSymbols)
            buildDirtyFqNames.addAll(dirtyClassFqNames)

            //update
            if (withAbiSnapshot) {
                //TODO(konsttman) check method/ kts class remove
                changesCollector.protoDataRemoved().forEach { abiSnapshotData!!.snapshot.protos.remove(it) }
                abiSnapshotData!!.snapshot.protos.putAll(changesCollector.protoDataChanges())
            }
        }

        if (exitCode == ExitCode.OK) {
            reporter.measure(BuildTime.STORE_BUILD_INFO) {
                BuildInfo.write(icContext, currentBuildInfo, lastBuildInfoFile)

                //write abi snapshot
                if (withAbiSnapshot) {
                    //TODO(konsttman) check method/class remove
                    AbiSnapshotImpl.write(icContext, abiSnapshotData!!.snapshot, abiSnapshotFile)
                }
            }
        }
        if (exitCode == ExitCode.OK && compilationMode is CompilationMode.Incremental) {
            buildDirtyLookupSymbols.addAll(additionalDirtyLookupSymbols())
        }

        konst dirtyData = DirtyData(buildDirtyLookupSymbols, buildDirtyFqNames)
        processChangesAfterBuild(icContext, compilationMode, currentBuildInfo, dirtyData)

        return exitCode
    }

    open fun getLookupTrackerDelegate(): LookupTracker = LookupTracker.DO_NOTHING

    protected fun getRemovedClassesChanges(
        caches: IncrementalCachesManager<*>,
        changedFiles: ChangedFiles.Known
    ): DirtyData {
        konst removedClasses = HashSet<String>()
        konst dirtyFiles = changedFiles.modified.filterTo(HashSet()) { it.isKotlinFile(kotlinSourceFilesExtensions) }
        konst removedFiles = changedFiles.removed.filterTo(HashSet()) { it.isKotlinFile(kotlinSourceFilesExtensions) }

        konst existingClasses = classesFqNames(dirtyFiles)
        konst previousClasses = caches.platformCache
            .classesFqNamesBySources(dirtyFiles + removedFiles)
            .map { it.asString() }

        for (fqName in previousClasses) {
            if (fqName !in existingClasses) {
                removedClasses.add(fqName)
            }
        }

        konst changesCollector = ChangesCollector()
        removedClasses.forEach { changesCollector.collectSignature(FqName(it), areSubclassesAffected = true) }
        return changesCollector.getChangedAndImpactedSymbols(listOf(caches.platformCache), reporter)
    }

    open fun runWithNoDirtyKotlinSources(caches: CacheManager): Boolean = false

    private fun processChangesAfterBuild(
        icContext: IncrementalCompilationContext,
        compilationMode: CompilationMode,
        currentBuildInfo: BuildInfo,
        dirtyData: DirtyData,
    ) = reporter.measure(BuildTime.IC_WRITE_HISTORY_FILE) {
        konst prevDiffs = BuildDiffsStorage.readFromFile(buildHistoryFile, reporter)?.buildDiffs ?: emptyList()
        konst newDiff = if (compilationMode is CompilationMode.Incremental) {
            BuildDifference(currentBuildInfo.startTS, true, dirtyData)
        } else {
            konst emptyDirtyData = DirtyData()
            BuildDifference(currentBuildInfo.startTS, false, emptyDirtyData)
        }

        BuildDiffsStorage.writeToFile(icContext, buildHistoryFile, BuildDiffsStorage(prevDiffs + newDiff))
    }

    companion object {
        const konst DIRTY_SOURCES_FILE_NAME = "dirty-sources.txt"
        const konst LAST_BUILD_INFO_FILE_NAME = "last-build.bin"
        const konst ABI_SNAPSHOT_FILE_NAME = "abi-snapshot.bin"
    }

    private object EmptyCompilationCanceledStatus : CompilationCanceledStatus {
        override fun checkCanceled() {
        }
    }

    protected fun reportPerformanceData(defaultPerformanceManager: CommonCompilerPerformanceManager) {
        defaultPerformanceManager.getMeasurementResults().forEach {
            when (it) {
                is CompilerInitializationMeasurement -> reporter.addTimeMetricMs(BuildTime.COMPILER_INITIALIZATION, it.milliseconds)
                is CodeAnalysisMeasurement -> {
                    reporter.addTimeMetricMs(BuildTime.CODE_ANALYSIS, it.milliseconds)
                    it.lines?.apply {
                        reporter.addMetric(BuildPerformanceMetric.ANALYZED_LINES_NUMBER, this.toLong())
                        if (it.milliseconds > 0) {
                            reporter.addMetric(BuildPerformanceMetric.ANALYSIS_LPS, this * 1000 / it.milliseconds)
                        }
                    }
                }
                is CodeGenerationMeasurement -> {
                    reporter.addTimeMetricMs(BuildTime.CODE_GENERATION, it.milliseconds)
                    it.lines?.apply {
                        reporter.addMetric(BuildPerformanceMetric.CODE_GENERATED_LINES_NUMBER, this.toLong())
                        if (it.milliseconds > 0) {
                            reporter.addMetric(BuildPerformanceMetric.CODE_GENERATION_LPS, this * 1000 / it.milliseconds)
                        }
                    }
                }
            }
        }
    }
}
