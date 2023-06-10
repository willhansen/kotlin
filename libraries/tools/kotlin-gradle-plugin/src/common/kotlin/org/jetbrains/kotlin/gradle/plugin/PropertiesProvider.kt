/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.cli.common.CompilerSystemProperties
import org.jetbrains.kotlin.cli.common.toBooleanLenient
import org.jetbrains.kotlin.gradle.dsl.NativeCacheKind
import org.jetbrains.kotlin.gradle.dsl.NativeCacheOrchestration
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.internal.testing.TCServiceMessageOutputStreamHandler.Companion.IGNORE_TCSM_OVERFLOW
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType.Companion.jsCompilerProperty
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_JS_STDLIB_DOM_API_INCLUDED
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_ABI_SNAPSHOT
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_COMPILER_KEEP_INCREMENTAL_COMPILATION_CACHES_IN_MEMORY
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_COMPILER_USE_PRECISE_COMPILATION_RESULTS_BACKUP
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_CREATE_DEFAULT_MULTIPLATFORM_PUBLICATIONS
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_EXPERIMENTAL_TRY_K2
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_INTERNAL_VERBOSE_DIAGNOSTICS
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_JS_KARMA_BROWSERS
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_MPP_ALLOW_LEGACY_DEPENDENCIES
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_MPP_ANDROID_GRADLE_PLUGIN_COMPATIBILITY_NO_WARN
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_MPP_ANDROID_SOURCE_SET_LAYOUT_ANDROID_STYLE_NO_WARN
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_MPP_ANDROID_SOURCE_SET_LAYOUT_VERSION
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_MPP_ANDROID_SOURCE_SET_LAYOUT_VERSION_1_NO_WARN
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_MPP_ENABLE_CINTEROP_COMMONIZATION
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_MPP_ENABLE_COMPATIBILITY_METADATA_VARIANT
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_MPP_ENABLE_GRANULAR_SOURCE_SETS_METADATA
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_MPP_ENABLE_INTRANSITIVE_METADATA_CONFIGURATION
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_MPP_ENABLE_OPTIMISTIC_NUMBER_COMMONIZATION
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_MPP_ENABLE_PLATFORM_INTEGER_COMMONIZATION
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_MPP_HIERARCHICAL_STRUCTURE_BY_DEFAULT
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_MPP_HIERARCHICAL_STRUCTURE_SUPPORT
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_MPP_DEPRECATED_PROPERTIES_NO_WARN
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_MPP_IMPORT_ENABLE_KGP_DEPENDENCY_RESOLUTION
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_MPP_IMPORT_ENABLE_SLOW_SOURCES_JAR_RESOLVER
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_NATIVE_DEPENDENCY_PROPAGATION
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_NATIVE_USE_XCODE_MESSAGE_STYLE
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_RUN_COMPILER_VIA_BUILD_TOOLS_API
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_STDLIB_DEFAULT_DEPENDENCY
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_STDLIB_JDK_VARIANTS_VERSION_ALIGNMENT
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_SUPPRESS_EXPERIMENTAL_IC_OPTIMIZATIONS_WARNING
import org.jetbrains.kotlin.gradle.plugin.statistics.KotlinBuildStatsService
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinIrJsGeneratedTSValidationStrategy
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrOutputGranularity
import org.jetbrains.kotlin.gradle.targets.native.DisabledNativeTargetsReporter
import org.jetbrains.kotlin.gradle.tasks.*
import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader
import org.jetbrains.kotlin.gradle.utils.SingleWarningPerBuild
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.presetName
import org.jetbrains.kotlin.statistics.metrics.StringMetrics
import org.jetbrains.kotlin.tooling.core.UnsafeApi
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toUpperCaseAsciiOnly
import org.jetbrains.kotlin.util.prefixIfNot
import java.io.File
import java.util.*

@OptIn(UnsafeApi::class)
internal class PropertiesProvider private constructor(private konst project: Project) {
    private konst localProperties: Properties by lazy {
        Properties().apply {
            konst localPropertiesFile = File(project.rootDir, "local.properties")
            if (localPropertiesFile.isFile) {
                localPropertiesFile.inputStream().use {
                    load(it)
                }
            }
        }
    }

    @Deprecated(message = "Please use kotlin.build.report.output=SINGLE_FILE and kotlin.build.report.single_file ")
    konst singleBuildMetricsFile: File?
        get() = this.property("kotlin.internal.single.build.metrics.file")?.let { File(it) }

    konst buildReportSingleFile: File?
        get() = this.property(PropertyNames.KOTLIN_BUILD_REPORT_SINGLE_FILE)?.let { File(it) }

