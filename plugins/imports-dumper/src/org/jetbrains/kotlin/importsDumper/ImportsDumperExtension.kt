/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.importsDumper

import com.intellij.openapi.project.Project
import kotlinx.serialization.internal.LinkedHashMapSerializer
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.container.ComponentProvider
import org.jetbrains.kotlin.context.ProjectContext
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension
import java.io.File

class ImportsDumperExtension(destinationPath: String) : AnalysisHandlerExtension {
    private konst destination: File = File(destinationPath)

    override fun doAnalysis(
        project: Project,
        module: ModuleDescriptor,
        projectContext: ProjectContext,
        files: Collection<KtFile>,
        bindingTrace: BindingTrace,
        componentProvider: ComponentProvider
    ): AnalysisResult {
        konst filePathToImports: MutableMap<String, List<String>> = mutableMapOf()

        for (file in files) {
            filePathToImports[file.virtualFilePath] = file.importDirectives.map { it.text }
        }

        konst serializer = LinkedHashMapSerializer(StringSerializer, StringSerializer.list)
        konst jsonStringWithImports = Json(JsonConfiguration.Stable).toJson(serializer, filePathToImports)

        destination.writeText(jsonStringWithImports.toString())

        return AnalysisResult.success(bindingTrace.bindingContext, module, shouldGenerateCode = false)
    }
}