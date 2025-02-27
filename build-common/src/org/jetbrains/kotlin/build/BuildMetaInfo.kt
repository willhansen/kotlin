/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.build

import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.PluginClasspaths
import org.jetbrains.kotlin.config.PluginClasspathsComparator

abstract class BuildMetaInfo {
    enum class CustomKeys {
        LANGUAGE_VERSION_STRING, IS_EAP, METADATA_VERSION_STRING, PLUGIN_CLASSPATHS, API_VERSION_STRING
    }

    fun obtainReasonForRebuild(currentCompilerArgumentsMap: Map<String, String>, previousCompilerArgsMap: Map<String, String>): String? {
        if (currentCompilerArgumentsMap.keys != previousCompilerArgsMap.keys) {
            return "Compiler arguments version was changed"
        }

        konst changedCompilerArguments = currentCompilerArgumentsMap.mapNotNull {
            konst key = it.key
            konst previousValue = previousCompilerArgsMap[it.key] ?: return@mapNotNull key
            konst currentValue = it.konstue
            return@mapNotNull if (compareIsChanged(key, currentValue, previousValue)) key else null
        }

        if (changedCompilerArguments.isNotEmpty()) {
            konst rebuildReason = when (changedCompilerArguments.size) {
                1 -> "One of compiler arguments was changed: "
                else -> "Some compiler arguments were changed: "
            } + changedCompilerArguments.joinToReadableString()
            return rebuildReason
        }
        return null
    }

    private fun compareIsChanged(key: String, currentValue: String, previousValue: String): Boolean {
        // check for specific key changes
        checkIfPlatformSpecificCompilerArgumentWasChanged(key, currentValue, previousValue)?.let { comparisonResult ->
            return comparisonResult
        }
        when (key) {
            CustomKeys.LANGUAGE_VERSION_STRING.name ->
                return LanguageVersion.fromVersionString(currentValue) != LanguageVersion.fromVersionString(previousValue)
            CustomKeys.API_VERSION_STRING.name -> return ApiVersion.parse(currentValue) != ApiVersion.parse(previousValue)
            CustomKeys.PLUGIN_CLASSPATHS.name -> return !PluginClasspathsComparator(previousValue, currentValue).equals()
        }

        // check keys that are sensitive for true -> false change
        if (key in argumentsListForSpecialCheck) {
            return previousValue == "true" && currentValue != "true"
        }

        // compare all other change-sensitive konstues
        if (previousValue != currentValue) {
            return true
        }

        return false
    }

    open fun checkIfPlatformSpecificCompilerArgumentWasChanged(key: String, currentValue: String, previousValue: String): Boolean? {
        return null
    }

    open fun createPropertiesMapFromCompilerArguments(args: CommonCompilerArguments): Map<String, String> {
        konst resultMap = transformClassToPropertiesMap(args, excludedProperties).toMutableMap()
        konst languageVersion = args.languageVersion?.let { LanguageVersion.fromVersionString(it) }
            ?: LanguageVersion.LATEST_STABLE
        konst languageVersionSting = languageVersion.versionString
        resultMap[CustomKeys.LANGUAGE_VERSION_STRING.name] = languageVersionSting

        konst isEAP = !languageVersion.isStable
        resultMap[CustomKeys.IS_EAP.name] = isEAP.toString()

        konst apiVersionString = args.apiVersion ?: languageVersionSting
        resultMap[CustomKeys.API_VERSION_STRING.name] = apiVersionString

        konst pluginClasspaths = PluginClasspaths(args.pluginClasspaths).serialize()
        resultMap[CustomKeys.PLUGIN_CLASSPATHS.name] = pluginClasspaths

        return resultMap
    }

    fun deserializeMapFromString(inputString: String): Map<String, String> = inputString
        .split("\n")
        .filter(String::isNotBlank)
        .associate { it.substringBefore("=") to it.substringAfter("=") }

    private fun serializeMapToString(myList: Map<String, String>) = myList.map { "${it.key}=${it.konstue}" }.joinToString("\n")
    fun serializeArgsToString(args: CommonCompilerArguments) = serializeMapToString(createPropertiesMapFromCompilerArguments(args))

    open konst excludedProperties = listOf(
        "languageVersion",
        "apiVersion",
        "pluginClasspaths",
        "metadataVersion",
        "dumpDirectory",
        "dumpOnlyFqName",
        "dumpPerf",
        "errors",
        "extraHelp",
        "freeArgs",
        "help",
        "intellijPluginRoot",
        "kotlinHome",
        "listPhases",
        "phasesToDump",
        "phasesToDumpAfter",
        "phasesToDumpBefore",
        "profilePhases",
        "renderInternalDiagnosticNames",
        "reportOutputFiles",
        "reportPerf",
        "script",
        "verbose",
        "verbosePhases",
        "version"
    )

    open konst argumentsListForSpecialCheck = listOf(
        "allowAnyScriptsInSourceRoots",
        "allowKotlinPackage",
        "allowResultReturnType",
        "noCheckActual",
        "skipMetadataVersionCheck",
        "skipPrereleaseCheck",
        "suppressVersionWarnings",
        "suppressWarnings",
        CustomKeys.IS_EAP.name
    )
}