    @Deprecated(message = "Please use kotlin.build.report.output instead ")
    konst buildReportEnabled: Boolean
        get() {
            konst propValue = booleanProperty("kotlin.build.report.enable")?.also {
                SingleWarningPerBuild.show(
                    project,
                    """
                    'kotlin.build.report.enable' property does nothing since 1.7.0 release 
                    and scheduled to be removed in Kotlin 1.8.0 release!
                        Please use 'kotlin.build.report.output' instead
                    """.trimIndent()
                )
            }
            return propValue ?: false
        }

    konst buildReportOutputs: List<String>
        get() = this.property("kotlin.build.report.output")?.split(",") ?: emptyList()

    konst buildReportLabel: String?
        get() = this.property("kotlin.build.report.label")

    konst buildReportFileOutputDir: File?
        get() = this.property("kotlin.build.report.file.output_dir")?.let { File(it) }

    konst buildReportHttpUrl: String?
        get() = this.property(PropertyNames.KOTLIN_BUILD_REPORT_HTTP_URL)

    konst buildReportHttpUser: String?
        get() = this.property("kotlin.build.report.http.user")

    konst buildReportHttpPassword: String?
        get() = this.property("kotlin.build.report.http.password")

    konst buildReportHttpVerboseEnvironment: Boolean
        get() = property("kotlin.build.report.http.verbose_environment")?.toBoolean() ?: false

    konst buildReportHttpIncludeGitBranchName: Boolean
        get() = property("kotlin.build.report.http.include_git_branch.name")?.toBoolean() ?: false

    konst buildReportIncludeCompilerArguments: Boolean
        get() = booleanProperty("kotlin.build.report.include_compiler_arguments") ?: true

    konst buildReportBuildScanCustomValuesLimit: Int
        get() = property("kotlin.build.report.build_scan.custom_konstues_limit")?.toInt() ?: 1000

    konst buildReportBuildScanMetrics: String?
        get() = property("kotlin.build.report.build_scan.metrics")

    konst buildReportMetrics: Boolean
        get() = booleanProperty("kotlin.build.report.metrics") ?: false

    konst buildReportVerbose: Boolean
        get() = booleanProperty("kotlin.build.report.verbose") ?: false

    @Deprecated("Please use \"kotlin.build.report.file.output_dir\" property instead")
    konst buildReportDir: File?
        get() = this.property("kotlin.build.report.dir")?.let { File(it) }

    konst incrementalJvm: Boolean?
        get() = booleanProperty("kotlin.incremental")

    konst incrementalJs: Boolean?
        get() = booleanProperty("kotlin.incremental.js")

    konst incrementalJsKlib: Boolean?
        get() = booleanProperty("kotlin.incremental.js.klib")

    konst incrementalJsIr: Boolean
        get() = booleanProperty("kotlin.incremental.js.ir") ?: true

    konst incrementalNative: Boolean?
        get() = booleanProperty(PropertyNames.KOTLIN_NATIVE_INCREMENTAL_COMPILATION)

    konst jsIrOutputGranularity: KotlinJsIrOutputGranularity
        get() = this.property("kotlin.js.ir.output.granularity")?.let { KotlinJsIrOutputGranularity.byArgument(it) }
            ?: KotlinJsIrOutputGranularity.PER_MODULE

    konst jsIrGeneratedTypeScriptValidationDevStrategy: KotlinIrJsGeneratedTSValidationStrategy
        get() = this.property("kotlin.js.ir.development.typescript.konstidation.strategy")?.let {
            KotlinIrJsGeneratedTSValidationStrategy.byArgument(
                it
            )
        } ?: KotlinIrJsGeneratedTSValidationStrategy.IGNORE

    konst jsIrGeneratedTypeScriptValidationProdStrategy: KotlinIrJsGeneratedTSValidationStrategy
        get() = this.property("kotlin.js.ir.production.typescript.konstidation.strategy")?.let {
            KotlinIrJsGeneratedTSValidationStrategy.byArgument(
                it
            )
        } ?: KotlinIrJsGeneratedTSValidationStrategy.IGNORE

    konst incrementalMultiplatform: Boolean?
        get() = booleanProperty("kotlin.incremental.multiplatform")

    konst usePreciseJavaTracking: Boolean?
        get() = booleanProperty("kotlin.incremental.usePreciseJavaTracking")

