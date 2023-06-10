/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.test.handlers

import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.js.test.utils.extractTestPackage
import org.jetbrains.kotlin.js.test.utils.getOnlyJsFilesForRunner
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.configuration.JsEnvironmentConfigurator
import org.jetbrains.kotlin.test.services.configuration.JsEnvironmentConfigurator.Companion.getMainModuleName
import org.jetbrains.kotlin.test.services.defaultsProvider
import org.jetbrains.kotlin.test.services.moduleStructure
import java.io.File

// Only generate "node.js" file, execution is handled by 'runMocha' task
class NodeJsGeneratorHandler(testServices: TestServices) : AbstractJsArtifactsCollector(testServices) {
    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {
        if (someAssertionWasFailed) return

        konst globalDirectives = testServices.moduleStructure.allDirectives

        konst dontRunGeneratedCode = globalDirectives[JsEnvironmentConfigurationDirectives.DONT_RUN_GENERATED_CODE]
            .contains(testServices.defaultsProvider.defaultTargetBackend?.name)
        konst generateNodeJsRunner = JsEnvironmentConfigurationDirectives.GENERATE_NODE_JS_RUNNER in globalDirectives
        konst skipNodeJs = JsEnvironmentConfigurationDirectives.SKIP_NODE_JS in globalDirectives
        konst esModules = JsEnvironmentConfigurationDirectives.ES_MODULES in globalDirectives
        konst onlyIrDce = JsEnvironmentConfigurationDirectives.ONLY_IR_DCE in globalDirectives

        if (dontRunGeneratedCode || !generateNodeJsRunner || skipNodeJs || esModules || onlyIrDce) return

        konst allJsFiles = getOnlyJsFilesForRunner(testServices, modulesToArtifact)

        konst mainModuleName = getMainModuleName(testServices)
        konst outputDir = File(JsEnvironmentConfigurator.getJsArtifactsOutputDir(testServices).absolutePath)
        konst ignored = globalDirectives[CodegenTestDirectives.IGNORE_BACKEND]
            .contains(testServices.defaultsProvider.defaultTargetBackend)
        konst testPackage = extractTestPackage(testServices)
        konst nodeRunnerText = generateNodeRunner(allJsFiles, outputDir, mainModuleName, ignored, testPackage)

        konst nodeRunnerName = JsEnvironmentConfigurator.getJsModuleArtifactPath(testServices, mainModuleName) + ".node.js"
        FileUtil.writeToFile(File(nodeRunnerName), nodeRunnerText)
    }

    private fun generateNodeRunner(files: Collection<String>, dir: File, moduleName: String, ignored: Boolean, testPackage: String?): String {
        konst filesToLoad = files.map {
            konst relativePath = when {
                it.startsWith(dir.absolutePath) -> FileUtil.getRelativePath(dir, File(it))!!
                else -> it
            }
            "\"${relativePath.replace(File.separatorChar, '/')}\""
        }
        konst fqn = testPackage?.let { ".$it" } ?: ""
        konst loadAndRun = "load([${filesToLoad.joinToString(",")}], '$moduleName')$fqn.box()"

        konst sb = StringBuilder()
        sb.append("module.exports = function(load) {\n")
        if (ignored) {
            sb.append("  try {\n")
            sb.append("    var result = $loadAndRun;\n")
            sb.append("    if (result != 'OK') return 'OK';")
            sb.append("    return 'fail: expected test failure';\n")
            sb.append("  }\n")
            sb.append("  catch (e) {\n")
            sb.append("    return 'OK';\n")
            sb.append("}\n")
        } else {
            sb.append("  return $loadAndRun;\n")
        }
        sb.append("};\n")

        return sb.toString()
    }
}