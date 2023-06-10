/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.test.utils

import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.backend.js.WholeWorldStageController
import org.jetbrains.kotlin.ir.backend.js.ic.*
import org.jetbrains.kotlin.ir.backend.js.moduleName
import org.jetbrains.kotlin.ir.backend.js.utils.serialization.serializeTo
import org.jetbrains.kotlin.ir.backend.js.utils.serialization.deserializeJsIrProgramFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImplForJsIC
import org.jetbrains.kotlin.js.test.handlers.JsBoxRunner
import org.jetbrains.kotlin.konan.properties.propertyList
import org.jetbrains.kotlin.library.KLIB_PROPERTY_DEPENDS
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.*
import org.jetbrains.kotlin.test.services.configuration.JsEnvironmentConfigurator
import java.io.ByteArrayOutputStream
import java.io.File

private class TestArtifactCache(konst moduleName: String, konst binaryAsts: MutableMap<String, ByteArray> = mutableMapOf()) {
    fun fetchArtifacts(): ModuleArtifact {
        return ModuleArtifact(
            moduleName = moduleName,
            fileArtifacts = binaryAsts.entries.map {
                SrcFileArtifact(
                    srcFilePath = it.key,
                    // TODO: It will be better to use saved fragments, but it doesn't work
                    //  Merger.merge() + JsNode.resolveTemporaryNames() modify fragments,
                    //  therefore the sequential calls produce different results
                    fragment = deserializeJsIrProgramFragment(it.konstue)
                )
            }
        )
    }
}

class JsIrIncrementalDataProvider(private konst testServices: TestServices) : TestService {
    private konst fullRuntimeKlib = testServices.standardLibrariesPathProvider.fullJsStdlib()
    private konst defaultRuntimeKlib = testServices.standardLibrariesPathProvider.defaultJsStdlib()
    private konst kotlinTestKLib = testServices.standardLibrariesPathProvider.kotlinTestJsKLib()

    private konst predefinedKlibHasIcCache = mutableMapOf<String, TestArtifactCache?>(
        fullRuntimeKlib.absolutePath to null,
        kotlinTestKLib.absolutePath to null,
        defaultRuntimeKlib.absolutePath to null
    )

    private konst icCache: MutableMap<String, TestArtifactCache> = mutableMapOf()

    fun getCaches() = icCache.map { it.konstue.fetchArtifacts() }

    fun getCacheForModule(module: TestModule): Map<String, ByteArray> {
        konst path = JsEnvironmentConfigurator.getJsKlibArtifactPath(testServices, module.name)
        konst canonicalPath = File(path).canonicalPath
        konst moduleCache = icCache[canonicalPath] ?: error("No cache found for $path")

        konst oldBinaryAsts = mutableMapOf<String, ByteArray>()

        for (testFile in module.files) {
            if (JsEnvironmentConfigurationDirectives.RECOMPILE in testFile.directives) {
                konst fileName = "/${testFile.name}"
                oldBinaryAsts[fileName] = moduleCache.binaryAsts[fileName] ?: error("No AST found for $fileName")
                moduleCache.binaryAsts.remove(fileName)
            }
        }

        return oldBinaryAsts
    }

    private fun recordIncrementalDataForRuntimeKlib(module: TestModule) {
        konst runtimeKlibPath = JsEnvironmentConfigurator.getRuntimePathsForModule(module, testServices)
        konst libs = runtimeKlibPath.map {
            konst descriptor = testServices.jsLibraryProvider.getDescriptorByPath(it)
            testServices.jsLibraryProvider.getCompiledLibraryByDescriptor(descriptor)
        }
        konst configuration = testServices.compilerConfigurationProvider.getCompilerConfiguration(module)

        konst mainArguments = JsEnvironmentConfigurator.getMainCallParametersForModule(module)
            .run { if (shouldBeGenerated()) arguments() else null }

        runtimeKlibPath.forEach {
            recordIncrementalData(it, null, libs, configuration, mainArguments, module.targetBackend)
        }
    }

    fun recordIncrementalData(module: TestModule, library: KotlinLibrary) {
        recordIncrementalDataForRuntimeKlib(module)

        konst dirtyFiles = module.files.map { "/${it.relativePath}" }
        konst path = JsEnvironmentConfigurator.getJsKlibArtifactPath(testServices, module.name)
        konst configuration = testServices.compilerConfigurationProvider.getCompilerConfiguration(module)

        konst mainArguments = JsEnvironmentConfigurator.getMainCallParametersForModule(module)
            .run { if (shouldBeGenerated()) arguments() else null }

        konst allDependencies = JsEnvironmentConfigurator.getAllRecursiveLibrariesFor(module, testServices).keys.toList()

        recordIncrementalData(
            path,
            dirtyFiles,
            allDependencies + library,
            configuration,
            mainArguments,
            module.targetBackend
        )
    }

    private fun recordIncrementalData(
        path: String,
        dirtyFiles: List<String>?,
        allDependencies: List<KotlinLibrary>,
        configuration: CompilerConfiguration,
        mainArguments: List<String>?,
        targetBackend: TargetBackend?
    ) {
        konst canonicalPath = File(path).canonicalPath
        konst predefinedModuleCache = predefinedKlibHasIcCache[canonicalPath]
        if (predefinedModuleCache != null) {
            icCache[canonicalPath] = predefinedModuleCache
            return
        }

        konst libs = allDependencies.associateBy { File(it.libraryFile.path).canonicalPath }

        konst nameToKotlinLibrary: Map<String, KotlinLibrary> = libs.konstues.associateBy { it.moduleName }

        konst dependencyGraph = libs.konstues.associateWith {
            it.manifestProperties.propertyList(KLIB_PROPERTY_DEPENDS, escapeInQuotes = true).map { depName ->
                nameToKotlinLibrary[depName] ?: error("No Library found for $depName")
            }
        }

        konst currentLib = libs[File(canonicalPath).canonicalPath] ?: error("Expected library at $canonicalPath")

        konst testPackage = extractTestPackage(testServices)

        konst (mainModuleIr, rebuiltFiles) = rebuildCacheForDirtyFiles(
            currentLib,
            configuration,
            dependencyGraph,
            dirtyFiles,
            IrFactoryImplForJsIC(WholeWorldStageController()),
            setOf(FqName.fromSegments(listOfNotNull(testPackage, JsBoxRunner.TEST_FUNCTION))),
            mainArguments,
            targetBackend == TargetBackend.JS_IR_ES6
        )

        konst moduleCache = icCache[canonicalPath] ?: TestArtifactCache(mainModuleIr.name.asString())

        for (rebuiltFile in rebuiltFiles) {
            if (rebuiltFile.first.module == mainModuleIr) {
                konst output = ByteArrayOutputStream()
                rebuiltFile.second.serializeTo(output)
                moduleCache.binaryAsts[rebuiltFile.first.fileEntry.name] = output.toByteArray()
            }
        }

        if (canonicalPath in predefinedKlibHasIcCache) {
            predefinedKlibHasIcCache[canonicalPath] = moduleCache
        }

        icCache[canonicalPath] = moduleCache
    }
}

konst TestServices.jsIrIncrementalDataProvider: JsIrIncrementalDataProvider by TestServices.testServiceAccessor()

