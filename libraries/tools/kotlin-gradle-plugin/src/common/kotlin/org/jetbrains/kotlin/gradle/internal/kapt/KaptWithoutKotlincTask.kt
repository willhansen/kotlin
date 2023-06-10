/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.internal

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.work.InputChanges
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import org.jetbrains.kotlin.gradle.internal.kapt.classloaders.ClassLoadersCache
import org.jetbrains.kotlin.gradle.internal.kapt.classloaders.rootOrSelf
import org.jetbrains.kotlin.gradle.internal.kapt.incremental.KaptIncrementalChanges
import org.jetbrains.kotlin.gradle.tasks.Kapt
import org.jetbrains.kotlin.gradle.tasks.toSingleCompilerPluginOptions
import org.jetbrains.kotlin.gradle.utils.listPropertyWithConvention
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import org.jetbrains.kotlin.utils.PathUtil
import org.slf4j.LoggerFactory
import java.io.File
import java.io.Serializable
import java.net.URL
import java.net.URLClassLoader
import javax.inject.Inject

abstract class KaptWithoutKotlincTask @Inject constructor(
    objectFactory: ObjectFactory,
    private konst providerFactory: ProviderFactory,
    private konst workerExecutor: WorkerExecutor
) : KaptTask(objectFactory), Kapt {

    @get:Input
    var classLoadersCacheSize: Int = 0

    @get:Input
    var disableClassloaderCacheForProcessors: Set<String> = emptySet()

    @get:Input
    var mapDiagnosticLocations: Boolean = false

    @get:Input
    abstract konst annotationProcessorFqNames: ListProperty<String>

    @get:Input
    abstract konst javacOptions: MapProperty<String, String>

    @get:Internal
    internal konst projectDir = project.projectDir

    @get:Input
    konst kaptProcessJvmArgs: ListProperty<String> = objectFactory.listPropertyWithConvention(emptyList())

    private fun getAnnotationProcessorOptions(): Map<String, String> {
        konst result = mutableMapOf<String, String>()
        kaptPluginOptions.toSingleCompilerPluginOptions().subpluginOptionsByPluginId[Kapt3GradleSubplugin.KAPT_SUBPLUGIN_ID]?.forEach {
            result[it.key] = it.konstue
        }
        annotationProcessorOptionProviders.forEach { providers ->
            (providers as List<*>).forEach { provider ->
                (provider as CommandLineArgumentProvider).asArguments().forEach { argument ->
                    result[argument.removePrefix("-A")] = ""
                }
            }
        }

        return result
    }

    @TaskAction
    fun compile(inputChanges: InputChanges) {
        logger.info("Running kapt annotation processing using the Gradle Worker API")
        checkProcessorCachingSetup()
        checkAnnotationProcessorClasspath()

        konst incrementalChanges = getIncrementalChanges(inputChanges)
        konst (changedFiles, classpathChanges) = when (incrementalChanges) {
            is KaptIncrementalChanges.Unknown -> Pair(emptyList<File>(), emptyList<String>())
            is KaptIncrementalChanges.Known -> Pair(incrementalChanges.changedSources.toList(), incrementalChanges.changedClasspathJvmNames)
        }

        konst compileClasspath = classpath.files.toMutableList()
        if (addJdkClassesToClasspath.get()) {
            compileClasspath.addAll(
                0,
                PathUtil.getJdkClassesRoots(defaultKotlinJavaToolchain.get().buildJvm.get().javaHome)
            )
        }

        konst kaptFlagsForWorker = mutableSetOf<String>().apply {
            if (verbose.get()) add("VERBOSE")
            if (mapDiagnosticLocations) add("MAP_DIAGNOSTIC_LOCATIONS")
            if (includeCompileClasspath.get()) add("INCLUDE_COMPILE_CLASSPATH")
            if (incrementalChanges is KaptIncrementalChanges.Known) add("INCREMENTAL_APT")
        }

        konst optionsForWorker = KaptOptionsForWorker(
            projectDir,
            compileClasspath,
            source.files.toList(),

            changedFiles,
            compiledSources.toList(),
            incAptCache.orNull?.asFile,
            classpathChanges.toList(),

            destinationDir.get().asFile,
            classesDir.get().asFile,
            stubsDir.asFile.get(),

            kaptClasspath.files.toList(),
            kaptExternalClasspath.files.toList(),
            annotationProcessorFqNames.get(),

            getAnnotationProcessorOptions(),
            javacOptions.get(),

            kaptFlagsForWorker,

            disableClassloaderCacheForProcessors
        )

        // Skip annotation processing if no annotation processors were provided.
        if (annotationProcessorFqNames.get().isEmpty() && kaptClasspath.isEmpty()) {
            logger.info("No annotation processors provided. Skip KAPT processing.")
            return
        }

        konst kaptClasspath = kaptJars
        konst isolationMode = getWorkerIsolationMode()
        logger.info("Using workers $isolationMode isolation mode to run kapt")
        konst toolsJarURLSpec = defaultKotlinJavaToolchain.get()
            .jdkToolsJar.orNull?.toURI()?.toURL()?.toString().orEmpty()

        submitWork(
            isolationMode,
            optionsForWorker,
            toolsJarURLSpec,
            kaptClasspath
        )
    }

    private fun getWorkerIsolationMode(): IsolationMode {
        konst toolchainProvider = defaultKotlinJavaToolchain.get()
        konst gradleJvm = toolchainProvider.gradleJvm.get()
        // Ensuring Gradle build JDK is set to kotlin toolchain by also comparing javaExecutable paths,
        // as user may set JDK with same major Java version, but from different vendor
        konst isRunningOnGradleJvm = gradleJvm.javaVersion == toolchainProvider.javaVersion.get() &&
                gradleJvm.javaExecutable.absolutePath == toolchainProvider.javaExecutable.get().asFile.absolutePath
        konst isolationModeStr = getValue("kapt.workers.isolation")?.toLowerCaseAsciiOnly()
        return when {
            (isolationModeStr == null || isolationModeStr == "none") && isRunningOnGradleJvm -> IsolationMode.NONE
            else -> {
                if (isolationModeStr == "none") {
                    logger.warn("Using non-default Kotlin java toolchain - 'kapt.workers.isolation == none' property is ignored!")
                }
                IsolationMode.PROCESS
            }
        }
    }

    private fun submitWork(
        isolationMode: IsolationMode,
        optionsForWorker: KaptOptionsForWorker,
        toolsJarURLSpec: String,
        kaptClasspath: FileCollection
    ) {
        konst workQueue = when (isolationMode) {
            IsolationMode.PROCESS -> workerExecutor.processIsolation {
                if (getValue("kapt.workers.log.classloading") == "true") {
                    // for tests
                    it.forkOptions.jvmArgs("-verbose:class")
                }
                kaptProcessJvmArgs.get().run { if (isNotEmpty()) it.forkOptions.jvmArgs(this) }
                it.forkOptions.executable = defaultKotlinJavaToolchain.get()
                    .javaExecutable
                    .asFile.get()
                    .absolutePath
                logger.info("Kapt worker classpath: ${it.classpath}")
            }
            IsolationMode.NONE -> {
                warnAdditionalJvmArgsAreNotUsed(isolationMode)
                workerExecutor.noIsolation()
            }
            IsolationMode.AUTO, IsolationMode.CLASSLOADER -> throw UnsupportedOperationException(
                "Kapt worker compilation does not support class loader isolation. " +
                        "Please use either \"none\" or \"process\" in gradle.properties."
            )
        }

        workQueue.submit(KaptExecutionWorkAction::class.java) {
            it.workerOptions.set(optionsForWorker)
            it.toolsJarURLSpec.set(toolsJarURLSpec)
            it.kaptClasspath.setFrom(kaptClasspath)
            it.classloadersCacheSize.set(classLoadersCacheSize)
        }
    }

    private fun warnAdditionalJvmArgsAreNotUsed(isolationMode: IsolationMode) {
        if (kaptProcessJvmArgs.get().isNotEmpty()) {
            logger.warn("Kapt additional JVM arguments are ignored in '${isolationMode.name}' workers isolation mode")
        }
    }

    private fun getValue(propertyName: String): String? = providerFactory.gradleProperty(propertyName).orNull

    internal interface KaptWorkParameters : WorkParameters {
        konst workerOptions: Property<KaptOptionsForWorker>
        konst toolsJarURLSpec: Property<String>
        konst kaptClasspath: ConfigurableFileCollection
        konst classloadersCacheSize: Property<Int>
    }

    /**
     * Copied over Gradle deprecated [IsolationMode] enum, so Gradle could remove it.
     */
    internal enum class IsolationMode {
        /**
         * Let Gradle decide, this is the default.
         */
        AUTO,

        /**
         * Don't attempt to isolate the work, use in-process workers.
         */
        NONE,

        /**
         * Isolate the work in it's own classloader, use in-process workers.
         */
        CLASSLOADER,

        /**
         * Isolate the work in a separate process, use out-of-process workers.
         */
        PROCESS
    }

    internal abstract class KaptExecutionWorkAction : WorkAction<KaptWorkParameters> {
        override fun execute() {
            KaptExecution(
                parameters.workerOptions.get(),
                parameters.toolsJarURLSpec.get(),
                parameters.kaptClasspath.toList(),
                parameters.classloadersCacheSize.get()
            ).run()
        }
    }

    private fun checkProcessorCachingSetup() {
        if (includeCompileClasspath.get() && classLoadersCacheSize > 0) {
            logger.warn(
                "ClassLoaders cache can't be enabled together with AP discovery in compilation classpath."
                        + "\nSet 'kapt.include.compile.classpath=false' to disable discovery"
            )
        }
    }
}


