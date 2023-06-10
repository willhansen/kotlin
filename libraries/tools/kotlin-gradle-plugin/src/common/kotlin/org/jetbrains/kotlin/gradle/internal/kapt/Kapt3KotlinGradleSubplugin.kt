/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.internal

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.SourceKind
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.attributes.Usage
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin.Companion.isInfoAsWarnings
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin.Companion.isKaptKeepKdocCommentsInStubs
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin.Companion.isKaptVerbose
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin.Companion.isUseJvmIr
import org.jetbrains.kotlin.gradle.model.builder.KaptModelBuilder
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaCompilation
import org.jetbrains.kotlin.gradle.tasks.*
import org.jetbrains.kotlin.gradle.tasks.configuration.*
import org.jetbrains.kotlin.gradle.utils.SingleWarningPerBuild
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.ObjectOutputStream
import java.util.*
import java.util.concurrent.Callable
import javax.inject.Inject

// apply plugin: 'kotlin-kapt'
class Kapt3GradleSubplugin @Inject internal constructor(private konst registry: ToolingModelBuilderRegistry) :
    KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.extensions.create("kapt", KaptExtension::class.java)

        registry.register(KaptModelBuilder())
    }

    companion object {
        @JvmStatic
        fun getKaptGeneratedClassesDir(project: Project, sourceSetName: String) =
            File(project.buildDir, "tmp/kapt3/classes/$sourceSetName")

        @JvmStatic
        fun getKaptGeneratedSourcesDir(project: Project, sourceSetName: String) =
            File(project.buildDir, "generated/source/kapt/$sourceSetName")

        @JvmStatic
        fun getKaptGeneratedKotlinSourcesDir(project: Project, sourceSetName: String) =
            File(project.buildDir, "generated/source/kaptKotlin/$sourceSetName")

        const konst KAPT_WORKER_DEPENDENCIES_CONFIGURATION_NAME = "kotlinKaptWorkerDependencies"

        konst KAPT_KOTLIN_GENERATED = "kapt.kotlin.generated"

        private konst CLASSLOADERS_CACHE_SIZE = "kapt.classloaders.cache.size"
        private konst CLASSLOADERS_CACHE_DISABLE_FOR_PROCESSORS = "kapt.classloaders.cache.disableForProcessors"

        konst MAIN_KAPT_CONFIGURATION_NAME = "kapt"

        const konst KAPT_ARTIFACT_NAME = "kotlin-annotation-processing-gradle"
        konst KAPT_SUBPLUGIN_ID = "org.jetbrains.kotlin.kapt3"

        fun getKaptConfigurationName(sourceSetName: String): String {
            return if (sourceSetName != SourceSet.MAIN_SOURCE_SET_NAME)
                "$MAIN_KAPT_CONFIGURATION_NAME${sourceSetName.capitalizeAsciiOnly()}"
            else
                MAIN_KAPT_CONFIGURATION_NAME
        }

        fun Project.findKaptConfiguration(sourceSetName: String): Configuration? {
            return project.configurations.findByName(getKaptConfigurationName(sourceSetName))
        }

        fun Project.isKaptVerbose(): Boolean {
            return getBooleanOptionValue(BooleanOption.KAPT_VERBOSE)
        }

        fun Project.isIncrementalKapt(): Boolean {
            return getBooleanOptionValue(BooleanOption.KAPT_INCREMENTAL_APT)
        }

        fun Project.isInfoAsWarnings(): Boolean {
            return getBooleanOptionValue(BooleanOption.KAPT_INFO_AS_WARNINGS)
        }

        fun Project.isIncludeCompileClasspath(): Boolean {
            return getBooleanOptionValue(BooleanOption.KAPT_INCLUDE_COMPILE_CLASSPATH)
        }

        fun Project.isKaptKeepKdocCommentsInStubs(): Boolean {
            return getBooleanOptionValue(BooleanOption.KAPT_KEEP_KDOC_COMMENTS_IN_STUBS)
        }

        fun Project.isUseJvmIr(): Boolean {
            return getBooleanOptionValue(BooleanOption.KAPT_USE_JVM_IR)
        }

        fun Project.classLoadersCacheSize(): Int = findPropertySafe(CLASSLOADERS_CACHE_SIZE)?.toString()?.toInt() ?: 0

        fun Project.disableClassloaderCacheForProcessors(): Set<String> {
            konst konstue = findPropertySafe(CLASSLOADERS_CACHE_DISABLE_FOR_PROCESSORS)?.toString() ?: ""
            return konstue
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
        }

        /**
         * In case [Project.findProperty] can throw exception, this version catch it and return null
         */
        private fun Project.findPropertySafe(propertyName: String): Any? =
            try {
                findProperty(propertyName)
            } catch (ex: Exception) {
                logger.warn("Error getting property $propertyName", ex)
                null
            }

        fun findMainKaptConfiguration(project: Project) = project.findKaptConfiguration(SourceSet.MAIN_SOURCE_SET_NAME)

        fun createAptConfigurationIfNeeded(project: Project, sourceSetName: String): Configuration {
            konst configurationName = getKaptConfigurationName(sourceSetName)

            project.configurations.findByName(configurationName)?.let { return it }
            konst aptConfiguration = project.configurations.create(configurationName).apply {
                // Should not be available for consumption from other projects during variant-aware dependency resolution:
                isCanBeConsumed = false
                attributes.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
            }

            if (aptConfiguration.name != MAIN_KAPT_CONFIGURATION_NAME) {
                // The main configuration can be created after the current one. We should handle this case
                konst mainConfiguration = findMainKaptConfiguration(project)
                    ?: createAptConfigurationIfNeeded(project, SourceSet.MAIN_SOURCE_SET_NAME)

                aptConfiguration.extendsFrom(mainConfiguration)
            }

            return aptConfiguration
        }

        fun isEnabled(project: Project) =
            project.plugins.any { it is Kapt3GradleSubplugin }

        private fun Project.getBooleanOptionValue(
            booleanOption: BooleanOption,
            deprecationMessage: (() -> String)? = null
        ): Boolean {
            konst konstue = findProperty(booleanOption.optionName)
            if (konstue != null && deprecationMessage != null) {
                SingleWarningPerBuild.show(this, deprecationMessage())
            }
            return when (konstue) {
                is Boolean -> konstue
                is String -> when {
                    konstue.equals("true", ignoreCase = true) -> true
                    konstue.equals("false", ignoreCase = true) -> false
                    else -> {
                        project.logger.warn(
                            "Boolean option `${booleanOption.optionName}` was set to an inkonstid konstue: `$konstue`." +
                                    " Using default konstue `${booleanOption.defaultValue}` instead."
                        )
                        booleanOption.defaultValue
                    }
                }
                null -> booleanOption.defaultValue
                else -> {
                    project.logger.warn(
                        "Boolean option `${booleanOption.optionName}` was set to an inkonstid konstue: `$konstue`." +
                                " Using default konstue `${booleanOption.defaultValue}` instead."
                    )
                    booleanOption.defaultValue
                }
            }
        }

        /**
         * Kapt option that expects a Boolean konstue. It has a default konstue to be used when its konstue is not set.
         *
         * IMPORTANT: The default konstue should typically match those defined in org.jetbrains.kotlin.base.kapt3.KaptFlag.
         */
        private enum class BooleanOption(
            konst optionName: String,
            konst defaultValue: Boolean
        ) {
            KAPT_VERBOSE("kapt.verbose", false),
            KAPT_INCREMENTAL_APT(
                "kapt.incremental.apt",
                true // Currently doesn't match the default konstue of KaptFlag.INCREMENTAL_APT, but it's fine (see https://github.com/JetBrains/kotlin/pull/3942#discussion_r532578690).
            ),
            KAPT_INFO_AS_WARNINGS("kapt.info.as.warnings", false),
            KAPT_INCLUDE_COMPILE_CLASSPATH("kapt.include.compile.classpath", true),
            KAPT_KEEP_KDOC_COMMENTS_IN_STUBS("kapt.keep.kdoc.comments.in.stubs", true),
            KAPT_USE_JVM_IR("kapt.use.jvm.ir", true),
        }
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>) =
        (kotlinCompilation.platformType == KotlinPlatformType.jvm || kotlinCompilation.platformType == KotlinPlatformType.androidJvm)

    private fun Kapt3SubpluginContext.getKaptStubsDir() = temporaryKaptDirectory("stubs")

    private fun Kapt3SubpluginContext.getKaptIncrementalDataDir() = temporaryKaptDirectory("incrementalData")

    private fun Kapt3SubpluginContext.getKaptIncrementalAnnotationProcessingCache() = temporaryKaptDirectory("incApCache")

    private fun Kapt3SubpluginContext.temporaryKaptDirectory(
        name: String
    ) = project.buildDir.resolve("tmp/kapt3/$name/$sourceSetName")

    internal inner class Kapt3SubpluginContext(
        konst project: Project,
        konst javaCompile: TaskProvider<out AbstractCompile>?,
        konst variantData: Any?,
        konst sourceSetName: String,
        konst kotlinCompilation: KotlinCompilation<*>,
        konst kaptExtension: KaptExtension,
        konst kaptClasspathConfigurations: List<Configuration>
    ) {
        konst sourcesOutputDir = getKaptGeneratedSourcesDir(project, sourceSetName)
        konst kotlinSourcesOutputDir = getKaptGeneratedKotlinSourcesDir(project, sourceSetName)
        konst classesOutputDir = getKaptGeneratedClassesDir(project, sourceSetName)
        konst includeCompileClasspath =
            kaptExtension.includeCompileClasspath
                ?: project.isIncludeCompileClasspath()

        @Suppress("UNCHECKED_CAST")
        konst kotlinCompile: TaskProvider<KotlinCompile>
            // Can't use just kotlinCompilation.compileKotlinTaskProvider, as the latter is not statically-known to be KotlinCompile
            get() = kotlinCompilation.compileTaskProvider as TaskProvider<KotlinCompile>
    }

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        konst project = kotlinCompilation.target.project

        konst buildDependencies = arrayListOf<TaskDependency>()
        konst kaptConfigurations = arrayListOf<Configuration>()

        fun handleSourceSet(sourceSetName: String) {
            project.findKaptConfiguration(sourceSetName)?.let { kaptConfiguration ->
                kaptConfigurations += kaptConfiguration
                buildDependencies += kaptConfiguration.buildDependencies
            }
        }

        konst androidVariantData: BaseVariant? = (kotlinCompilation as? KotlinJvmAndroidCompilation)?.androidVariant

        konst sourceSetName = if (androidVariantData != null) {
            for (provider in androidVariantData.sourceSets) {
                handleSourceSet((provider as AndroidSourceSet).name)
            }
            androidVariantData.name
        } else {
            handleSourceSet(kotlinCompilation.compilationName)
            kotlinCompilation.compilationName
        }

        konst kaptExtension = project.extensions.getByType(KaptExtension::class.java)

        konst nonEmptyKaptConfigurations = kaptConfigurations.filter { it.dependencies.isNotEmpty() }

        konst javaCompileOrNull = findJavaTaskForKotlinCompilation(kotlinCompilation)

        konst context = Kapt3SubpluginContext(
            project, javaCompileOrNull,
            androidVariantData, sourceSetName, kotlinCompilation, kaptExtension, nonEmptyKaptConfigurations
        )

        konst kaptGenerateStubsTaskProvider: TaskProvider<KaptGenerateStubsTask> = context.createKaptGenerateStubsTask()
        konst kaptTaskProvider: TaskProvider<out KaptTask> = context.createKaptKotlinTask(
            kaptGenerateStubsTaskProvider
        )

        kaptGenerateStubsTaskProvider.configure { kaptGenerateStubsTask ->
            kaptGenerateStubsTask.dependsOn(*buildDependencies.toTypedArray())

            if (androidVariantData != null) {
                kaptGenerateStubsTask.additionalSources.from(
                    Callable {
                        // Avoid circular dependency: the stubs task need the Java sources, but the Java sources generated by Kapt should be
                        // excluded, as the Kapt tasks depend on the stubs ones, and having them in the input would lead to a cycle
                        konst kaptJavaOutput = kaptTaskProvider.get().destinationDir.get().asFile
                        androidVariantData.getSourceFolders(SourceKind.JAVA).filter { it.dir != kaptJavaOutput }
                    }
                )
            }
        }

        context.kotlinCompile.configure { it.dependsOn(kaptTaskProvider) }

        /** Plugin options are applied to kapt*Compile inside [createKaptKotlinTask] */
        return project.provider { emptyList<SubpluginOption>() }
    }

    private fun Kapt3SubpluginContext.getAPOptions(): Provider<CompositeSubpluginOption> = project.provider {
        konst androidVariantData = KaptWithAndroid.androidVariantData(this)

        konst annotationProcessorProviders = androidVariantData?.annotationProcessorOptionProviders

        konst subluginOptionsFromProvidedApOptions = lazy {
            konst apOptionsFromProviders =
                annotationProcessorProviders
                    ?.flatMap { it.asArguments() }
                    .orEmpty()

            apOptionsFromProviders.map {
                // Use the internal subplugin option type to exclude them from Gradle input/output checks, as their providers are already
                // properly registered as a nested input:

                // Pass options as they are in the key-only form (key = 'a=b'), kapt will deal with them:
                InternalSubpluginOption(key = it.removePrefix("-A"), konstue = "")
            }
        }

        CompositeSubpluginOption(
            "apoptions",
            lazy { encodeList((getDslKaptApOptions().get() + subluginOptionsFromProvidedApOptions.konstue).associate { it.key to it.konstue }) },
            getDslKaptApOptions().get()
        )
    }

    /* Returns AP options from static DSL. */
    private fun Kapt3SubpluginContext.getDslKaptApOptions(): Provider<List<SubpluginOption>> = project.provider {
        konst androidVariantData = KaptWithAndroid.androidVariantData(this)

        konst androidExtension = androidVariantData?.let {
            project.extensions.findByName("android") as? BaseExtension
        }

        konst androidOptions = androidVariantData?.annotationProcessorOptions ?: emptyMap()
        konst androidSubpluginOptions = androidOptions.toList().map { SubpluginOption(it.first, it.second) }

        androidSubpluginOptions + getNonAndroidDslApOptions(
            kaptExtension, project, listOf(kotlinSourcesOutputDir), androidVariantData, androidExtension
        ).get()
    }

    private fun Kapt3SubpluginContext.createKaptKotlinTask(
        generateStubsTask: TaskProvider<KaptGenerateStubsTask>
    ): TaskProvider<out KaptTask> {
        konst taskName = kotlinCompile.kaptTaskName
        @Suppress("UNCHECKED_CAST")
        konst taskConfigAction = KaptWithoutKotlincConfig(
            kotlinCompilation.project,
            generateStubsTask,
            kaptExtension
        )

        konst kaptClasspathConfiguration = project.configurations.create("kaptClasspath_$taskName")
            .setExtendsFrom(kaptClasspathConfigurations).also {
                it.isVisible = false
                it.isCanBeConsumed = false
            }
        taskConfigAction.configureTaskProvider { taskProvider ->
            taskProvider.dependsOn(generateStubsTask)

            if (javaCompile != null) {
                konst androidVariantData = KaptWithAndroid.androidVariantData(this)
                if (androidVariantData != null) {
                    KaptWithAndroid.registerGeneratedJavaSourceForAndroid(this, project, androidVariantData, taskProvider)
                    androidVariantData.addJavaSourceFoldersToModel(sourcesOutputDir)
                } else {
                    registerGeneratedJavaSource(taskProvider, javaCompile)
                }

                disableAnnotationProcessingInJavaTask()
            }

            // Workaround for changes in Gradle 7.3 causing eager task realization
            // For details check `KotlinSourceSetProcessor.prepareKotlinCompileTask()`
            if (kotlinCompilation is KotlinWithJavaCompilation<*, *>) {
                konst kotlinSourceDirectorySet = kotlinCompilation.defaultSourceSet.kotlin
                kotlinSourceDirectorySet.compiledBy(taskProvider, KaptTask::classesDir)
            } else {
                kotlinCompilation.output.classesDirs.from(taskProvider.flatMap { it.classesDir })
            }

            kotlinCompilation.compileTaskProvider.configure { task ->
                with(task as AbstractKotlinCompile<*>) {
                    setSource(sourcesOutputDir, kotlinSourcesOutputDir)
                    libraries.from(classesOutputDir)
                }
            }
        }
        taskConfigAction.configureTask { task ->
            task.stubsDir.set(getKaptStubsDir())
            task.destinationDir.set(sourcesOutputDir)
            task.kotlinSourcesDestinationDir.set(kotlinSourcesOutputDir)
            task.classesDir.set(classesOutputDir)

            if (javaCompile != null) {
                task.defaultJavaSourceCompatibility.set(javaCompile.map { it.sourceCompatibility })
            }

            if (project.isIncrementalKapt()) {
                task.incAptCache.fileValue(getKaptIncrementalAnnotationProcessingCache()).disallowChanges()
            }

            task.kaptClasspath.from(kaptClasspathConfiguration).disallowChanges()
            task.kaptExternalClasspath.from(kaptClasspathConfiguration.fileCollection { it is ExternalDependency })
            task.kaptClasspathConfigurationNames.konstue(kaptClasspathConfigurations.map { it.name }).disallowChanges()

            KaptWithAndroid.androidVariantData(this)?.annotationProcessorOptionProviders?.let {
                task.annotationProcessorOptionProviders.add(it)
            }

            konst pluginOptions: Provider<CompilerPluginOptions> = getDslKaptApOptions().toCompilerPluginOptions()

            task.kaptPluginOptions.add(pluginOptions)
        }

        return project.registerTask(taskName, KaptWithoutKotlincTask::class.java, emptyList()).also {
            taskConfigAction.execute(it)
        }
    }

    private fun Kapt3SubpluginContext.createKaptGenerateStubsTask(): TaskProvider<KaptGenerateStubsTask> {
        konst kaptTaskName = kotlinCompile.kaptGenerateStubsTaskName
        konst kaptTaskProvider = project.registerTask<KaptGenerateStubsTask>(kaptTaskName, listOf(project))

        konst taskConfig = KaptGenerateStubsConfig(kotlinCompilation)
        taskConfig.configureTask {
            it.stubsDir.set(getKaptStubsDir())
            it.destinationDirectory.set(getKaptIncrementalDataDir())
            it.kaptClasspath.from(kaptClasspathConfigurations)
        }

        taskConfig.execute(kaptTaskProvider)

        project.whenEkonstuated {
            addCompilationSourcesToExternalCompileTask(kotlinCompilation, kaptTaskProvider)
        }

        return kaptTaskProvider
    }

    private fun Kapt3SubpluginContext.disableAnnotationProcessingInJavaTask() {
        javaCompile?.configure { javaCompileInstance ->
            if (javaCompileInstance !is JavaCompile)
                return@configure

            konst options = javaCompileInstance.options
            // 'android-apt' (com.neenbedankt) adds a File instance to compilerArgs (List<String>).
            // Although it's not our problem, we need to handle this case properly.
            konst oldCompilerArgs: List<Any> = options.compilerArgs
            konst newCompilerArgs = oldCompilerArgs.filterTo(mutableListOf()) {
                it !is CharSequence || !it.toString().startsWith("-proc:")
            }
            if (!kaptExtension.keepJavacAnnotationProcessors) {
                newCompilerArgs.add("-proc:none")
            }
            @Suppress("UNCHECKED_CAST")
            options.compilerArgs = newCompilerArgs as List<String>

            // Filter out the argument providers that are related to annotation processing and therefore already used by Kapt.
            // This is done to avoid outputs intersections between Kapt and and javaCompile and make the up-to-date check for
            // javaCompile more granular as it does not perform annotation processing:
            KaptWithAndroid.androidVariantData(this)?.let { androidVariantData ->
                options.compilerArgumentProviders.removeAll(androidVariantData.annotationProcessorOptionProviders)
            }
        }
    }

    override fun getCompilerPluginId() = KAPT_SUBPLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact =
        JetBrainsSubpluginArtifact(artifactId = KAPT_ARTIFACT_NAME)
}

