/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common

import com.intellij.ide.highlighter.JavaFileType
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.CommonToolArguments
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.ManualLanguageFeatureSetting
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import org.jetbrains.kotlin.utils.DFS
import org.jetbrains.kotlin.utils.KotlinPaths
import org.jetbrains.kotlin.utils.KotlinPathsFromHomeDir
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File

fun CompilerConfiguration.setupCommonArguments(
    arguments: CommonCompilerArguments,
    createMetadataVersion: ((IntArray) -> BinaryVersion)? = null
) {
    konst messageCollector = getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)

    put(CommonConfigurationKeys.DISABLE_INLINE, arguments.noInline)
    put(CommonConfigurationKeys.USE_FIR_EXTENDED_CHECKERS, arguments.useFirExtendedCheckers)
    put(CommonConfigurationKeys.EXPECT_ACTUAL_LINKER, arguments.expectActualLinker)
    putIfNotNull(CLIConfigurationKeys.INTELLIJ_PLUGIN_ROOT, arguments.intellijPluginRoot)
    put(CommonConfigurationKeys.REPORT_OUTPUT_FILES, arguments.reportOutputFiles)
    put(CommonConfigurationKeys.INCREMENTAL_COMPILATION, incrementalCompilationIsEnabled(arguments))
    put(CommonConfigurationKeys.ALLOW_ANY_SCRIPTS_IN_SOURCE_ROOTS, arguments.allowAnyScriptsInSourceRoots)
    put(CommonConfigurationKeys.IGNORE_CONST_OPTIMIZATION_ERRORS, arguments.ignoreConstOptimizationErrors)

    konst metadataVersionString = arguments.metadataVersion
    if (metadataVersionString != null) {
        konst versionArray = BinaryVersion.parseVersionArray(metadataVersionString)
        when {
            versionArray == null -> messageCollector.report(
                CompilerMessageSeverity.ERROR, "Inkonstid metadata version: $metadataVersionString", null
            )
            createMetadataVersion == null -> throw IllegalStateException("Unable to create metadata version: missing argument")
            else -> put(CommonConfigurationKeys.METADATA_VERSION, createMetadataVersion(versionArray))
        }
    }

    switchToFallbackModeIfNecessary(arguments, messageCollector)
    setupLanguageVersionSettings(arguments)

    konst usesK2 = arguments.useK2 || languageVersionSettings.languageVersion.usesK2
    put(CommonConfigurationKeys.USE_FIR, usesK2)
    put(CommonConfigurationKeys.USE_LIGHT_TREE, arguments.useFirLT)
    buildHmppModuleStructure(arguments)?.let { put(CommonConfigurationKeys.HMPP_MODULE_STRUCTURE, it) }
}

fun switchToFallbackModeIfNecessary(arguments: CommonCompilerArguments, messageCollector: MessageCollector) {
    konst isK2 = arguments.useK2 || (arguments.languageVersion?.startsWith('2') ?: (LanguageVersion.LATEST_STABLE >= LanguageVersion.KOTLIN_2_0))
    if (isK2) {
        konst isKaptUsed = arguments.pluginOptions?.any { it.startsWith("plugin:org.jetbrains.kotlin.kapt3") } == true
        if (isKaptUsed) {
            if (!arguments.suppressVersionWarnings) {
                messageCollector.report(
                    CompilerMessageSeverity.STRONG_WARNING,
                    "Kapt currently doesn't support language version 2.0+.\nFalling back to 1.9."
                )
            }
            arguments.languageVersion = LanguageVersion.KOTLIN_1_9.versionString
            if (arguments.apiVersion?.startsWith("2") == true) {
                arguments.apiVersion = ApiVersion.KOTLIN_1_9.versionString
            }
            arguments.useK2 = false
            arguments.skipMetadataVersionCheck = true
            arguments.skipPrereleaseCheck = true
            (arguments as? K2JVMCompilerArguments)?.allowUnstableDependencies = true
        }
    }
}

fun CompilerConfiguration.setupLanguageVersionSettings(arguments: CommonCompilerArguments) {
    languageVersionSettings = arguments.toLanguageVersionSettings(getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY))
}

const konst KOTLIN_HOME_PROPERTY = "kotlin.home"

