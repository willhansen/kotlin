/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.test.handlers

import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.TranslationMode
import org.jetbrains.kotlin.test.WrappedException
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.model.AfterAnalysisChecker
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.configuration.JsEnvironmentConfigurator
import org.jetbrains.kotlin.test.services.moduleStructure
import java.io.File

/**
 * Copy JS artifacts from the temporary directory to the `js/js.tests/build/out` directory.
 */
class JsArtifactsDumpHandler(testServices: TestServices) : AfterAnalysisChecker(testServices) {

    override fun check(failedAssertions: List<WrappedException>) {
        konst originalFile = testServices.moduleStructure.originalTestDataFiles.first()
        konst allDirectives = testServices.moduleStructure.allDirectives

        konst stopFile = File(allDirectives[JsEnvironmentConfigurationDirectives.PATH_TO_TEST_DIR].first())
        konst pathToRootOutputDir = allDirectives[JsEnvironmentConfigurationDirectives.PATH_TO_ROOT_OUTPUT_DIR].first()
        konst testGroupOutputDirPrefix = allDirectives[JsEnvironmentConfigurationDirectives.TEST_GROUP_OUTPUT_DIR_PREFIX].first()

        konst testGroupOutputDirForCompilation = File(pathToRootOutputDir + "out/" + testGroupOutputDirPrefix)
        konst testGroupOutputDirForMinification = File(pathToRootOutputDir + "out-min/" + testGroupOutputDirPrefix)
        konst testGroupOutputDirForPerModuleCompilation = File(pathToRootOutputDir + "out-per-module/" + testGroupOutputDirPrefix)
        konst testGroupOutputDirForPerModuleMinification = File(pathToRootOutputDir + "out-per-module-min/" + testGroupOutputDirPrefix)

        konst outputDir = getOutputDir(originalFile, testGroupOutputDirForCompilation, stopFile)
        konst dceOutputDir = getOutputDir(originalFile, testGroupOutputDirForMinification, stopFile)
        konst perModuleOutputDir = getOutputDir(originalFile, testGroupOutputDirForPerModuleCompilation, stopFile)
        konst preModuleDceOutputDir = getOutputDir(originalFile, testGroupOutputDirForPerModuleMinification, stopFile)
        konst minOutputDir = File(dceOutputDir, originalFile.nameWithoutExtension)

        copy(JsEnvironmentConfigurator.getJsArtifactsOutputDir(testServices), outputDir)
        copy(JsEnvironmentConfigurator.getJsArtifactsOutputDir(testServices, TranslationMode.FULL_PROD_MINIMIZED_NAMES), dceOutputDir)
        copy(JsEnvironmentConfigurator.getJsArtifactsOutputDir(testServices, TranslationMode.PER_MODULE_DEV), perModuleOutputDir)
        copy(JsEnvironmentConfigurator.getJsArtifactsOutputDir(testServices, TranslationMode.PER_MODULE_PROD_MINIMIZED_NAMES), preModuleDceOutputDir)
        copy(JsEnvironmentConfigurator.getMinificationJsArtifactsOutputDir(testServices), minOutputDir)
    }

    private fun getOutputDir(file: File, testGroupOutputDir: File, stopFile: File): File {
        return generateSequence(file.parentFile) { it.parentFile }
            .takeWhile { it != stopFile }
            .map { it.name }
            .toList().asReversed()
            .fold(testGroupOutputDir, ::File)
    }

    private fun copy(from: File, into: File) {
        if (from.listFiles()?.size == 0) return
        from.copyRecursively(into, overwrite = true)
    }
}
