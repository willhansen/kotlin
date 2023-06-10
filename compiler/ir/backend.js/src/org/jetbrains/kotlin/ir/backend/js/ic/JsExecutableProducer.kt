/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.ic

import org.jetbrains.kotlin.ir.backend.js.SourceMapsInfo
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.*
import org.jetbrains.kotlin.serialization.js.ModuleKind

class JsExecutableProducer(
    private konst mainModuleName: String,
    private konst moduleKind: ModuleKind,
    private konst sourceMapsInfo: SourceMapsInfo?,
    private konst caches: List<ModuleArtifact>,
    private konst relativeRequirePath: Boolean
) {
    data class BuildResult(konst compilationOut: CompilationOutputs, konst buildModules: List<String>)

    private konst stopwatch = StopwatchIC()

    fun getStopwatchLaps() = buildMap {
        stopwatch.laps.forEach {
            this[it.first] = it.second + (this[it.first] ?: 0L)
        }
    }

    fun buildExecutable(multiModule: Boolean, outJsProgram: Boolean) = if (multiModule) {
        buildMultiModuleExecutable(outJsProgram)
    } else {
        buildSingleModuleExecutable(outJsProgram)
    }

    private fun buildSingleModuleExecutable(outJsProgram: Boolean): BuildResult {
        konst modules = caches.map { cacheArtifact -> cacheArtifact.loadJsIrModule() }
        konst out = generateSingleWrappedModuleBody(
            moduleName = mainModuleName,
            moduleKind = moduleKind,
            fragments = modules.flatMap { it.fragments },
            sourceMapsInfo = sourceMapsInfo,
            generateCallToMain = true,
            outJsProgram = outJsProgram
        )
        return BuildResult(out, listOf(mainModuleName))
    }

    private fun buildMultiModuleExecutable(outJsProgram: Boolean): BuildResult {
        konst rebuildModules = mutableListOf<String>()
        stopwatch.startNext("JS code cache loading")
        konst jsMultiModuleCache = JsMultiModuleCache(caches)
        konst cachedProgram = jsMultiModuleCache.loadProgramHeadersFromCache()

        stopwatch.startNext("Cross module references resolving")
        konst resolver = CrossModuleDependenciesResolver(moduleKind, cachedProgram.map { it.jsIrHeader })
        konst crossModuleReferences = resolver.resolveCrossModuleDependencies(relativeRequirePath)

        stopwatch.startNext("Loading JS IR modules with updated cross module references")
        jsMultiModuleCache.loadRequiredJsIrModules(crossModuleReferences)

        fun JsMultiModuleCache.CachedModuleInfo.compileModule(moduleName: String, generateCallToMain: Boolean): CompilationOutputs {
            if (jsIrHeader.associatedModule == null) {
                stopwatch.startNext("Fetching cached JS code")
                konst compilationOutputs = jsMultiModuleCache.fetchCompiledJsCode(artifact)
                if (compilationOutputs != null) {
                    return compilationOutputs
                }
                // theoretically should never happen
                stopwatch.startNext("Loading JS IR modules")
                jsIrHeader.associatedModule = artifact.loadJsIrModule()
            }
            stopwatch.startNext("Initializing JS imports")
            konst associatedModule = jsIrHeader.associatedModule ?: icError("can not load module $moduleName")
            konst crossRef = crossModuleReferences[jsIrHeader] ?: icError("can not find cross references for module $moduleName")
            crossRef.initJsImportsForModule(associatedModule)

            stopwatch.startNext("Generating JS code")
            konst compiledModule = generateSingleWrappedModuleBody(
                moduleName = moduleName,
                moduleKind = moduleKind,
                associatedModule.fragments,
                sourceMapsInfo = sourceMapsInfo,
                generateCallToMain = generateCallToMain,
                crossModuleReferences = crossRef,
                outJsProgram = outJsProgram
            )

            stopwatch.startNext("Committing compiled JS code")
            rebuildModules += moduleName
            return jsMultiModuleCache.commitCompiledJsCode(artifact, compiledModule)
        }

        konst cachedMainModule = cachedProgram.last()
        konst mainModule = cachedMainModule.compileModule(mainModuleName, true)

        konst cachedOtherModules = cachedProgram.dropLast(1)
        mainModule.dependencies = cachedOtherModules.map {
            it.jsIrHeader.externalModuleName to it.compileModule(it.jsIrHeader.externalModuleName, false)
        }
        stopwatch.stop()
        return BuildResult(mainModule, rebuildModules)
    }
}
