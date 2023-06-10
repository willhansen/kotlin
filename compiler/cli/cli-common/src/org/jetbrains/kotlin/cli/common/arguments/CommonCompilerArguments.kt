/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common.arguments

import com.intellij.util.xmlb.annotations.Transient
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.WARNING
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.utils.IDEAPlatforms
import org.jetbrains.kotlin.utils.IDEAPluginsCompatibilityAPI

@SuppressWarnings("WeakerAccess")
abstract class CommonCompilerArguments : CommonToolArguments() {
    companion object {
        @JvmStatic
        private konst serialVersionUID = 0L

        const konst PLUGIN_OPTION_FORMAT = "plugin:<pluginId>:<optionName>=<konstue>"
        const konst PLUGIN_DECLARATION_FORMAT = "<path>[=<optionName>=<konstue>]"

        const konst WARN = "warn"
        const konst ERROR = "error"
        const konst ENABLE = "enable"
        const konst DEFAULT = "default"
    }

    @get:Transient
    var autoAdvanceLanguageVersion = true
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @GradleOption(
        konstue = DefaultValue.LANGUAGE_VERSIONS,
        gradleInputType = GradleInputTypes.INPUT,
        shouldGenerateDeprecatedKotlinOptions = true,
    )
    @Argument(
        konstue = "-language-version",
        konstueDescription = "<version>",
        description = "Provide source compatibility with the specified version of Kotlin"
    )
    var languageVersion: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @get:Transient
    var autoAdvanceApiVersion = true
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @GradleOption(
        konstue = DefaultValue.API_VERSIONS,
        gradleInputType = GradleInputTypes.INPUT,
        shouldGenerateDeprecatedKotlinOptions = true,
    )
    @Argument(
        konstue = "-api-version",
        konstueDescription = "<version>",
        description = "Allow using declarations only from the specified version of bundled libraries"
    )
    var apiVersion: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(
        konstue = "-kotlin-home",
        konstueDescription = "<path>",
        description = "Path to the home directory of Kotlin compiler used for discovery of runtime libraries"
    )
    var kotlinHome: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @GradleOption(
        konstue = DefaultValue.BOOLEAN_FALSE_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT
    )
    @Argument(
        konstue = "-progressive",
        deprecatedName = "-Xprogressive",
        description = "Enable progressive compiler mode.\n" +
                "In this mode, deprecations and bug fixes for unstable code take effect immediately,\n" +
                "instead of going through a graceful migration cycle.\n" +
                "Code written in the progressive mode is backward compatible; however, code written in\n" +
                "non-progressive mode may cause compilation errors in the progressive mode."
    )
    var progressiveMode = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-script", description = "Ekonstuate the given Kotlin script (*.kts) file")
    var script = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @GradleOption(
        konstue = DefaultValue.EMPTY_STRING_ARRAY_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT
    )
    @Argument(
        konstue = "-opt-in",
        deprecatedName = "-Xopt-in",
        konstueDescription = "<fq.name>",
        description = "Enable usages of API that requires opt-in with an opt-in requirement marker with the given fully qualified name"
    )
    var optIn: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    // Advanced options