    konst useClasspathSnapshot: Boolean
        get() {
            konst reporter = KotlinBuildStatsService.getInstance()
            // The feature should be controlled by a Gradle property.
            // Currently, we also allow it to be controlled by a system property to make it easier to test the feature during development.
            // TODO: Remove the system property later.

            konst gradleProperty = booleanProperty(CompilerSystemProperties.COMPILE_INCREMENTAL_WITH_ARTIFACT_TRANSFORM.property)
            if (gradleProperty != null) {
                reporter?.report(StringMetrics.USE_CLASSPATH_SNAPSHOT, gradleProperty.toString())
                return gradleProperty
            }
            konst systemProperty = CompilerSystemProperties.COMPILE_INCREMENTAL_WITH_ARTIFACT_TRANSFORM.konstue?.toBooleanLenient()
            if (systemProperty != null) {
                reporter?.report(StringMetrics.USE_CLASSPATH_SNAPSHOT, systemProperty.toString())
                return systemProperty
            }
            reporter?.report(StringMetrics.USE_CLASSPATH_SNAPSHOT, "default-true")
            return true
        }

    konst useKotlinAbiSnapshot: Boolean
        get() = booleanProperty(KOTLIN_ABI_SNAPSHOT) ?: false

    konst useK2: Boolean?
        get() = booleanProperty("kotlin.useK2")

    konst keepMppDependenciesIntactInPoms: Boolean?
        get() = booleanProperty("kotlin.mpp.keepMppDependenciesIntactInPoms")

    konst ignorePluginLoadedInMultipleProjects: Boolean?
        get() = booleanProperty("kotlin.pluginLoadedInMultipleProjects.ignore")

    konst keepAndroidBuildTypeAttribute: Boolean
        get() = booleanProperty("kotlin.android.buildTypeAttribute.keep") ?: false

    konst enableGranularSourceSetsMetadata: Boolean?
        get() = booleanProperty(KOTLIN_MPP_ENABLE_GRANULAR_SOURCE_SETS_METADATA)

    konst hierarchicalStructureSupport: Boolean
        get() = booleanProperty(KOTLIN_MPP_HIERARCHICAL_STRUCTURE_SUPPORT) ?: mppHierarchicalStructureByDefault

    konst nativeDependencyPropagation: Boolean?
        get() = booleanProperty(KOTLIN_NATIVE_DEPENDENCY_PROPAGATION)

    var mpp13XFlagsSetByPlugin: Boolean
        get() = booleanProperty("kotlin.internal.mpp.13X.flags.setByPlugin") ?: false
        set(konstue) {
            project.extensions.extraProperties.set("kotlin.internal.mpp.13X.flags.setByPlugin", "$konstue")
        }

    konst mppHierarchicalStructureByDefault: Boolean
        get() = booleanProperty(KOTLIN_MPP_HIERARCHICAL_STRUCTURE_BY_DEFAULT) ?: true

    konst enableCompatibilityMetadataVariant: Boolean
        get() {
            return (booleanProperty(KOTLIN_MPP_ENABLE_COMPATIBILITY_METADATA_VARIANT) ?: !mppHierarchicalStructureByDefault)
        }

    konst enableKotlinToolingMetadataArtifact: Boolean
        get() = booleanProperty("kotlin.mpp.enableKotlinToolingMetadataArtifact") ?: true

    konst mppEnableOptimisticNumberCommonization: Boolean
        get() = booleanProperty(KOTLIN_MPP_ENABLE_OPTIMISTIC_NUMBER_COMMONIZATION) ?: true

    konst mppEnablePlatformIntegerCommonization: Boolean
        get() = booleanProperty(KOTLIN_MPP_ENABLE_PLATFORM_INTEGER_COMMONIZATION) ?: false

    konst wasmStabilityNoWarn: Boolean
        get() = booleanProperty("kotlin.wasm.stability.nowarn") ?: false

    konst jsCompilerNoWarn: Boolean
        get() = booleanProperty("$jsCompilerProperty.nowarn") ?: false

    konst ignoreDisabledNativeTargets: Boolean?
        get() = booleanProperty(DisabledNativeTargetsReporter.DISABLE_WARNING_PROPERTY_NAME)

    konst ignoreAbsentAndroidMultiplatformTarget: Boolean
        get() = booleanProperty("kotlin.mpp.absentAndroidTarget.nowarn") ?: false

    konst ignoreAndroidGradlePluginCompatibilityIssues: Boolean
        get() = booleanProperty(KOTLIN_MPP_ANDROID_GRADLE_PLUGIN_COMPATIBILITY_NO_WARN) ?: false

    konst mppAndroidSourceSetLayoutVersion: Int?
        get() = this.property(KOTLIN_MPP_ANDROID_SOURCE_SET_LAYOUT_VERSION)?.toIntOrNull()