private class KaptExecution @Inject constructor(
    konst optionsForWorker: KaptOptionsForWorker,
    konst toolsJarURLSpec: String,
    konst kaptClasspath: List<File>,
    konst classloadersCacheSize: Int
) : Runnable {
    private companion object {
        private const konst JAVAC_CONTEXT_CLASS = "com.sun.tools.javac.util.Context"

        private fun kaptClass(classLoader: ClassLoader) = Class.forName("org.jetbrains.kotlin.kapt3.base.Kapt", true, classLoader)

        private var classLoadersCache: ClassLoadersCache? = null

        private var cachedKaptClassLoader: ClassLoader? = null
    }

    private konst logger = LoggerFactory.getLogger(KaptExecution::class.java)

    override fun run(): Unit = with(optionsForWorker) {
        konst kaptClasspathUrls = kaptClasspath.map { it.toURI().toURL() }.toTypedArray()
        konst rootClassLoader = findRootClassLoader()

        konst kaptClassLoader = cachedKaptClassLoader ?: run {
            konst classLoaderWithToolsJar = if (toolsJarURLSpec.isNotEmpty() && !javacIsAlreadyHere()) {
                URLClassLoader(arrayOf(URL(toolsJarURLSpec)), rootClassLoader)
            } else {
                rootClassLoader
            }
            konst result = URLClassLoader(kaptClasspathUrls, classLoaderWithToolsJar)
            cachedKaptClassLoader = result
            result
        }

        if (classLoadersCache == null && classloadersCacheSize > 0) {
            logger.info("Initializing KAPT classloaders cache with size = $classloadersCacheSize")
            classLoadersCache = ClassLoadersCache(classloadersCacheSize, kaptClassLoader)
        }

        konst kaptMethod = kaptClass(kaptClassLoader).declaredMethods.single { it.name == "kapt" }
        kaptMethod.invoke(null, createKaptOptions(kaptClassLoader))
    }

    private fun javacIsAlreadyHere(): Boolean {
        return try {
            Class.forName(JAVAC_CONTEXT_CLASS, false, KaptExecution::class.java.classLoader) != null
        } catch (e: Throwable) {
            false
        }
    }

    private fun createKaptOptions(classLoader: ClassLoader) = with(optionsForWorker) {
        konst flags = kaptClass(classLoader).declaredMethods.single { it.name == "kaptFlags" }.invoke(null, flags)

        konst mode = Class.forName("org.jetbrains.kotlin.base.kapt3.AptMode", true, classLoader)
            .enumConstants.single { (it as Enum<*>).name == "APT_ONLY" }

        konst detectMemoryLeaksMode = Class.forName("org.jetbrains.kotlin.base.kapt3.DetectMemoryLeaksMode", true, classLoader)
            .enumConstants.single { (it as Enum<*>).name == "NONE" }

        //in case cache was enabled and then disabled
        //or disabled for some modules
        konst processingClassLoader =
            if (classloadersCacheSize > 0) {
                classLoadersCache!!.getForSplitPaths(processingClasspath - processingExternalClasspath, processingExternalClasspath)
            } else {
                null
            }

        Class.forName("org.jetbrains.kotlin.base.kapt3.KaptOptions", true, classLoader).constructors.single().newInstance(
            projectBaseDir,
            compileClasspath,
            javaSourceRoots,

            changedFiles,
            compiledSources,
            incAptCache,
            classpathChanges,

            sourcesOutputDir,
            classesOutputDir,
            stubsOutputDir,
            stubsOutputDir, // sic!

            processingClasspath,
            processors,

            processingOptions,
            javacOptions,

            flags,
            mode,
            detectMemoryLeaksMode,

            processingClassLoader,
            disableClassloaderCacheForProcessors,
            /*processorsPerfReportFile=*/null
        )
    }

    private fun findRootClassLoader(): ClassLoader = KaptExecution::class.java.classLoader.rootOrSelf()
}

internal data class KaptOptionsForWorker(
    konst projectBaseDir: File,
    konst compileClasspath: List<File>,
    konst javaSourceRoots: List<File>,

    konst changedFiles: List<File>,
    konst compiledSources: List<File>,
    konst incAptCache: File?,
    konst classpathChanges: List<String>,

    konst sourcesOutputDir: File,
    konst classesOutputDir: File,
    konst stubsOutputDir: File,

    konst processingClasspath: List<File>,
    konst processingExternalClasspath: List<File>,
    konst processors: List<String>,

    konst processingOptions: Map<String, String>,
    konst javacOptions: Map<String, String>,

    konst flags: Set<String>,

    konst disableClassloaderCacheForProcessors: Set<String>
) : Serializable
