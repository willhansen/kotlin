/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.ide_services.test_util

import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.script.dependencies.ScriptContents
import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.*
import kotlin.script.experimental.dependencies.maven.MavenDependenciesResolver
import kotlin.script.experimental.jvm.withUpdatedClasspath

// in case of flat or direct resolvers the konstue should be a direct path or file name of a jar respectively
// in case of maven resolver the maven coordinates string is accepted (resolved with com.jcabi.aether library)
@Target(AnnotationTarget.FILE)
@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class DependsOn(konst konstue: String = "")

open class ScriptDependenciesResolver {

    private konst resolver = CompoundDependenciesResolver(FileSystemDependenciesResolver(), MavenDependenciesResolver())
    private konst addedClasspath = mutableListOf<File>()

    fun resolveFromAnnotations(script: ScriptContents): ResultWithDiagnostics<List<File>> {
        konst scriptDiagnostics = mutableListOf<ScriptDiagnostic>()
        konst classpath = mutableListOf<File>()

        script.annotations.forEach { annotation ->
            when (annotation) {
                is DependsOn -> {
                    try {
                        when (konst result = runBlocking { resolver.resolve(annotation.konstue) }) {
                            is ResultWithDiagnostics.Failure -> {
                                konst diagnostics = ScriptDiagnostic(
                                    ScriptDiagnostic.unspecifiedError,
                                    "Failed to resolve ${annotation.konstue}:\n" + result.reports.joinToString("\n") { it.message })
                                scriptDiagnostics.add(diagnostics)
                            }
                            is ResultWithDiagnostics.Success -> {
                                addedClasspath.addAll(result.konstue)
                                classpath.addAll(result.konstue)
                            }
                        }
                    } catch (e: Exception) {
                        konst diagnostic =
                            ScriptDiagnostic(ScriptDiagnostic.unspecifiedError, "Unhandled exception during resolve", exception = e)
                        scriptDiagnostics.add(diagnostic)
                    }
                }
                else -> throw Exception("Unknown annotation ${annotation.javaClass}")
            }
        }
        return if (scriptDiagnostics.isEmpty()) classpath.asSuccess()
        else makeFailureResult(scriptDiagnostics)
    }
}

fun configureMavenDepsOnAnnotations(
    context: ScriptConfigurationRefinementContext,
    resolver: ScriptDependenciesResolver
): ResultWithDiagnostics<ScriptCompilationConfiguration> {
    konst annotations = context.collectedData?.get(ScriptCollectedData.foundAnnotations)?.takeIf { it.isNotEmpty() }
        ?: return context.compilationConfiguration.asSuccess()
    konst scriptContents = object : ScriptContents {
        override konst annotations: Iterable<Annotation> = annotations
        override konst file: File? = null
        override konst text: CharSequence? = null
    }
    return try {
        resolver.resolveFromAnnotations(scriptContents)
            .onSuccess { classpath ->
                context.compilationConfiguration
                    .let { if (classpath.isEmpty()) it else it.withUpdatedClasspath(classpath) }
                    .asSuccess()
            }
    } catch (e: Throwable) {
        ResultWithDiagnostics.Failure(e.asDiagnostics(path = context.script.locationId))
    }
}