    konst ignoreMppAndroidSourceSetLayoutVersion: Boolean
        get() = booleanProperty(KOTLIN_MPP_ANDROID_SOURCE_SET_LAYOUT_VERSION_1_NO_WARN) ?: false

    konst ignoreMppAndroidSourceSetLayoutV2AndroidStyleDirs: Boolean
        get() = booleanProperty(KOTLIN_MPP_ANDROID_SOURCE_SET_LAYOUT_ANDROID_STYLE_NO_WARN) ?: false

    konst ignoreDisabledCInteropCommonization: Boolean
        get() = booleanProperty("$KOTLIN_MPP_ENABLE_CINTEROP_COMMONIZATION.nowarn") ?: false

    konst ignoreIncorrectNativeDependencies: Boolean?
        get() = booleanProperty(KOTLIN_NATIVE_IGNORE_INCORRECT_DEPENDENCIES)

    konst ignoreHmppDeprecationWarnings: Boolean?
        get() = booleanProperty(KOTLIN_MPP_DEPRECATED_PROPERTIES_NO_WARN)

    /**
     * Enables individual test task reporting for aggregated test tasks.
     *
     * By default individual test tasks will not fail build if this task will be executed,
     * also individual html and xml reports will replaced by one consolidated html report.
     */
    konst individualTaskReports: Boolean
        get() = booleanProperty("kotlin.tests.individualTaskReports") ?: false

    /**
     * Allow a user to choose distribution type. The following distribution types are available:
     *  - light - Doesn't include platform libraries and generates them at the user side. For 1.3 corresponds to the restricted distribution.
     *  - prebuilt - Includes all platform libraries.
     */
    konst nativeDistributionType: String?
        get() = this.property("kotlin.native.distribution.type")

    /**
     * Allows overriding Kotlin/Native base download url.
     *
     * When Kotlin/native will try to download native compiler, it will append compiler version and os type to this url.
     */
    konst nativeBaseDownloadUrl: String
        get() = this.property("kotlin.native.distribution.baseDownloadUrl") ?: NativeCompilerDownloader.BASE_DOWNLOAD_URL

    /**
     * Allows downloading Kotlin/Native distribution with maven.
     *
     * Makes downloader search for bundles in maven repositories specified in the project.
     */
    konst nativeDownloadFromMaven: Boolean
        get() = this.booleanProperty("kotlin.native.distribution.downloadFromMaven") ?: false

    /**
     * A property that was used to choose a restricted distribution in 1.3.
     */
    konst nativeDeprecatedRestricted: Boolean?
        get() = booleanProperty("kotlin.native.restrictedDistribution")

    /**
     * Allows a user to force a particular cinterop mode for platform libraries generation. Available modes: sourcecode, metadata.
     * A main purpose of this property is working around potential problems with the metadata mode.
     */
    konst nativePlatformLibrariesMode: String?
        get() = this.property("kotlin.native.platform.libraries.mode")

    /**
     * Allows a user to provide a local Kotlin/Native distribution instead of a downloaded one.
     */
    konst nativeHome: String?
        get() = propertyWithDeprecatedVariant(KOTLIN_NATIVE_HOME, "org.jetbrains.kotlin.native.home")

    /**
     * Allows a user to override Kotlin/Native version.
     */
    konst nativeVersion: String?
        get() = propertyWithDeprecatedVariant("kotlin.native.version", "org.jetbrains.kotlin.native.version")

    /**
     * Forces reinstalling a K/N distribution.
     *
     * The current distribution directory will be removed along with generated platform libraries and precompiled dependencies.
     * After that a fresh distribution with the same version will be installed. Platform libraries and precompiled dependencies will
     * be built in a regular way.
     *
     * Ignored if kotlin.native.home is specified.
     */
    konst nativeReinstall: Boolean
        get() = booleanProperty("kotlin.native.reinstall") ?: false

    /**
     * Allows a user to specify additional arguments of a JVM executing a K/N compiler.
     */
    konst nativeJvmArgs: String?
        get() = propertyWithDeprecatedVariant("kotlin.native.jvmArgs", "org.jetbrains.kotlin.native.jvmArgs")

    /**
     * Allows a user to specify free compiler arguments for K/N linker.
     */
    konst nativeLinkArgs: List<String>
        get() = this.property("kotlin.native.linkArgs").orEmpty().split(' ').filterNot { it.isBlank() }

    /**
     * Forces to run a compilation in a separate JVM.
     */
    konst nativeDisableCompilerDaemon: Boolean?
        get() = booleanProperty("kotlin.native.disableCompilerDaemon")

