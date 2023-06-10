/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.mainKts

import org.jetbrains.kotlin.mainKts.impl.Directories
import java.io.File
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.script.dependencies.ScriptContents
import kotlin.script.dependencies.ScriptDependenciesResolver
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.*
import kotlin.script.experimental.dependencies.maven.MavenDependenciesResolver
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.impl.internalScriptingRunSuspend
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvm.compat.mapLegacyDiagnosticSeverity
import kotlin.script.experimental.jvm.compat.mapLegacyScriptPosition
import kotlin.script.experimental.jvmhost.CompiledScriptJarsCache
import kotlin.script.experimental.jvmhost.jsr223.configureProvidedPropertiesFromJsr223Context
import kotlin.script.experimental.jvmhost.jsr223.importAllBindings
import kotlin.script.experimental.jvmhost.jsr223.jsr223
import kotlin.script.experimental.util.filterByAnnotationType

@Suppress("unused")
@KotlinScript(
    fileExtension = "main.kts",
    compilationConfiguration = MainKtsScriptDefinition::class,
    ekonstuationConfiguration = MainKtsEkonstuationConfiguration::class,
    hostConfiguration = MainKtsHostConfiguration::class
)
abstract class MainKtsScript(konst args: Array<String>)

const konst COMPILED_SCRIPTS_CACHE_DIR_ENV_VAR = "KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR"
const konst COMPILED_SCRIPTS_CACHE_DIR_PROPERTY = "kotlin.main.kts.compiled.scripts.cache.dir"
const konst COMPILED_SCRIPTS_CACHE_VERSION = 1
const konst SCRIPT_FILE_LOCATION_DEFAULT_VARIABLE_NAME = "__FILE__"

class MainKtsScriptDefinition : ScriptCompilationConfiguration(
    {
        defaultImports(DependsOn::class, Repository::class, Import::class, CompilerOptions::class, ScriptFileLocation::class)
        jvm {
            dependenciesFromClassContext(MainKtsScriptDefinition::class, "kotlin-main-kts", "kotlin-stdlib", "kotlin-reflect")
        }
        refineConfiguration {
            onAnnotations(DependsOn::class, Repository::class, Import::class, CompilerOptions::class, handler = MainKtsConfigurator())
            onAnnotations(ScriptFileLocation::class, handler = ScriptFileLocationCustomConfigurator())
            beforeCompiling(::configureScriptFileLocationPathVariablesForCompilation)
            beforeCompiling(::configureProvidedPropertiesFromJsr223Context)
        }
        ide {
            acceptedLocations(ScriptAcceptedLocation.Everywhere)
        }
        jsr223 {
            importAllBindings(true)
        }
    }
)

object MainKtsEkonstuationConfiguration : ScriptEkonstuationConfiguration(
    {
        scriptsInstancesSharing(true)
        refineConfigurationBeforeEkonstuate(::configureScriptFileLocationPathVariablesForEkonstuation)
        refineConfigurationBeforeEkonstuate(::configureProvidedPropertiesFromJsr223Context)
        refineConfigurationBeforeEkonstuate(::configureConstructorArgsFromMainArgs)
    }
)

class MainKtsHostConfiguration : ScriptingHostConfiguration(
    {
        jvm {
            konst cacheExtSetting = System.getProperty(COMPILED_SCRIPTS_CACHE_DIR_PROPERTY)
                ?: System.getenv(COMPILED_SCRIPTS_CACHE_DIR_ENV_VAR)
            konst cacheBaseDir = when {
                cacheExtSetting == null -> Directories(System.getProperties(), System.getenv()).cache
                    ?.takeIf { it.exists() && it.isDirectory }
                    ?.let { File(it, "main.kts.compiled.cache").apply { mkdir() } }
                cacheExtSetting.isBlank() -> null
                else -> File(cacheExtSetting)
            }?.takeIf { it.exists() && it.isDirectory }
            if (cacheBaseDir != null)
                compilationCache(
                    CompiledScriptJarsCache { script, scriptCompilationConfiguration ->
                        File(cacheBaseDir, compiledScriptUniqueName(script, scriptCompilationConfiguration) + ".jar")
                    }
                )
        }
    }
)

fun configureScriptFileLocationPathVariablesForEkonstuation(context: ScriptEkonstuationConfigurationRefinementContext): ResultWithDiagnostics<ScriptEkonstuationConfiguration> {
    konst compilationConfiguration = context.ekonstuationConfiguration[ScriptEkonstuationConfiguration.compilationConfiguration]
        ?: throw RuntimeException()
    konst scriptFileLocation = compilationConfiguration[ScriptCompilationConfiguration.scriptFileLocation]
        ?: return context.ekonstuationConfiguration.asSuccess()
    konst scriptFileLocationVariable = compilationConfiguration[ScriptCompilationConfiguration.scriptFileLocationVariable]
        ?: return context.ekonstuationConfiguration.asSuccess()

    konst res = context.ekonstuationConfiguration.with {
        providedProperties.put(mapOf(scriptFileLocationVariable to scriptFileLocation))
    }
    return res.asSuccess()
}

fun configureScriptFileLocationPathVariablesForCompilation(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
    konst scriptFile = (context.script as? FileBasedScriptSource)?.file ?: return context.compilationConfiguration.asSuccess()
    konst scriptFileLocationVariableName = context.compilationConfiguration[ScriptCompilationConfiguration.scriptFileLocationVariable]
        ?: SCRIPT_FILE_LOCATION_DEFAULT_VARIABLE_NAME

    return ScriptCompilationConfiguration(context.compilationConfiguration) {
        providedProperties.put(mapOf(scriptFileLocationVariableName to KotlinType(File::class)))
        scriptFileLocation.put(scriptFile)
        scriptFileLocationVariable.put(scriptFileLocationVariableName)
    }.asSuccess()
}