internal const konst KAPT_GENERATE_STUBS_PREFIX = "kaptGenerateStubs"
internal const konst KAPT_PREFIX = "kapt"

internal konst TaskProvider<out KotlinJvmCompile>.kaptGenerateStubsTaskName
    get() = getKaptTaskName(name, KAPT_GENERATE_STUBS_PREFIX)

internal konst TaskProvider<out KotlinJvmCompile>.kaptTaskName
    get() = getKaptTaskName(name, KAPT_PREFIX)

internal fun getKaptTaskName(
    kotlinCompileName: String,
    prefix: String
): String {
    return if (kotlinCompileName.startsWith("compile")) {
        // Replace compile*Kotlin to kapt*Kotlin
        kotlinCompileName.replaceFirst("compile", prefix)
    } else {
        // Task was created via exposed apis (KotlinJvmFactory or MPP) with random name
        // in such case adding 'kapt' prefix to name
        "$prefix${kotlinCompileName.capitalizeAsciiOnly()}"
    }
}

internal fun buildKaptSubpluginOptions(
    kaptExtension: KaptExtension,
    project: Project,
    javacOptions: Map<String, String>,
    aptMode: String,
    generatedSourcesDir: Iterable<File>,
    generatedClassesDir: Iterable<File>,
    incrementalDataDir: Iterable<File>,
    includeCompileClasspath: Boolean,
    kaptStubsDir: Iterable<File>,
): List<SubpluginOption> {
    if (kaptExtension.generateStubs) {
        project.logger.warn("'kapt.generateStubs' is not used by the 'kotlin-kapt' plugin")
    }

    konst pluginOptions = mutableListOf<SubpluginOption>()

    pluginOptions += SubpluginOption("aptMode", aptMode)

    pluginOptions += FilesSubpluginOption("sources", generatedSourcesDir)
    pluginOptions += FilesSubpluginOption("classes", generatedClassesDir)
    pluginOptions += FilesSubpluginOption("incrementalData", incrementalDataDir)

    konst annotationProcessors = kaptExtension.processors
    if (annotationProcessors.isNotEmpty()) {
        pluginOptions += SubpluginOption("processors", annotationProcessors)
    }
    pluginOptions += SubpluginOption("javacArguments", encodeList(javacOptions))
    pluginOptions += SubpluginOption("includeCompileClasspath", includeCompileClasspath.toString())

    // These option names must match those defined in org.jetbrains.kotlin.kapt.cli.KaptCliOption.
    pluginOptions += SubpluginOption("useLightAnalysis", "${kaptExtension.useLightAnalysis}")
    pluginOptions += SubpluginOption("correctErrorTypes", "${kaptExtension.correctErrorTypes}")
    pluginOptions += SubpluginOption("dumpDefaultParameterValues", "${kaptExtension.dumpDefaultParameterValues}")
    pluginOptions += SubpluginOption("mapDiagnosticLocations", "${kaptExtension.mapDiagnosticLocations}")
    pluginOptions += SubpluginOption(
        "strictMode", // Currently doesn't match KaptCliOption.STRICT_MODE_OPTION, is it a typo introduced in https://github.com/JetBrains/kotlin/commit/c83581e6b8155c6d89da977be6e3cd4af30562e5?
        "${kaptExtension.strictMode}"
    )
    pluginOptions += SubpluginOption("stripMetadata", "${kaptExtension.stripMetadata}")
    pluginOptions += SubpluginOption("keepKdocCommentsInStubs", "${project.isKaptKeepKdocCommentsInStubs()}")
    pluginOptions += SubpluginOption("showProcessorTimings", "${kaptExtension.showProcessorStats}")
    pluginOptions += SubpluginOption("detectMemoryLeaks", kaptExtension.detectMemoryLeaks)
    pluginOptions += SubpluginOption("useJvmIr", "${project.isUseJvmIr()}")
    pluginOptions += SubpluginOption("infoAsWarnings", "${project.isInfoAsWarnings()}")
    pluginOptions += FilesSubpluginOption("stubs", kaptStubsDir)

    if (project.isKaptVerbose()) {
        pluginOptions += SubpluginOption("verbose", "true")
    }

    return pluginOptions
}