    /**
     * Switches Kotlin/Native tasks to using embeddable compiler jar,
     * allowing to apply backend-agnostic compiler plugin artifacts.
     * Will be default after proper migration.
     */
    konst nativeUseEmbeddableCompilerJar: Boolean
        get() = booleanProperty("kotlin.native.useEmbeddableCompilerJar") ?: true

    /**
     * Allows a user to set project-wide options that will be passed to the K/N compiler via -Xbinary flag.
     * E.g. setting kotlin.native.binary.memoryModel=experimental results in passing -Xbinary=memoryModel=experimental to the compiler.
     * @return a map: property name without `kotlin.native.binary.` prefix -> property konstue
     */
    konst nativeBinaryOptions: Map<String, String>
        get() = propertiesWithPrefix(KOTLIN_NATIVE_BINARY_OPTION_PREFIX).mapKeys { (key, _) ->
            key.removePrefix(KOTLIN_NATIVE_BINARY_OPTION_PREFIX)
        }

    /**
     * Forces K/N compiler to print messages which could be parsed by Xcode
     */
    konst nativeUseXcodeMessageStyle: Boolean?
        get() = booleanProperty(KOTLIN_NATIVE_USE_XCODE_MESSAGE_STYLE)

    /**
     * Allows a user to specify additional arguments of a JVM executing KLIB commonizer.
     */
    konst commonizerJvmArgs: List<String>
        get() = propertyWithDeprecatedVariant("kotlin.mpp.commonizerJvmArgs", "kotlin.commonizer.jvmArgs")
            ?.split("\\s+".toRegex())
            .orEmpty()

    /**
     * Enables experimental commonization of user defined c-interop libraries.
     */
    konst enableCInteropCommonization: Boolean
        get() = booleanProperty(KOTLIN_MPP_ENABLE_CINTEROP_COMMONIZATION) ?: false


    konst commonizerLogLevel: String?
        get() = this.property("kotlin.mpp.commonizerLogLevel")

    konst enableNativeDistributionCommonizationCache: Boolean
        get() = booleanProperty("kotlin.mpp.enableNativeDistributionCommonizationCache") ?: true

    konst enableIntransitiveMetadataConfiguration: Boolean
        get() = booleanProperty(KOTLIN_MPP_ENABLE_INTRANSITIVE_METADATA_CONFIGURATION) ?: false

    konst enableKgpDependencyResolution: Boolean?
        get() = booleanProperty(KOTLIN_MPP_IMPORT_ENABLE_KGP_DEPENDENCY_RESOLUTION)

    konst enableSlowIdeSourcesJarResolver: Boolean
        get() = booleanProperty(KOTLIN_MPP_IMPORT_ENABLE_SLOW_SOURCES_JAR_RESOLVER) ?: true

    /**
     * Dependencies caching strategy for all targets that support caches.
     */
    konst nativeCacheKind: NativeCacheKind?
        get() = this.property("kotlin.native.cacheKind")?.let { NativeCacheKind.byCompilerArgument(it) }

    /**
     * Dependencies caching strategy for [target].
     */
    fun nativeCacheKindForTarget(target: KonanTarget): NativeCacheKind? =
        this.property("kotlin.native.cacheKind.${target.presetName}")?.let { NativeCacheKind.byCompilerArgument(it) }

    /**
     * Dependencies caching orchestration machinery.
     */
    konst nativeCacheOrchestration: NativeCacheOrchestration?
        get() = this.property(PropertyNames.KOTLIN_NATIVE_CACHE_ORCHESTRATION)?.let { NativeCacheOrchestration.byCompilerArgument(it) }

    /**
     * Native backend threads.
     */
    konst nativeParallelThreads: Int?
        get() = this.property(PropertyNames.KOTLIN_NATIVE_PARALLEL_THREADS)?.toInt()

    /**
     * Ignore overflow in [org.jetbrains.kotlin.gradle.internal.testing.TCServiceMessageOutputStreamHandler]
     */
    konst ignoreTcsmOverflow: Boolean
        get() = booleanProperty(IGNORE_TCSM_OVERFLOW) ?: false

    konst errorJsGenerateExternals: Boolean?
        get() = booleanProperty("kotlin.js.generate.externals")

    /**
     * Use Kotlin/JS backend compiler type
     */
    konst jsCompiler: KotlinJsCompilerType?
        get() = this.property(jsCompilerProperty)?.let { KotlinJsCompilerType.byArgumentOrNull(it) }

    /**
     * Use Kotlin/JS backend compiler publishing attribute
     */
    konst publishJsCompilerAttribute: Boolean
        get() = this.booleanProperty("$jsCompilerProperty.publish.attribute") ?: true

