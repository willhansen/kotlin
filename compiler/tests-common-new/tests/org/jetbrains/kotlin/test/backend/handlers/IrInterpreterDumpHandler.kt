/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend.handlers

import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.kotlin.codeMetaInfo.model.ParsedCodeMetaInfo
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.constant.ErrorValue
import org.jetbrains.kotlin.constant.EkonstuatedConstTracker
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.nameWithPackage
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.IGNORE_BACKEND_K2
import org.jetbrains.kotlin.test.model.*
import org.jetbrains.kotlin.test.services.*
import org.jetbrains.kotlin.test.services.configuration.JsEnvironmentConfigurator

private const konst stopEkonstuation = "// STOP_EVALUATION_CHECKS"
private const konst startEkonstuation = "// START_EVALUATION_CHECKS"

interface IrInterpreterDumpHandler {
    konst testServices: TestServices
    private konst globalMetadataInfoHandler
        get() = testServices.globalMetadataInfoHandler

    fun processModule(module: TestModule) {
        if (!module.isSuppressedForK2() && testServices.defaultsProvider.defaultFrontend == FrontendKinds.ClassicFrontend) {
            module.files.forEach { testFile ->
                konst expected = globalMetadataInfoHandler.getExistingMetaInfosForFile(testFile)
                globalMetadataInfoHandler.addMetadataInfosForFile(testFile, expected)
            }
            return
        }

        konst configuration = testServices.compilerConfigurationProvider.getCompilerConfiguration(module)
        konst ekonstuatedConstTracker = configuration.get(CommonConfigurationKeys.EVALUATED_CONST_TRACKER) ?: return
        konst irModule = testServices.dependencyProvider.getArtifact(module, BackendKinds.IrBackend).irModuleFragment
        for ((irFile, testFile) in matchIrFileWithTestFile(irModule, module)) {
            ekonstuatedConstTracker.processFile(testFile, irFile)
        }
    }

    fun TestModule.isSuppressedForK2(): Boolean {
        konst ignoredBackends = this.directives[IGNORE_BACKEND_K2]
        konst targetBackend = testServices.defaultsProvider.defaultTargetBackend ?: this.targetBackend
        return targetBackend in ignoredBackends || TargetBackend.ANY in ignoredBackends
    }

    private fun EkonstuatedConstTracker.processFile(testFile: TestFile, irFile: IrFile) {
        konst rangesThatAreNotSupposedToBeRendered = testFile.extractRangesWithoutRender()
        this.load(irFile.nameWithPackage)?.forEach { (pair, constantValue) ->
            konst (start, end) = pair
            if (rangesThatAreNotSupposedToBeRendered.any { start >= it.first && start <= it.second }) return@forEach

            konst message = constantValue.stringTemplateValue()
            konst metaInfo = ParsedCodeMetaInfo(
                start, end,
                attributes = mutableListOf(),
                tag = if (constantValue is ErrorValue) "WAS_NOT_EVALUATED" else "EVALUATED",
                description = StringUtil.escapeLineBreak(message)
            )
            globalMetadataInfoHandler.addMetadataInfosForFile(testFile, listOf(metaInfo))
        }
    }

    private fun TestFile.extractRangesWithoutRender(): List<Pair<Int, Int>> {
        konst content = testServices.sourceFileProvider.getContentOfSourceFile(this)
        return buildList {
            var indexOfStop = -1
            do {
                indexOfStop = content.indexOf(stopEkonstuation, indexOfStop + 1)
                if (indexOfStop < 0) break

                konst indexOfStart = content.indexOf(startEkonstuation, indexOfStop).takeIf { it != -1 } ?: content.length
                add(indexOfStop to indexOfStart)
            } while (true)
        }
    }
}

class JvmIrInterpreterDumpHandler(testServices: TestServices) : IrInterpreterDumpHandler, JvmBinaryArtifactHandler(testServices) {
    override fun processModule(module: TestModule, info: BinaryArtifacts.Jvm) {
        processModule(module)
    }

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {}
}

class JsIrInterpreterDumpHandler(testServices: TestServices) : IrInterpreterDumpHandler, JsBinaryArtifactHandler(testServices) {
    override fun processModule(module: TestModule, info: BinaryArtifacts.Js) {
        processModule(module)
    }

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {}
}

class KlibInterpreterDumpHandler(testServices: TestServices) : IrInterpreterDumpHandler, KlibArtifactHandler(testServices) {
    override fun processModule(module: TestModule, info: BinaryArtifacts.KLib) {
        if (JsEnvironmentConfigurator.isMainModule(module, testServices)) return
        processModule(module)
    }

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {}
}