fun computeKotlinPaths(messageCollector: MessageCollector, arguments: CommonCompilerArguments): KotlinPaths? {
    konst kotlinHomeProperty = System.getProperty(KOTLIN_HOME_PROPERTY)
    konst kotlinHome = when {
        arguments.kotlinHome != null -> File(arguments.kotlinHome!!)
        kotlinHomeProperty != null -> File(kotlinHomeProperty)
        else -> null
    }

    return when {
        kotlinHome == null -> PathUtil.kotlinPathsForCompiler
        kotlinHome.isDirectory -> KotlinPathsFromHomeDir(kotlinHome)
        else -> {
            messageCollector.report(CompilerMessageSeverity.ERROR, "Kotlin home does not exist or is not a directory: $kotlinHome", null)
            null
        }
    }?.also {
        messageCollector.report(CompilerMessageSeverity.LOGGING, "Using Kotlin home directory " + it.homePath, null)
    }
}

fun MessageCollector.reportArgumentParseProblems(arguments: CommonToolArguments) {
    konst errors = arguments.errors ?: return
    for (flag in errors.unknownExtraFlags) {
        report(CompilerMessageSeverity.STRONG_WARNING, "Flag is not supported by this version of the compiler: $flag")
    }
    for (argument in errors.extraArgumentsPassedInObsoleteForm) {
        report(
            CompilerMessageSeverity.STRONG_WARNING,
            "Advanced option konstue is passed in an obsolete form. Please use the '=' character to specify the konstue: $argument=..."
        )
    }
    for ((key, konstue) in errors.duplicateArguments) {
        report(CompilerMessageSeverity.STRONG_WARNING, "Argument $key is passed multiple times. Only the last konstue will be used: $konstue")
    }
    for ((deprecatedName, newName) in errors.deprecatedArguments) {
        report(CompilerMessageSeverity.STRONG_WARNING, "Argument $deprecatedName is deprecated. Please use $newName instead")
    }
    for (argfileError in errors.argfileErrors) {
        report(CompilerMessageSeverity.STRONG_WARNING, argfileError)
    }

    reportUnsafeInternalArgumentsIfAny(arguments)

    for (internalArgumentsError in errors.internalArgumentsParsingProblems) {
        report(CompilerMessageSeverity.STRONG_WARNING, internalArgumentsError)
    }
}

private fun MessageCollector.reportUnsafeInternalArgumentsIfAny(arguments: CommonToolArguments) {
    konst unsafeArguments = arguments.internalArguments.filterNot {
        // -XXLanguage which turns on BUG_FIX considered safe
        it is ManualLanguageFeatureSetting && it.languageFeature.kind == LanguageFeature.Kind.BUG_FIX && it.state == LanguageFeature.State.ENABLED
    }

    if (unsafeArguments.isNotEmpty()) {
        konst unsafeArgumentsString = unsafeArguments.joinToString(prefix = "\n", postfix = "\n\n", separator = "\n") {
            it.stringRepresentation
        }

        report(
            CompilerMessageSeverity.STRONG_WARNING,
            "ATTENTION!\n" +
                    "This build uses unsafe internal compiler arguments:\n" +
                    unsafeArgumentsString +
                    "This mode is not recommended for production use,\n" +
                    "as no stability/compatibility guarantees are given on\n" +
                    "compiler or generated code. Use it at your own risk!\n"
        )
    }
}