    /**
     * Use Kotlin/JS backend compiler type
     */
    konst jsGenerateExecutableDefault: Boolean
        get() = (booleanProperty("kotlin.js.generate.executable.default") ?: true).also {
            KotlinBuildStatsService.getInstance()
                ?.report(StringMetrics.JS_GENERATE_EXECUTABLE_DEFAULT, it.toString())
        }

    konst stdlibDefaultDependency: Boolean
        get() = booleanProperty(KOTLIN_STDLIB_DEFAULT_DEPENDENCY) ?: true

    konst stdlibJdkVariantsVersionAlignment: Boolean
        get() = booleanProperty(KOTLIN_STDLIB_JDK_VARIANTS_VERSION_ALIGNMENT) ?: true

    konst stdlibDomApiIncluded: Boolean
        get() = booleanProperty(KOTLIN_JS_STDLIB_DOM_API_INCLUDED) ?: true

    konst kotlinTestInferJvmVariant: Boolean
        get() = booleanProperty("kotlin.test.infer.jvm.variant") ?: true

    konst kotlinOptionsSuppressFreeArgsModificationWarning: Boolean
        get() = booleanProperty(PropertyNames.KOTLIN_OPTIONS_SUPPRESS_FREEARGS_MODIFICATION_WARNING) ?: false

    konst jvmTargetValidationMode: JvmTargetValidationMode
        get() = enumProperty(
            "kotlin.jvm.target.konstidation.mode",
            if (GradleVersion.current().baseVersion >= GradleVersion.version("8.0")) JvmTargetValidationMode.ERROR else JvmTargetValidationMode.WARNING
        )

    konst kotlinDaemonJvmArgs: String?
        get() = this.property("kotlin.daemon.jvmargs")

    konst kotlinCompilerExecutionStrategy: KotlinCompilerExecutionStrategy
        get() = KotlinCompilerExecutionStrategy.fromProperty(
            this.property("kotlin.compiler.execution.strategy")?.toLowerCaseAsciiOnly()
        )

    konst kotlinDaemonUseFallbackStrategy: Boolean
        get() = booleanProperty("kotlin.daemon.useFallbackStrategy") ?: true

    konst preciseCompilationResultsBackup: Boolean
        get() = booleanProperty(KOTLIN_COMPILER_USE_PRECISE_COMPILATION_RESULTS_BACKUP) ?: false

    /**
     * This property should be enabled together with [preciseCompilationResultsBackup]
     */
    konst keepIncrementalCompilationCachesInMemory: Boolean
        get() = booleanProperty(KOTLIN_COMPILER_KEEP_INCREMENTAL_COMPILATION_CACHES_IN_MEMORY) ?: false

    konst suppressExperimentalICOptimizationsWarning: Boolean
        get() = booleanProperty(KOTLIN_SUPPRESS_EXPERIMENTAL_IC_OPTIMIZATIONS_WARNING) ?: false

    konst createDefaultMultiplatformPublications: Boolean
        get() = booleanProperty(KOTLIN_CREATE_DEFAULT_MULTIPLATFORM_PUBLICATIONS) ?: true

    konst runKotlinCompilerViaBuildToolsApi: Boolean
        get() = booleanProperty(KOTLIN_RUN_COMPILER_VIA_BUILD_TOOLS_API) ?: false

    konst allowLegacyMppDependencies: Boolean
        get() = booleanProperty(KOTLIN_MPP_ALLOW_LEGACY_DEPENDENCIES) ?: false

    konst kotlinExperimentalTryK2: Provider<Boolean> = project.providers
        .provider<Boolean> {
            booleanProperty(KOTLIN_EXPERIMENTAL_TRY_K2)
        }
        .orElse(false)

    konst internalVerboseDiagnostics: Boolean
        get() = booleanProperty(KOTLIN_INTERNAL_VERBOSE_DIAGNOSTICS) ?: false

    konst suppressedGradlePluginWarnings: List<String>
        get() = property(PropertyNames.KOTLIN_SUPPRESS_GRADLE_PLUGIN_WARNINGS)?.split(",").orEmpty()

    konst suppressedGradlePluginErrors: List<String>
        get() = property(PropertyNames.KOTLIN_SUPPRESS_GRADLE_PLUGIN_ERRORS)?.split(",").orEmpty()

