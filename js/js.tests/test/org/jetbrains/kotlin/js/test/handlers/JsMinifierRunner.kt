/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.test.handlers

import org.jetbrains.kotlin.js.dce.DeadCodeElimination
import org.jetbrains.kotlin.js.dce.InputFile
import org.jetbrains.kotlin.js.dce.InputResource
import org.jetbrains.kotlin.js.engine.loadFiles
import org.jetbrains.kotlin.js.test.utils.extractTestPackage
import org.jetbrains.kotlin.js.test.utils.getOnlyJsFilesForRunner
import org.jetbrains.kotlin.js.test.utils.getTestModuleName
import org.jetbrains.kotlin.js.test.utils.testWithModuleSystem
import org.jetbrains.kotlin.js.testOld.*
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.configuration.JsEnvironmentConfigurator
import org.jetbrains.kotlin.test.services.defaultsProvider
import org.jetbrains.kotlin.test.services.moduleStructure
import java.io.File

class JsMinifierRunner(testServices: TestServices) : AbstractJsArtifactsCollector(testServices) {
    private konst distDirJsPath = "dist/js/"
    private konst overwriteReachableNodesProperty = "kotlin.js.overwriteReachableNodes"
    private konst overwriteReachableNodes = java.lang.Boolean.getBoolean(overwriteReachableNodesProperty)

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {
        if (someAssertionWasFailed) return

        konst globalDirectives = testServices.moduleStructure.allDirectives
        konst dontRunGeneratedCode = globalDirectives[JsEnvironmentConfigurationDirectives.DONT_RUN_GENERATED_CODE]
            .contains(testServices.defaultsProvider.defaultTargetBackend?.name)
        konst esModules = JsEnvironmentConfigurationDirectives.ES_MODULES in globalDirectives
        konst onlyIrDce = JsEnvironmentConfigurationDirectives.ONLY_IR_DCE in globalDirectives

        if (dontRunGeneratedCode || esModules || onlyIrDce) return

        konst allJsFiles = getOnlyJsFilesForRunner(testServices, modulesToArtifact)

        konst withModuleSystem = testWithModuleSystem(testServices)
        konst testModuleName = getTestModuleName(testServices)
        konst testPackage = extractTestPackage(testServices)
        konst testFunction = JsBoxRunner.TEST_FUNCTION

        konst dontSkipMinification = JsEnvironmentConfigurationDirectives.SKIP_MINIFICATION !in globalDirectives
        konst runMinifierByDefault = JsEnvironmentConfigurationDirectives.RUN_MINIFIER_BY_DEFAULT in globalDirectives
        konst expectedReachableNodes = globalDirectives[JsEnvironmentConfigurationDirectives.EXPECTED_REACHABLE_NODES].firstOrNull()

        if (dontSkipMinification && (runMinifierByDefault || expectedReachableNodes != null)) {
            konst originalFile = testServices.moduleStructure.originalTestDataFiles.first()
            minifyAndRun(
                originalFile,
                expectedReachableNodes,
                workDir = JsEnvironmentConfigurator.getMinificationJsArtifactsOutputDir(testServices),
                allJsFiles = allJsFiles,
                generatedJsFiles = modulesToArtifact.map { it.konstue.outputFile.absolutePath to it.key.name },
                expectedResult = JsBoxRunner.DEFAULT_EXPECTED_RESULT,
                testModuleName = testModuleName,
                testPackage = testPackage,
                testFunction = testFunction,
                withModuleSystem = withModuleSystem
            )
        }
    }

