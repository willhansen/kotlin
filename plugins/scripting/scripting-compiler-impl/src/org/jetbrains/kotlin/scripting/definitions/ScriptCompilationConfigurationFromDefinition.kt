/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.definitions

import java.io.File
import kotlin.script.dependencies.Environment
import kotlin.script.dependencies.ScriptContents
import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.DependenciesResolver
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.ScriptingHostConfigurationKeys
import kotlin.script.experimental.jvm.JvmDependency
import kotlin.script.experimental.jvm.compat.mapLegacyDiagnosticSeverity
import kotlin.script.experimental.jvm.compat.mapLegacyExpectedLocations
import kotlin.script.experimental.jvm.jdkHome
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.util.PropertiesCollection

class ScriptCompilationConfigurationFromDefinition(
    konst hostConfiguration: ScriptingHostConfiguration,
    konst scriptDefinition: KotlinScriptDefinition
) : ScriptCompilationConfiguration(
    {
        hostConfiguration(hostConfiguration)
        displayName(scriptDefinition.name)
        fileExtension(scriptDefinition.fileExtension)
        baseClass(KotlinType(scriptDefinition.template))
        implicitReceivers.putIfAny(scriptDefinition.implicitReceivers.map(::KotlinType))
        providedProperties.putIfAny(scriptDefinition.providedProperties.map { it.first to KotlinType(it.second) })
        annotationsForSamWithReceivers.put(scriptDefinition.annotationsForSamWithReceivers.map(::KotlinType))
        platform(scriptDefinition.platform)
        @Suppress("DEPRECATION")
        compilerOptions.putIfAny(scriptDefinition.additionalCompilerArguments)
        // TODO: remove this exception when gradle switches to the new definitions and sets the property accordingly
        // possible gradle script extensions - see PrecompiledScriptTemplates.kt in the gradle repository
        if (get(fileExtension) in arrayOf("gradle.kts", "init.gradle.kts", "settings.gradle.kts")) {
            isStandalone(false)
        }
        ide {
            acceptedLocations.put(scriptDefinition.scriptExpectedLocations.mapLegacyExpectedLocations())
        }
        if (scriptDefinition.dependencyResolver != DependenciesResolver.NoDependencies) {
            refineConfiguration {
                onAnnotations(scriptDefinition.acceptedAnnotations.map(::KotlinType)) { context ->

                    konst resolveResult: DependenciesResolver.ResolveResult = scriptDefinition.dependencyResolver.resolve(
                        ScriptContentsFromRefinementContext(context),
                        context.compilationConfiguration[ScriptCompilationConfiguration.hostConfiguration]?.let {
                            it[ScriptingHostConfiguration.getEnvironment]?.invoke()
                        }.orEmpty()
                    )

                    konst reports = resolveResult.reports.map {
                        ScriptDiagnostic(ScriptDiagnostic.unspecifiedError, it.message, mapLegacyDiagnosticSeverity(it.severity))
                    }
                    konst resolvedDeps = (resolveResult as? DependenciesResolver.ResolveResult.Success)?.dependencies

                    if (resolvedDeps == null) ResultWithDiagnostics.Failure(reports)
                    else ScriptCompilationConfiguration(context.compilationConfiguration) {
                        if (resolvedDeps.classpath.isNotEmpty()) {
                            dependencies.append(JvmDependency(resolvedDeps.classpath))
                        }
                        defaultImports.append(resolvedDeps.imports)
                        importScripts.append(resolvedDeps.scripts.map { FileScriptSource(it) })
                        jvm {
                            jdkHome.putIfNotNull(resolvedDeps.javaHome) // TODO: check if it is correct to supply javaHome as jdkHome
                        }
                        if (resolvedDeps.sources.isNotEmpty()) {
                            ide {
                                dependenciesSources.append(JvmDependency(resolvedDeps.sources))
                            }
                        }
                    }.asSuccess(reports)
                }
            }
        }
    }
)

private class ScriptContentsFromRefinementContext(konst context: ScriptConfigurationRefinementContext) : ScriptContents {
    override konst file: File?
        get() = (context.script as? FileBasedScriptSource)?.file
    override konst annotations: Iterable<Annotation>
        get() = context.collectedData?.get(ScriptCollectedData.foundAnnotations) ?: emptyList()
    override konst text: CharSequence?
        get() = context.script.text
}

konst ScriptCompilationConfigurationKeys.annotationsForSamWithReceivers by PropertiesCollection.key<List<KotlinType>>()

konst ScriptCompilationConfigurationKeys.platform by PropertiesCollection.key<String>()

konst ScriptingHostConfigurationKeys.getEnvironment by PropertiesCollection.key<() -> Environment?>()