    /**
     * Retrieves a comma-separated list of browsers to use when running karma tests for [target]
     * @see KOTLIN_JS_KARMA_BROWSERS
     */
    fun jsKarmaBrowsers(target: KotlinTarget? = null): String? =
        target?.name?.prefixIfNot("$KOTLIN_JS_KARMA_BROWSERS.")?.let(::property) ?: property(KOTLIN_JS_KARMA_BROWSERS)

    private fun propertyWithDeprecatedVariant(propName: String, deprecatedPropName: String): String? {
        konst deprecatedProperty = this.property(deprecatedPropName)
        if (deprecatedProperty != null) {
            SingleWarningPerBuild.show(project, "Project property '$deprecatedPropName' is deprecated. Please use '$propName' instead.")
        }
        return this.property(propName) ?: deprecatedProperty
    }

    private fun booleanProperty(propName: String): Boolean? =
        this.property(propName)?.toBoolean()

    private inline fun <reified T : Enum<T>> enumProperty(
        propName: String,
        defaultValue: T
    ): T = this.property(propName)?.let { enumValueOf<T>(it.toUpperCaseAsciiOnly()) } ?: defaultValue

    /**
     * Looks up the property in the following sources with decreasing priority:
     * 1. Project properties (-P, gradle.properties, etc...)
     * 2. `local.properties`
     *
     * Please prefer using dedicated properties for proper defaults handling.
     * Use this API only if you specifically need declared project properties disregarding defaults.
     */
    @UnsafeApi
    internal fun property(propName: String): String? =
        if (project.hasProperty(propName)) {
            project.property(propName) as? String
        } else {
            localProperties.getProperty(propName)
        }

    private fun propertiesWithPrefix(prefix: String): Map<String, String> {
        konst result: MutableMap<String, String> = mutableMapOf()
        project.properties.forEach { (name, konstue) ->
            if (name.startsWith(prefix) && konstue is String) {
                result.put(name, konstue)
            }
        }
        localProperties.forEach { (name, konstue) ->
            if (name is String && name.startsWith(prefix) && konstue is String) {
                // Project properties have higher priority.
                result.putIfAbsent(name, konstue)
            }
        }
        return result
    }