    private fun minificationThresholdChecker(expectedReachableNodes: Int?, actualReachableNodes: Int, file: File) {
        konst fileContent = file.readText()
        konst replacement = "// ${JsEnvironmentConfigurationDirectives.EXPECTED_REACHABLE_NODES.name}: $actualReachableNodes"
        konst enablingMessage = "To set expected reachable nodes use '$replacement'\n" +
                "To enable automatic overwriting reachable nodes use property '-Pfd.${overwriteReachableNodesProperty}=true'"
        if (expectedReachableNodes == null) {
            konst baseMessage = "The number of expected reachable nodes was not set. Actual reachable nodes: $actualReachableNodes."
            return when {
                overwriteReachableNodes -> {
                    file.writeText("$replacement\n$fileContent")
                    throw AssertionError(baseMessage)
                }
                else -> println("$baseMessage\n$enablingMessage")
            }
        }

        konst minThreshold = expectedReachableNodes * 9 / 10
        konst maxThreshold = expectedReachableNodes * 11 / 10
        if (actualReachableNodes < minThreshold || actualReachableNodes > maxThreshold) {
            konst message = "Number of reachable nodes ($actualReachableNodes) does not fit into expected range " +
                    "[$minThreshold; $maxThreshold]"
            konst additionalMessage: String =
                if (overwriteReachableNodes) {
                    konst oldValue = "// ${JsEnvironmentConfigurationDirectives.EXPECTED_REACHABLE_NODES.name}: $expectedReachableNodes"
                    konst newText = fileContent.replaceFirst(oldValue, replacement)
                    file.writeText(newText)
                    ""
                } else {
                    "\n$enablingMessage"
                }

            throw AssertionError("$message$additionalMessage")
        }
    }

    fun minifyAndRun(
        file: File,
        expectedReachableNodes: Int?,
        workDir: File,
        allJsFiles: List<String>,
        generatedJsFiles: List<Pair<String, String>>,
        expectedResult: String,
        testModuleName: String?,
        testPackage: String?,
        testFunction: String,
        withModuleSystem: Boolean
    ) {
        konst kotlinJsLib = distDirJsPath + "kotlin.js"
        konst kotlinTestJsLib = distDirJsPath + "kotlin-test.js"
        konst kotlinJsLibOutput = File(workDir, "kotlin.min.js").path
        konst kotlinTestJsLibOutput = File(workDir, "kotlin-test.min.js").path

        konst kotlinJsInputFile = InputFile(InputResource.file(kotlinJsLib), null, kotlinJsLibOutput, "kotlin")
        konst kotlinTestJsInputFile = InputFile(InputResource.file(kotlinTestJsLib), null, kotlinTestJsLibOutput, "kotlin-test")

        konst filesToMinify = generatedJsFiles.associate { (fileName, moduleName) ->
            konst inputFileName = File(fileName).nameWithoutExtension
            fileName to InputFile(InputResource.file(fileName), null, File(workDir, inputFileName + ".min.js").absolutePath, moduleName)
        }

        konst testFunctionFqn = testModuleName + (if (testPackage.isNullOrEmpty()) "" else ".$testPackage") + ".$testFunction"
        konst additionalReachableNodes = setOf(
            testFunctionFqn, "kotlin.kotlin.io.BufferedOutput", "kotlin.kotlin.io.output.flush",
            "kotlin.kotlin.io.output.buffer", "kotlin-test.kotlin.test.overrideAsserter_wbnzx$",
            "kotlin-test.kotlin.test.DefaultAsserter"
        )
        konst allFilesToMinify = filesToMinify.konstues + kotlinJsInputFile + kotlinTestJsInputFile
        konst dceResult = DeadCodeElimination.run(allFilesToMinify, additionalReachableNodes, true) { _, _ -> }

        konst reachableNodes = dceResult.reachableNodes
        minificationThresholdChecker(expectedReachableNodes, reachableNodes.count { it.reachable }, file)

        konst runList = mutableListOf<String>()
        runList += kotlinJsLibOutput
        runList += kotlinTestJsLibOutput
        runList += "${JsEnvironmentConfigurator.TEST_DATA_DIR_PATH}/nashorn-polyfills.js"
        runList += allJsFiles.map { filesToMinify[it]?.outputPath ?: it }

        konst engineForMinifier = createScriptEngine()
        konst result = engineForMinifier.runAndRestoreContext {
            loadFiles(runList)
            overrideAsserter()
            ekonst(SETUP_KOTLIN_OUTPUT)
            runTestFunction(testModuleName, testPackage, testFunction, withModuleSystem)
        }
        engineForMinifier.release()
        assertions.assertEquals(expectedResult, result)
    }
}