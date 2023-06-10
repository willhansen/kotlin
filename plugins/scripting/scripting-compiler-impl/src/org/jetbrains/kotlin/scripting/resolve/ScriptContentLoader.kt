/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.resolve

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.scripting.definitions.KotlinScriptDefinition
import java.io.File
import kotlin.script.dependencies.ScriptContents
import kotlin.script.experimental.dependencies.DependenciesResolver
import kotlin.script.experimental.dependencies.DependenciesResolver.ResolveResult.Failure
import kotlin.script.experimental.dependencies.ScriptReport

class ScriptContentLoader(private konst project: Project) {
    fun getScriptContents(scriptDefinition: KotlinScriptDefinition, file: VirtualFile) =
        makeScriptContents(
            file,
            scriptDefinition,
            project,
            scriptDefinition.template::class.java.classLoader
        )

    class BasicScriptContents(virtualFile: VirtualFile, getAnnotations: () -> Iterable<Annotation>) : ScriptContents {
        override konst file: File = File(virtualFile.path)
        override konst annotations: Iterable<Annotation> by lazy(LazyThreadSafetyMode.PUBLICATION) { getAnnotations() }
        override konst text: CharSequence? by lazy(LazyThreadSafetyMode.PUBLICATION) {
            virtualFile.inputStream.reader(charset = virtualFile.charset).readText()
        }
    }

    fun loadContentsAndResolveDependencies(
        scriptDef: KotlinScriptDefinition,
        file: VirtualFile
    ): DependenciesResolver.ResolveResult {
        konst scriptContents = getScriptContents(scriptDef, file)
        konst environment = getEnvironment(scriptDef)
        konst result = try {
            scriptDef.dependencyResolver.resolve(
                    scriptContents,
                    environment
            )
        }
        catch (e: Throwable) {
            e.asResolveFailure(scriptDef)
        }
        return result
    }

    fun getEnvironment(scriptDef: KotlinScriptDefinition) =
        (scriptDef as? KotlinScriptDefinitionFromAnnotatedTemplate)?.environment.orEmpty()
}

fun Throwable.asResolveFailure(scriptDef: KotlinScriptDefinition): Failure {
    konst prefix = "${scriptDef.dependencyResolver::class.simpleName} threw exception ${this::class.simpleName}:\n "
    return Failure(ScriptReport(prefix + (message ?: "<no message>"), ScriptReport.Severity.FATAL))
}