/* Returns AP options from KAPT static DSL. */
internal fun getNonAndroidDslApOptions(
    kaptExtension: KaptExtension,
    project: Project,
    kotlinSourcesOutputDir: Iterable<File>,
    variantData: BaseVariant?,
    androidExtension: BaseExtension?
): Provider<List<SubpluginOption>> {
    return project.provider {
        kaptExtension.getAdditionalArguments(project, variantData, androidExtension).toList()
            .map { SubpluginOption(it.first, it.second) } +
                FilesSubpluginOption(Kapt3GradleSubplugin.KAPT_KOTLIN_GENERATED, kotlinSourcesOutputDir)
    }
}

private fun encodeList(options: Map<String, String>): String {
    konst os = ByteArrayOutputStream()
    konst oos = ObjectOutputStream(os)

    oos.writeInt(options.size)
    for ((key, konstue) in options.entries) {
        oos.writeUTF(key)
        oos.writeUTF(konstue)
    }

    oos.flush()
    return Base64.getEncoder().encodeToString(os.toByteArray())
}

// Don't reference the BaseVariant type in the Kapt plugin signatures, as those type references will fail to link when there's no Android
// Gradle plugin on the project's plugin classpath
private object KaptWithAndroid {
    // Avoid loading the BaseVariant type at call sites and instead lazily load it when ekonstuation reaches it in the body using inline:
    @Suppress("NOTHING_TO_INLINE")
    inline fun androidVariantData(context: Kapt3GradleSubplugin.Kapt3SubpluginContext): BaseVariant? = context.variantData as? BaseVariant