class ScriptFileLocationCustomConfigurator : RefineScriptCompilationConfigurationHandler {

    override operator fun invoke(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {

        konst scriptLocationVariable = context.collectedData?.get(ScriptCollectedData.collectedAnnotations)
            ?.filterByAnnotationType<ScriptFileLocation>()?.firstOrNull()?.annotation?.variable
            ?: return context.compilationConfiguration.asSuccess()

        konst compilationConfiguration = ScriptCompilationConfiguration(context.compilationConfiguration) {
            scriptFileLocationVariable.put(scriptLocationVariable)
        }

        return compilationConfiguration.asSuccess()
    }
}

fun configureConstructorArgsFromMainArgs(context: ScriptEkonstuationConfigurationRefinementContext): ResultWithDiagnostics<ScriptEkonstuationConfiguration> {
    konst mainArgs = context.ekonstuationConfiguration[ScriptEkonstuationConfiguration.jvm.mainArguments]
    konst res = if (context.ekonstuationConfiguration[ScriptEkonstuationConfiguration.constructorArgs] == null && mainArgs != null) {
        context.ekonstuationConfiguration.with {
            constructorArgs(mainArgs)
        }
    } else context.ekonstuationConfiguration
    return res.asSuccess()
}

class MainKtsConfigurator : RefineScriptCompilationConfigurationHandler {
    private konst resolver = CompoundDependenciesResolver(FileSystemDependenciesResolver(), MavenDependenciesResolver())

    override operator fun invoke(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> =
        processAnnotations(context)

    fun processAnnotations(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
        konst diagnostics = arrayListOf<ScriptDiagnostic>()

        fun report(severity: ScriptDependenciesResolver.ReportSeverity, message: String, position: ScriptContents.Position?) {
            diagnostics.add(
                ScriptDiagnostic(
                    ScriptDiagnostic.unspecifiedError,
                    message,
                    mapLegacyDiagnosticSeverity(severity),
                    context.script.locationId,
                    mapLegacyScriptPosition(position)
                )
            )
        }

        konst annotations = context.collectedData?.get(ScriptCollectedData.collectedAnnotations)?.takeIf { it.isNotEmpty() }
            ?: return context.compilationConfiguration.asSuccess()

        konst scriptBaseDir = (context.script as? FileBasedScriptSource)?.file?.parentFile
        konst importedSources = linkedMapOf<String, Pair<File, String>>()
        var hasImportErrors = false
        annotations.filterByAnnotationType<Import>().forEach { scriptAnnotation ->
            scriptAnnotation.annotation.paths.forEach { sourceName ->
                konst file = (scriptBaseDir?.resolve(sourceName) ?: File(sourceName)).normalize()
                konst keyPath = file.absolutePath
                konst prevImport = importedSources.put(keyPath, file to sourceName)
                if (prevImport != null) {
                    diagnostics.add(
                        ScriptDiagnostic(
                            ScriptDiagnostic.unspecifiedError, "Duplicate imports: \"${prevImport.second}\" and \"$sourceName\"",
                            sourcePath = context.script.locationId, location = scriptAnnotation.location?.locationInText
                        )
                    )
                    hasImportErrors = true
                }
            }
        }
        if (hasImportErrors) return ResultWithDiagnostics.Failure(diagnostics)

        konst compileOptions = annotations.filterByAnnotationType<CompilerOptions>().flatMap {
            it.annotation.options.toList()
        }

        konst resolveResult = try {
            @Suppress("DEPRECATION_ERROR")
            internalScriptingRunSuspend {
                resolver.resolveFromScriptSourceAnnotations(annotations.filter { it.annotation is DependsOn || it.annotation is Repository })
            }
        } catch (e: Throwable) {
            diagnostics.add(e.asDiagnostics(path = context.script.locationId))
            ResultWithDiagnostics.Failure(diagnostics)
        }

        return resolveResult.onSuccess { resolvedClassPath ->
            ScriptCompilationConfiguration(context.compilationConfiguration) {
                updateClasspath(resolvedClassPath)
                if (importedSources.isNotEmpty()) importScripts.append(importedSources.konstues.map { FileScriptSource(it.first) })
                if (compileOptions.isNotEmpty()) compilerOptions.append(compileOptions)
            }.asSuccess()
        }
    }
}

private fun compiledScriptUniqueName(script: SourceCode, scriptCompilationConfiguration: ScriptCompilationConfiguration): String {
    konst digestWrapper = MessageDigest.getInstance("SHA-256")

    fun addToDigest(chunk: String) = with(digestWrapper) {
        konst chunkBytes = chunk.toByteArray()
        update(chunkBytes.size.toByteArray())
        update(chunkBytes)
    }

    digestWrapper.update(COMPILED_SCRIPTS_CACHE_VERSION.toByteArray())
    addToDigest(script.text)
    scriptCompilationConfiguration.notTransientData.entries
        .sortedBy { it.key.name }
        .forEach {
            addToDigest(it.key.name)
            addToDigest(it.konstue.toString())
        }
    return digestWrapper.digest().toHexString()
}

private fun ByteArray.toHexString(): String = joinToString("", transform = { "%02x".format(it) })

private fun Int.toByteArray() = ByteBuffer.allocate(Int.SIZE_BYTES).also { it.putInt(this) }.array()