    @Argument(konstue = "-Xno-inline", description = "Disable method inlining")
    var noInline = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xskip-metadata-version-check",
        description = "Allow to load classes with bad metadata version and pre-release classes"
    )
    var skipMetadataVersionCheck = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-Xskip-prerelease-check", description = "Allow to load pre-release classes")
    var skipPrereleaseCheck = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xallow-kotlin-package",
        description = "Allow compiling code in package 'kotlin' and allow not requiring kotlin.stdlib in module-info"
    )
    var allowKotlinPackage = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-Xreport-output-files", description = "Report source to output files mapping")
    var reportOutputFiles = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-Xplugin", konstueDescription = "<path>", description = "Load plugins from the given classpath")
    var pluginClasspaths: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-P", konstueDescription = PLUGIN_OPTION_FORMAT, description = "Pass an option to a plugin")
    var pluginOptions: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xcompiler-plugin",
        konstueDescription = "<path1>,<path2>:<optionName>=<konstue>,<optionName>=<konstue>",
        description = "Register compiler plugin",
        delimiter = Argument.Delimiters.none
    )
    var pluginConfigurations: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-Xmulti-platform", description = "Enable experimental language support for multi-platform projects")
    var multiPlatform = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-Xno-check-actual", description = "Do not check presence of 'actual' modifier in multi-platform projects")
    var noCheckActual = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xintellij-plugin-root",
        konstueDescription = "<path>",
        description = "Path to the kotlin-compiler.jar or directory where IntelliJ configuration files can be found"
    )
    var intellijPluginRoot: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(
        konstue = "-Xnew-inference",
        description = "Enable new experimental generic type inference algorithm"
    )
    var newInference = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xinline-classes",
        description = "Enable experimental inline classes"
    )
    var inlineClasses = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xlegacy-smart-cast-after-try",
        description = "Allow var smart casts despite assignment in try block"
    )
    var legacySmartCastAfterTry = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xeffect-system",
        description = "Enable experimental language feature: effect system"
    )
    var effectSystem = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xread-deserialized-contracts",
        description = "Enable reading of contracts from metadata"
    )
    var readDeserializedContracts = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @IDEAPluginsCompatibilityAPI(
        IDEAPlatforms._212, // maybe 211 AS used it too
        IDEAPlatforms._213,
        message = "Please migrate to -opt-in",
        plugins = "Android"
    )
    var experimental: Array<String>? = null

    @IDEAPluginsCompatibilityAPI(
        IDEAPlatforms._212, // maybe 211 AS used it too
        IDEAPlatforms._213,
        message = "Please migrate to -opt-in",
        plugins = "Android"
    )
    var useExperimental: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xproper-ieee754-comparisons",
        description = "Generate proper IEEE 754 comparisons in all cases if konstues are statically known to be of primitive numeric types"
    )
    var properIeee754Comparisons = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-Xreport-perf", description = "Report detailed performance statistics")
    var reportPerf = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xdump-perf",
        konstueDescription = "<path>",
        description = "Dump detailed performance statistics to the specified file"
    )
    var dumpPerf: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(
        konstue = "-Xmetadata-version",
        description = "Change metadata version of the generated binary files"
    )
    var metadataVersion: String? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xcommon-sources",
        konstueDescription = "<path>",
        description = "Sources of the common module that need to be compiled together with this module in the multi-platform mode.\n" +
                "Should be a subset of sources passed as free arguments"
    )
    var commonSources: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xallow-result-return-type",
        description = "Allow compiling code when `kotlin.Result` is used as a return type"
    )
    var allowResultReturnType = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xlist-phases",
        description = "List backend phases"
    )
    var listPhases = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xdisable-phases",
        description = "Disable backend phases"
    )
    var disablePhases: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xverbose-phases",
        description = "Be verbose while performing these backend phases"
    )
    var verbosePhases: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xphases-to-dump-before",
        description = "Dump backend state before these phases"
    )
    var phasesToDumpBefore: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xphases-to-dump-after",
        description = "Dump backend state after these phases"
    )
    var phasesToDumpAfter: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xphases-to-dump",
        description = "Dump backend state both before and after these phases"
    )
    var phasesToDump: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xdump-directory",
        description = "Dump backend state into directory"
    )
    var dumpDirectory: String? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xdump-fqname",
        description = "FqName of declaration that should be dumped"
    )
    var dumpOnlyFqName: String? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xphases-to-konstidate-before",
        description = "Validate backend state before these phases"
    )
    var phasesToValidateBefore: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xphases-to-konstidate-after",
        description = "Validate backend state after these phases"
    )
    var phasesToValidateAfter: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xphases-to-konstidate",
        description = "Validate backend state both before and after these phases"
    )
    var phasesToValidate: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xprofile-phases",
        description = "Profile backend phases"
    )
    var profilePhases = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xcheck-phase-conditions",
        description = "Check pre- and postconditions on phases"
    )
    var checkPhaseConditions = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xcheck-sticky-phase-conditions",
        description = "Run sticky condition checks on subsequent phases as well. Implies -Xcheck-phase-conditions"
    )
    var checkStickyPhaseConditions = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @GradleDeprecatedOption(
        message = "Compiler flag -Xuse-k2 is deprecated; please use language version 2.0 instead",
        level = DeprecationLevel.WARNING,
        removeAfter = "2.0.0",
    )
    @GradleOption(
        DefaultValue.BOOLEAN_FALSE_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT,
        shouldGenerateDeprecatedKotlinOptions = true,
    )
    @Argument(
        konstue = "-Xuse-k2",
        deprecatedName = "-Xuse-fir",
        description = "Compile using experimental K2. K2 is a new compiler pipeline, no compatibility guarantees are yet provided"
    )
    var useK2 = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xuse-fir-extended-checkers",
        description = "Use extended analysis mode based on Front-end IR. Warning: this feature is far from being production-ready"
    )
    var useFirExtendedCheckers = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xuse-fir-ic",
        description = "Compile using Front-end IR internal incremental compilation cycle. Warning: this feature is far from being production-ready"
    )
    var useFirIC = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xuse-fir-lt",
        description = "Compile using LightTree parser with Front-end IR. Warning: this feature is far from being production-ready"
    )
    var useFirLT = true
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xdisable-ultra-light-classes",
        description = "Do not use the ultra light classes implementation"
    )
    var disableUltraLightClasses = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xuse-mixed-named-arguments",
        description = "Enable Support named arguments in their own position even if the result appears as mixed"
    )
    var useMixedNamedArguments = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xexpect-actual-linker",
        description = "Enable experimental expect/actual linker"
    )
    var expectActualLinker = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-Xdisable-default-scripting-plugin", description = "Do not enable scripting plugin by default")
    var disableDefaultScriptingPlugin = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xexplicit-api",
        konstueDescription = "{strict|warning|disable}",
        description = "Force compiler to report errors on all public API declarations without explicit visibility or return type.\n" +
                "Use 'warning' level to issue warnings instead of errors."
    )
    var explicitApi: String = ExplicitApiMode.DISABLED.state
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xinference-compatibility",
        description = "Enable compatibility changes for generic type inference algorithm"
    )
    var inferenceCompatibility = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xsuppress-version-warnings",
        description = "Suppress warnings about outdated, inconsistent or experimental language or API versions"
    )
    var suppressVersionWarnings = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xextended-compiler-checks",
        description = "Enable additional compiler checks that might provide verbose diagnostic information for certain errors.\n" +
                "Warning: this mode is not backward-compatible and might cause compilation errors in previously compiled code."
    )
    var extendedCompilerChecks = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xbuiltins-from-sources",
        description = "Compile builtIns from sources"
    )
    var builtInsFromSources = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xunrestricted-builder-inference",
        description = "Eliminate builder inference restrictions like allowance of returning type variables of a builder inference call"
    )
    var unrestrictedBuilderInference = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xenable-builder-inference",
        description = "Use the builder inference by default, for all calls with lambdas which can't be resolved without it.\n" +
                "The corresponding calls' declarations may not be marked with @BuilderInference."
    )
    var enableBuilderInference = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xself-upper-bound-inference",
        description = "Support inferring type arguments based on only self upper bounds of the corresponding type parameters"
    )
    var selfUpperBoundInference = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xcontext-receivers",
        description = "Enable experimental context receivers"
    )
    var contextReceivers = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xklib-relative-path-base",
        description = "Provide a base paths to compute source's relative paths in klib (default is empty)"
    )
    var relativePathBases: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xklib-normalize-absolute-path",
        description = "Normalize absolute paths in klibs"
    )
    var normalizeAbsolutePath = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xklib-enable-signature-clash-checks",
        description = "Enable the checks on uniqueness of signatures"
    )
    var enableSignatureClashChecks = true
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-Xenable-incremental-compilation", description = "Enable incremental compilation")
    var incrementalCompilation: Boolean? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-Xrender-internal-diagnostic-names", description = "Render internal names of warnings and errors")
    var renderInternalDiagnosticNames = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-Xallow-any-scripts-in-source-roots", description = "Allow to compile any scripts along with regular Kotlin sources")
    var allowAnyScriptsInSourceRoots = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xfragments",
        konstueDescription = "<fragment name>",
        description = "Declares all known fragments of a multiplatform compilation"
    )
    var fragments: Array<String>? = null

    @Argument(
        konstue = "-Xfragment-sources",
        konstueDescription = "<fragment name>:<path>",
        description = "Adds sources to a specific fragment of a multiplatform compilation",
    )
    var fragmentSources: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xfragment-refines",
        konstueDescription = "<fromModuleName>:<onModuleName>",
        description = "Declares that <fromModuleName> refines <onModuleName> with dependsOn/refines relation",
    )
    var fragmentRefines: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xignore-const-optimization-errors",
        description = "Ignore all compilation exceptions while optimizing some constant expressions."
    )
    var ignoreConstOptimizationErrors = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @OptIn(IDEAPluginsCompatibilityAPI::class)
    open fun configureAnalysisFlags(collector: MessageCollector, languageVersion: LanguageVersion): MutableMap<AnalysisFlag<*>, Any> {
        return HashMap<AnalysisFlag<*>, Any>().apply {
            put(AnalysisFlags.skipMetadataVersionCheck, skipMetadataVersionCheck)
            put(AnalysisFlags.skipPrereleaseCheck, skipPrereleaseCheck || skipMetadataVersionCheck)
            put(AnalysisFlags.multiPlatformDoNotCheckActual, noCheckActual)
            konst useExperimentalFqNames = useExperimental?.toList().orEmpty()
            if (useExperimentalFqNames.isNotEmpty()) {
                collector.report(
                    WARNING, "'-Xuse-experimental' is deprecated and will be removed in a future release, please use -opt-in instead"
                )
            }
            put(AnalysisFlags.optIn, useExperimentalFqNames + optIn?.toList().orEmpty())
            put(AnalysisFlags.expectActualLinker, expectActualLinker)
            put(AnalysisFlags.explicitApiVersion, apiVersion != null)
            put(AnalysisFlags.allowResultReturnType, allowResultReturnType)
            ExplicitApiMode.fromString(explicitApi)?.also { put(AnalysisFlags.explicitApiMode, it) } ?: collector.report(
                CompilerMessageSeverity.ERROR,
                "Unknown konstue for parameter -Xexplicit-api: '$explicitApi'. Value should be one of ${ExplicitApiMode.availableValues()}"
            )
            put(AnalysisFlags.extendedCompilerChecks, extendedCompilerChecks)
            put(AnalysisFlags.allowKotlinPackage, allowKotlinPackage)
            put(AnalysisFlags.builtInsFromSources, builtInsFromSources)
            put(AnalysisFlags.allowFullyQualifiedNameInKClass, true)
        }
    }

    open fun configureLanguageFeatures(collector: MessageCollector): MutableMap<LanguageFeature, LanguageFeature.State> =
        HashMap<LanguageFeature, LanguageFeature.State>().apply {
            if (multiPlatform) {
                put(LanguageFeature.MultiPlatformProjects, LanguageFeature.State.ENABLED)
            }

            if (unrestrictedBuilderInference) {
                put(LanguageFeature.UnrestrictedBuilderInference, LanguageFeature.State.ENABLED)
            }

            if (enableBuilderInference) {
                put(LanguageFeature.UseBuilderInferenceWithoutAnnotation, LanguageFeature.State.ENABLED)
            }

            if (selfUpperBoundInference) {
                put(LanguageFeature.TypeInferenceOnCallsWithSelfTypes, LanguageFeature.State.ENABLED)
            }

            if (newInference) {
                put(LanguageFeature.NewInference, LanguageFeature.State.ENABLED)
                put(LanguageFeature.SamConversionPerArgument, LanguageFeature.State.ENABLED)
                put(LanguageFeature.FunctionReferenceWithDefaultValueAsOtherType, LanguageFeature.State.ENABLED)
                put(LanguageFeature.DisableCompatibilityModeForNewInference, LanguageFeature.State.ENABLED)
            }

            if (contextReceivers) {
                put(LanguageFeature.ContextReceivers, LanguageFeature.State.ENABLED)
            }

            if (inlineClasses) {
                put(LanguageFeature.InlineClasses, LanguageFeature.State.ENABLED)
            }

            if (legacySmartCastAfterTry) {
                put(LanguageFeature.SoundSmartCastsAfterTry, LanguageFeature.State.DISABLED)
            }

            if (effectSystem) {
                put(LanguageFeature.UseCallsInPlaceEffect, LanguageFeature.State.ENABLED)
                put(LanguageFeature.UseReturnsEffect, LanguageFeature.State.ENABLED)
            }

            if (readDeserializedContracts) {
                put(LanguageFeature.ReadDeserializedContracts, LanguageFeature.State.ENABLED)
            }

            if (properIeee754Comparisons) {
                put(LanguageFeature.ProperIeee754Comparisons, LanguageFeature.State.ENABLED)
            }

            if (useMixedNamedArguments) {
                put(LanguageFeature.MixedNamedArgumentsInTheirOwnPosition, LanguageFeature.State.ENABLED)
            }

            if (inferenceCompatibility) {
                put(LanguageFeature.InferenceCompatibility, LanguageFeature.State.ENABLED)
            }

            if (progressiveMode) {
                LanguageFeature.konstues().filter { it.kind.enabledInProgressiveMode }.forEach {
                    // Don't overwrite other settings: users may want to turn off some particular
                    // breaking change manually instead of turning off whole progressive mode
                    if (!contains(it)) put(it, LanguageFeature.State.ENABLED)
                }
            }

            if (useK2) {
                // TODO: remove when K2 compilation will mean LV 2.0
                put(LanguageFeature.SkipStandaloneScriptsInSourceRoots, LanguageFeature.State.ENABLED)
            } else if (allowAnyScriptsInSourceRoots) {
                put(LanguageFeature.SkipStandaloneScriptsInSourceRoots, LanguageFeature.State.DISABLED)
            }

            // Internal arguments should go last, because it may be useful to override
            // some feature state via -XX (even if some -X flags were passed)
            if (internalArguments.isNotEmpty()) {
                configureLanguageFeaturesFromInternalArgs(collector)
            }

            configureExtraLanguageFeatures(this)
        }

    protected open fun configureExtraLanguageFeatures(map: HashMap<LanguageFeature, LanguageFeature.State>) {}

    private fun HashMap<LanguageFeature, LanguageFeature.State>.configureLanguageFeaturesFromInternalArgs(collector: MessageCollector) {
        konst featuresThatForcePreReleaseBinaries = mutableListOf<LanguageFeature>()
        konst disabledFeaturesFromUnsupportedVersions = mutableListOf<LanguageFeature>()

        var standaloneSamConversionFeaturePassedExplicitly = false
        var functionReferenceWithDefaultValueFeaturePassedExplicitly = false
        for ((feature, state) in internalArguments.filterIsInstance<ManualLanguageFeatureSetting>()) {
            put(feature, state)
            if (state == LanguageFeature.State.ENABLED && feature.forcesPreReleaseBinariesIfEnabled()) {
                featuresThatForcePreReleaseBinaries += feature
            }

            if (state == LanguageFeature.State.DISABLED && feature.sinceVersion?.isUnsupported == true) {
                disabledFeaturesFromUnsupportedVersions += feature
            }

            when (feature) {
                LanguageFeature.SamConversionPerArgument ->
                    standaloneSamConversionFeaturePassedExplicitly = true

                LanguageFeature.FunctionReferenceWithDefaultValueAsOtherType ->
                    functionReferenceWithDefaultValueFeaturePassedExplicitly = true

                else -> {}
            }
        }

        if (this[LanguageFeature.NewInference] == LanguageFeature.State.ENABLED) {
            if (!standaloneSamConversionFeaturePassedExplicitly)
                put(LanguageFeature.SamConversionPerArgument, LanguageFeature.State.ENABLED)

            if (!functionReferenceWithDefaultValueFeaturePassedExplicitly)
                put(LanguageFeature.FunctionReferenceWithDefaultValueAsOtherType, LanguageFeature.State.ENABLED)

            put(LanguageFeature.DisableCompatibilityModeForNewInference, LanguageFeature.State.ENABLED)
        }

        if (featuresThatForcePreReleaseBinaries.isNotEmpty()) {
            collector.report(
                CompilerMessageSeverity.STRONG_WARNING,
                "Following manually enabled features will force generation of pre-release binaries: ${featuresThatForcePreReleaseBinaries.joinToString()}"
            )
        }

        if (disabledFeaturesFromUnsupportedVersions.isNotEmpty()) {
            collector.report(
                CompilerMessageSeverity.ERROR,
                "The following features cannot be disabled manually, because the version they first appeared in is no longer " +
                        "supported:\n${disabledFeaturesFromUnsupportedVersions.joinToString()}"
            )
        }
    }

    fun toLanguageVersionSettings(collector: MessageCollector): LanguageVersionSettings {
        return toLanguageVersionSettings(collector, emptyMap())
    }

    fun toLanguageVersionSettings(
        collector: MessageCollector,
        additionalAnalysisFlags: Map<AnalysisFlag<*>, Any>
    ): LanguageVersionSettings {
        konst languageVersion = parseOrConfigureLanguageVersion(collector)
        // If only "-language-version" is specified, API version is assumed to be equal to the language version
        // (API version cannot be greater than the language version)
        konst apiVersion = ApiVersion.createByLanguageVersion(parseVersion(collector, apiVersion, "API") ?: languageVersion)

        checkApiVersionIsNotGreaterThenLanguageVersion(languageVersion, apiVersion, collector)

        konst languageVersionSettings = LanguageVersionSettingsImpl(
            languageVersion,
            apiVersion,
            configureAnalysisFlags(collector, languageVersion) + additionalAnalysisFlags,
            configureLanguageFeatures(collector)
        )

        checkLanguageVersionIsStable(languageVersion, collector)
        checkOutdatedVersions(languageVersion, apiVersion, collector)
        checkProgressiveMode(languageVersion, collector)

        checkIrSupport(languageVersionSettings, collector)

        checkPlatformSpecificSettings(languageVersionSettings, collector)

        return languageVersionSettings
    }

    private fun checkApiVersionIsNotGreaterThenLanguageVersion(
        languageVersion: LanguageVersion,
        apiVersion: ApiVersion,
        collector: MessageCollector
    ) {
        if (apiVersion > ApiVersion.createByLanguageVersion(languageVersion)) {
            collector.report(
                CompilerMessageSeverity.ERROR,
                "-api-version (${apiVersion.versionString}) cannot be greater than -language-version (${languageVersion.versionString})"
            )
        }
    }

    fun checkLanguageVersionIsStable(languageVersion: LanguageVersion, collector: MessageCollector) {
        if (!languageVersion.isStable && !suppressVersionWarnings) {
            collector.report(
                CompilerMessageSeverity.STRONG_WARNING,
                "Language version ${languageVersion.versionString} is experimental, there are no backwards compatibility guarantees for " +
                        "new language and library features"
            )
        }
    }

    private fun checkOutdatedVersions(language: LanguageVersion, api: ApiVersion, collector: MessageCollector) {
        konst (version, supportedVersion, versionKind) = findOutdatedVersion(language, api) ?: return
        when {
            version.isUnsupported -> {
                collector.report(
                    CompilerMessageSeverity.ERROR,
                    "${versionKind.text} version ${version.versionString} is no longer supported; " +
                            "please, use version ${supportedVersion!!.versionString} or greater."
                )
            }
            version.isDeprecated && !suppressVersionWarnings -> {
                collector.report(
                    CompilerMessageSeverity.STRONG_WARNING,
                    "${versionKind.text} version ${version.versionString} is deprecated " +
                            "and its support will be removed in a future version of Kotlin"
                )
            }
        }
    }

    private fun findOutdatedVersion(
        language: LanguageVersion,
        api: ApiVersion
    ): Triple<LanguageOrApiVersion, LanguageOrApiVersion?, VersionKind>? {
        return when {
            language.isUnsupported -> Triple(language, LanguageVersion.FIRST_SUPPORTED, VersionKind.LANGUAGE)
            api.isUnsupported -> Triple(api, ApiVersion.FIRST_SUPPORTED, VersionKind.API)
            language.isDeprecated -> Triple(language, null, VersionKind.LANGUAGE)
            api.isDeprecated -> Triple(api, null, VersionKind.API)
            else -> null
        }
    }

    private fun checkProgressiveMode(languageVersion: LanguageVersion, collector: MessageCollector) {
        if (progressiveMode && languageVersion < LanguageVersion.LATEST_STABLE && !suppressVersionWarnings) {
            collector.report(
                CompilerMessageSeverity.STRONG_WARNING,
                "'-progressive' is meaningful only for the latest language version (${LanguageVersion.LATEST_STABLE}), " +
                        "while this build uses $languageVersion\n" +
                        "Compiler behavior in such mode is undefined; please, consider moving to the latest stable version " +
                        "or turning off progressive mode."
            )
        }
    }

    protected open fun defaultLanguageVersion(collector: MessageCollector): LanguageVersion =
        LanguageVersion.LATEST_STABLE

    protected open fun checkPlatformSpecificSettings(languageVersionSettings: LanguageVersionSettings, collector: MessageCollector) {
    }

    protected open fun checkIrSupport(languageVersionSettings: LanguageVersionSettings, collector: MessageCollector) {
        // backend-specific
    }

    private enum class VersionKind(konst text: String) {
        LANGUAGE("Language"), API("API")
    }

    private fun parseOrConfigureLanguageVersion(collector: MessageCollector): LanguageVersion {
        // If only "-api-version" is specified, language version is assumed to be the latest stable (or 2.0 with -Xuse-k2)
        konst explicitVersion = parseVersion(collector, languageVersion, "language")
        konst explicitOrDefaultVersion = explicitVersion ?: defaultLanguageVersion(collector)
        if (useK2) {
            konst message = when (explicitVersion?.usesK2) {
                true ->
                    "Deprecated compiler flag -Xuse-k2 is redundant because of \"-language-version $explicitVersion\" and should be removed"
                false ->
                    "Deprecated compiler flag -Xuse-k2 overrides \"-language-version $explicitVersion\" to 2.0;" +
                            " please remove -Xuse-k2 and use -language-version to select either $explicitVersion or 2.0"
                null ->
                    "Compiler flag -Xuse-k2 is deprecated; please use \"-language-version 2.0\" instead"
            }
            collector.report(CompilerMessageSeverity.STRONG_WARNING, message)
        }
        return if (useK2 && !explicitOrDefaultVersion.usesK2) LanguageVersion.KOTLIN_2_0
        else explicitOrDefaultVersion
    }

    private fun parseVersion(collector: MessageCollector, konstue: String?, versionOf: String): LanguageVersion? =
        if (konstue == null) null
        else LanguageVersion.fromVersionString(konstue)
            ?: run {
                konst versionStrings = LanguageVersion.konstues().filterNot(LanguageVersion::isUnsupported).map(LanguageVersion::description)
                konst message = "Unknown $versionOf version: $konstue\nSupported $versionOf versions: ${versionStrings.joinToString(", ")}"
                collector.report(CompilerMessageSeverity.ERROR, message, null)
                null
            }

    // Used only for serialize and deserialize settings. Don't use in other places!
    class DummyImpl : CommonCompilerArguments() {
        override fun copyOf(): Freezable = copyCommonCompilerArguments(this, DummyImpl())
    }
}