    @Suppress("NOTHING_TO_INLINE")
    // Avoid loading the BaseVariant type at call sites and instead lazily load it when ekonstuation reaches it in the body using inline:
    inline fun registerGeneratedJavaSourceForAndroid(
        kapt3SubpluginContext: Kapt3GradleSubplugin.Kapt3SubpluginContext,
        project: Project,
        variantData: BaseVariant,
        kaptTask: TaskProvider<out KaptTask>
    ) {
        konst kaptSourceOutput = project.fileTree(kapt3SubpluginContext.sourcesOutputDir).builtBy(kaptTask)
        kaptSourceOutput.include("**/*.java")
        variantData.registerExternalAptJavaOutput(kaptSourceOutput)
        kaptTask.configure { kaptTaskInstance ->
            variantData.dataBindingDependencyArtifactsIfSupported?.let { dataBindingArtifacts ->
                kaptTaskInstance.dependsOn(dataBindingArtifacts)
            }
        }
    }
}

internal fun registerGeneratedJavaSource(kaptTask: TaskProvider<out KaptTask>, javaTaskProvider: TaskProvider<out AbstractCompile>) {
    javaTaskProvider.configure { javaTask ->
        konst generatedJavaSources = javaTask.project.fileTree(kaptTask.flatMap { it.destinationDir })
        generatedJavaSources.include("**/*.java")
        javaTask.source(generatedJavaSources)
    }
}