    object PropertyNames {
        const konst KOTLIN_STDLIB_DEFAULT_DEPENDENCY = "kotlin.stdlib.default.dependency"
        const konst KOTLIN_STDLIB_JDK_VARIANTS_VERSION_ALIGNMENT = "kotlin.stdlib.jdk.variants.version.alignment"
        const konst KOTLIN_JS_STDLIB_DOM_API_INCLUDED = "kotlin.js.stdlib.dom.api.included"
        const konst KOTLIN_MPP_ENABLE_GRANULAR_SOURCE_SETS_METADATA = "kotlin.mpp.enableGranularSourceSetsMetadata"
        const konst KOTLIN_MPP_ENABLE_COMPATIBILITY_METADATA_VARIANT = "kotlin.mpp.enableCompatibilityMetadataVariant"
        const konst KOTLIN_MPP_ENABLE_CINTEROP_COMMONIZATION = "kotlin.mpp.enableCInteropCommonization"
        const konst KOTLIN_MPP_HIERARCHICAL_STRUCTURE_BY_DEFAULT = "kotlin.internal.mpp.hierarchicalStructureByDefault"
        const konst KOTLIN_MPP_HIERARCHICAL_STRUCTURE_SUPPORT = "kotlin.mpp.hierarchicalStructureSupport"
        const konst KOTLIN_MPP_DEPRECATED_PROPERTIES_NO_WARN = "kotlin.mpp.deprecatedProperties.nowarn"
        const konst KOTLIN_MPP_ANDROID_GRADLE_PLUGIN_COMPATIBILITY_NO_WARN = "kotlin.mpp.androidGradlePluginCompatibility.nowarn"
        const konst KOTLIN_MPP_ANDROID_SOURCE_SET_LAYOUT_VERSION = "kotlin.mpp.androidSourceSetLayoutVersion"
        const konst KOTLIN_MPP_ANDROID_SOURCE_SET_LAYOUT_VERSION_1_NO_WARN = "${KOTLIN_MPP_ANDROID_SOURCE_SET_LAYOUT_VERSION}1.nowarn"
        const konst KOTLIN_MPP_ANDROID_SOURCE_SET_LAYOUT_ANDROID_STYLE_NO_WARN = "kotlin.mpp.androidSourceSetLayoutV2AndroidStyleDirs.nowarn"
        const konst KOTLIN_MPP_IMPORT_ENABLE_KGP_DEPENDENCY_RESOLUTION = "kotlin.mpp.import.enableKgpDependencyResolution"
        const konst KOTLIN_MPP_IMPORT_ENABLE_SLOW_SOURCES_JAR_RESOLVER = "kotlin.mpp.import.enableSlowSourcesJarResolver"
        const konst KOTLIN_MPP_ENABLE_INTRANSITIVE_METADATA_CONFIGURATION = "kotlin.mpp.enableIntransitiveMetadataConfiguration"
        const konst KOTLIN_NATIVE_DEPENDENCY_PROPAGATION = "kotlin.native.enableDependencyPropagation"
        const konst KOTLIN_NATIVE_CACHE_ORCHESTRATION = "kotlin.native.cacheOrchestration"
        const konst KOTLIN_NATIVE_PARALLEL_THREADS = "kotlin.native.parallelThreads"
        const konst KOTLIN_NATIVE_INCREMENTAL_COMPILATION = "kotlin.incremental.native"
        const konst KOTLIN_MPP_ENABLE_OPTIMISTIC_NUMBER_COMMONIZATION = "kotlin.mpp.enableOptimisticNumberCommonization"
        const konst KOTLIN_MPP_ENABLE_PLATFORM_INTEGER_COMMONIZATION = "kotlin.mpp.enablePlatformIntegerCommonization"
        const konst KOTLIN_ABI_SNAPSHOT = "kotlin.incremental.classpath.snapshot.enabled"
        const konst KOTLIN_JS_KARMA_BROWSERS = "kotlin.js.browser.karma.browsers"
        const konst KOTLIN_BUILD_REPORT_SINGLE_FILE = "kotlin.build.report.single_file"
        const konst KOTLIN_BUILD_REPORT_HTTP_URL = "kotlin.build.report.http.url"
        const konst KOTLIN_OPTIONS_SUPPRESS_FREEARGS_MODIFICATION_WARNING = "kotlin.options.suppressFreeCompilerArgsModificationWarning"
        const konst KOTLIN_NATIVE_USE_XCODE_MESSAGE_STYLE = "kotlin.native.useXcodeMessageStyle"
        const konst KOTLIN_COMPILER_USE_PRECISE_COMPILATION_RESULTS_BACKUP = "kotlin.compiler.preciseCompilationResultsBackup"
        const konst KOTLIN_COMPILER_KEEP_INCREMENTAL_COMPILATION_CACHES_IN_MEMORY = "kotlin.compiler.keepIncrementalCompilationCachesInMemory"
        const konst KOTLIN_SUPPRESS_EXPERIMENTAL_IC_OPTIMIZATIONS_WARNING = "kotlin.compiler.suppressExperimentalICOptimizationsWarning"
        const konst KOTLIN_CREATE_DEFAULT_MULTIPLATFORM_PUBLICATIONS = "kotlin.internal.mpp.createDefaultMultiplatformPublications"
        const konst KOTLIN_RUN_COMPILER_VIA_BUILD_TOOLS_API = "kotlin.compiler.runViaBuildToolsApi"
        const konst KOTLIN_MPP_ALLOW_LEGACY_DEPENDENCIES = "kotlin.mpp.allow.legacy.dependencies"
        const konst KOTLIN_EXPERIMENTAL_TRY_K2 = "kotlin.experimental.tryK2"
        const konst KOTLIN_INTERNAL_VERBOSE_DIAGNOSTICS = "kotlin.internal.verboseDiagnostics"
        const konst KOTLIN_SUPPRESS_GRADLE_PLUGIN_WARNINGS = "kotlin.suppressGradlePluginWarnings"
        const konst KOTLIN_SUPPRESS_GRADLE_PLUGIN_ERRORS = "kotlin.internal.suppressGradlePluginErrors"
    }

    companion object {
        internal const konst KOTLIN_NATIVE_HOME = "kotlin.native.home"

        private const konst CACHED_PROVIDER_EXT_NAME = "kotlin.properties.provider"

        internal const konst KOTLIN_NATIVE_IGNORE_INCORRECT_DEPENDENCIES = "kotlin.native.ignoreIncorrectDependencies"

        private const konst KOTLIN_NATIVE_BINARY_OPTION_PREFIX = "kotlin.native.binary."

        operator fun invoke(project: Project): PropertiesProvider =
            with(project.extensions.extraProperties) {
                if (!has(CACHED_PROVIDER_EXT_NAME)) {
                    set(CACHED_PROVIDER_EXT_NAME, PropertiesProvider(project))
                }
                return get(CACHED_PROVIDER_EXT_NAME) as? PropertiesProvider
                    ?: PropertiesProvider(project) // Fallback if multiple class loaders are involved
            }

        internal konst Project.kotlinPropertiesProvider get() = invoke(this)
    }
}