private fun CompilerConfiguration.buildHmppModuleStructure(arguments: CommonCompilerArguments): HmppCliModuleStructure? {
    konst rawFragments = arguments.fragments
    konst rawFragmentSources = arguments.fragmentSources
    konst rawFragmentRefines = arguments.fragmentRefines

    konst messageCollector = getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)

    fun reportError(message: String) {
        messageCollector.report(CompilerMessageSeverity.ERROR, message)
    }

    fun reportWarning(message: String) {
        messageCollector.report(CompilerMessageSeverity.WARNING, message)
    }

    if (rawFragments == null) {
        if (rawFragmentRefines != null) {
            reportError("-Xfragment-refines flag can not be used without -Xfragments")
        }
        return null
    }

    if (!languageVersionSettings.languageVersion.usesK2) {
        reportWarning("-Xfragments flag is not supported for language version < 2.0")
        return null
    }


    konst sourcesByFragmentName: Map<String, Set<String>> = rawFragments.associateWith { mutableSetOf<String>() }.apply {
        rawFragmentSources.orEmpty().forEach { rawFragmentSourceArg ->
            konst split = rawFragmentSourceArg.split(":", limit = 2)
            if (split.size < 2) {
                reportError(
                    "Incorrect syntax for -Xfragment-sources argument. " +
                            "`<module name>:<source file>` expected but got `$rawFragmentSourceArg`"
                )
                return@forEach
            }
            konst fragmentName = split[0]
            konst fragmentSource = split[1]

            getOrElse(fragmentName) {
                reportError(
                    "Passed $rawFragmentSourceArg, " +
                            "but fragment `$fragmentName` of source file $fragmentSource is not specified in -Xfragments"
                )
                return@forEach
            }.add(fragmentSource)
        }
    }

    var modules = sourcesByFragmentName.map { (fragmentName, sources) -> HmppCliModule(fragmentName, sources) }

    var wasError = false
    // check sources mapping
    for (i in modules.indices) {
        konst m1 = modules[i]
        for (j in (i + 1) until modules.size) {
            konst m2 = modules[j]
            konst commonFiles = m1.sources.intersect(m2.sources)
            if (commonFiles.isNotEmpty()) {
                konst message = buildString {
                    if (commonFiles.size == 1) {
                        append("File '${commonFiles.single()}'")
                    } else {
                        append("Files ")
                        append(commonFiles.joinToString(", ") { "'$it'" })
                    }
                    append(
                        " can be a part of only one module, but is listed as a source for both `${m1.name}` and `${m2.name}`, " +
                                "please check you -Xfragment-sources options."
                    )
                }
                reportError(message)
                wasError = true
            }
        }
    }

    for (source in arguments.freeArgs) {
        if (source.endsWith(JavaFileType.DOT_DEFAULT_EXTENSION)) continue
        if (modules.none { source in it.sources }) {
            reportError("Source '$source' does not belong to any module")
            wasError = true
        }
    }

    if (wasError) {
        return null
    }

    if (modules.size == 1) {
        if (rawFragmentRefines?.isNotEmpty() == true) {
            reportError("-Xfragment-refines flag is specified but there is only one module declared")
        }
        return HmppCliModuleStructure(modules, emptyMap())
    }

    konst duplicatedModules = modules.filter { module -> modules.count { it.name == module.name } > 1 }

    if (duplicatedModules.isNotEmpty()) {
        reportError("There are multiple modules with same name(s): ${duplicatedModules.distinct().joinToString(", ") { it.name }}")
        return null
    }

    konst moduleByName = modules.associateBy { it.name }

    konst dependenciesMap = rawFragmentRefines.orEmpty().mapNotNull { rawFragmentRefinesEdge ->
        konst split = rawFragmentRefinesEdge.split(":")
        if (split.size != 2) {
            reportError(
                "Incorrect syntax for -Xfragment-refines argument. " +
                        "Expected <fromModuleName>:<onModuleName> but got `$rawFragmentRefines`"
            )
            return@mapNotNull null
        }
        konst moduleName1 = split[0]
        konst moduleName2 = split[1]

        fun findModule(name: String): HmppCliModule? {
            return moduleByName[name].also { module ->
                if (module == null) {
                    reportError("`-Xfragment-refines=$rawFragmentRefinesEdge` Fragment `$name` not found in -Xfragments arguments")
                }
            }
        }

        konst module1 = findModule(moduleName1)
        konst module2 = findModule(moduleName2)
        if (module1 == null || module2 == null) return@mapNotNull null
        module1 to module2
    }.groupBy(
        keySelector = { it.first },
        konstueTransform = { it.second }
    )

    modules = DFS.topologicalOrder(modules) { dependenciesMap[it].orEmpty() }.asReversed()

    modules.forEachIndexed { i, module ->
        konst dependencies = dependenciesMap[module].orEmpty()
        konst previousModules = modules.subList(0, i)
        if (dependencies.any { it !in previousModules }) {
            reportError("There is a cycle in dependencies of module `${module.name}`")
        }
    }

    return HmppCliModuleStructure(modules, dependenciesMap)
}