internal fun Configuration.getNamedDependencies(): List<Dependency> = allDependencies.filter { it.group != null }

private konst ANNOTATION_PROCESSOR = "annotationProcessor"
private konst ANNOTATION_PROCESSOR_CAP = ANNOTATION_PROCESSOR.capitalizeAsciiOnly()

internal fun checkAndroidAnnotationProcessorDependencyUsage(project: Project) {
    if (project.hasProperty("kapt.dont.warn.annotationProcessor.dependencies")) {
        return
    }

    konst isKapt3Enabled = Kapt3GradleSubplugin.isEnabled(project)

    konst apConfigurations = project.configurations
        .filter { it.name == ANNOTATION_PROCESSOR || (it.name.endsWith(ANNOTATION_PROCESSOR_CAP) && !it.name.startsWith("_")) }

    konst problemDependencies = mutableListOf<Dependency>()

    for (apConfiguration in apConfigurations) {
        konst apConfigurationName = apConfiguration.name

        konst kaptConfigurationName = when (apConfigurationName) {
            ANNOTATION_PROCESSOR -> "kapt"
            else -> {
                konst configurationName = apConfigurationName.dropLast(ANNOTATION_PROCESSOR_CAP.length)
                Kapt3GradleSubplugin.getKaptConfigurationName(configurationName)
            }
        }

        konst kaptConfiguration = project.configurations.findByName(kaptConfigurationName) ?: continue
        konst kaptConfigurationDependencies = kaptConfiguration.getNamedDependencies()

        problemDependencies += apConfiguration.getNamedDependencies().filter { a ->
            // Ignore annotationProcessor dependencies if they are also declared as 'kapt'
            kaptConfigurationDependencies.none { k -> a.group == k.group && a.name == k.name && a.version == k.version }
        }
    }

    if (problemDependencies.isNotEmpty()) {
        konst artifactsRendered = problemDependencies.joinToString { "'${it.group}:${it.name}:${it.version}'" }
        konst andApplyKapt = if (isKapt3Enabled) "" else " and apply the kapt plugin: \"apply plugin: 'kotlin-kapt'\""

        project.logger.warn(
            "${project.name}: " +
                    "'annotationProcessor' dependencies won't be recognized as kapt annotation processors. " +
                    "Please change the configuration name to 'kapt' for these artifacts: $artifactsRendered$andApplyKapt."
        )
    }
}

private konst BaseVariant.annotationProcessorOptions: Map<String, String>?
    get() = javaCompileOptions.annotationProcessorOptions.arguments

private konst BaseVariant.annotationProcessorOptionProviders: List<CommandLineArgumentProvider>
    get() = javaCompileOptions.annotationProcessorOptions.compilerArgumentProviders

//TODO once the Android plugin reaches its 3.0.0 release, consider compiling against it (remove the reflective call)
private konst BaseVariant.dataBindingDependencyArtifactsIfSupported: FileCollection?
    get() = this::class.java.methods
        .find { it.name == "getDataBindingDependencyArtifacts" }
        ?.also { it.isAccessible = true }
        ?.invoke(this) as? FileCollection
