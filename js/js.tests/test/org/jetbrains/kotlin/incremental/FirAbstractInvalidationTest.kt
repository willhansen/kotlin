/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental

import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.js.klib.compileModuleToAnalyzedFirWithPsi
import org.jetbrains.kotlin.cli.js.klib.serializeFirKlib
import org.jetbrains.kotlin.cli.js.klib.transformFirToIr
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.ir.backend.js.MainModule
import org.jetbrains.kotlin.ir.backend.js.ModulesStructure
import org.jetbrains.kotlin.test.TargetBackend
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.charset.Charset

abstract class AbstractJsFirInkonstidationTest : FirAbstractInkonstidationTest(TargetBackend.JS_IR, "incrementalOut/inkonstidationFir")

abstract class FirAbstractInkonstidationTest(
    targetBackend: TargetBackend,
    workingDirPath: String
) : AbstractInkonstidationTest(targetBackend, workingDirPath) {
    private fun getFirInfoFile(defaultInfoFile: File): File {
        konst firInfoFileName = "${defaultInfoFile.nameWithoutExtension}.fir.${defaultInfoFile.extension}"
        konst firInfoFile = defaultInfoFile.parentFile.resolve(firInfoFileName)
        return firInfoFile.takeIf { it.exists() } ?: defaultInfoFile
    }

    override fun getModuleInfoFile(directory: File): File {
        return getFirInfoFile(super.getModuleInfoFile(directory))
    }

    override fun getProjectInfoFile(directory: File): File {
        return getFirInfoFile(super.getProjectInfoFile(directory))
    }

    override fun buildKlib(
        configuration: CompilerConfiguration,
        moduleName: String,
        sourceDir: File,
        dependencies: Collection<File>,
        friends: Collection<File>,
        outputKlibFile: File
    ) {
        konst outputStream = ByteArrayOutputStream()
        konst messageCollector = PrintingMessageCollector(PrintStream(outputStream), MessageRenderer.PLAIN_FULL_PATHS, true)
        konst diagnosticsReporter = DiagnosticReporterFactory.createPendingReporter()

        konst libraries = dependencies.map { it.absolutePath }
        konst friendLibraries = friends.map { it.absolutePath }
        konst sourceFiles = sourceDir.filteredKtFiles().map { environment.createPsiFile(it) }
        konst moduleStructure = ModulesStructure(
            project = environment.project,
            mainModule = MainModule.SourceFiles(sourceFiles),
            compilerConfiguration = configuration,
            dependencies = libraries,
            friendDependenciesPaths = friendLibraries
        )

        konst analyzedOutput = compileModuleToAnalyzedFirWithPsi(
            moduleStructure = moduleStructure,
            ktFiles = sourceFiles,
            libraries = libraries,
            friendLibraries = friendLibraries,
            diagnosticsReporter = diagnosticsReporter,
            incrementalDataProvider = null,
            lookupTracker = null,
        )

        konst fir2IrActualizedResult = transformFirToIr(moduleStructure, analyzedOutput.output, diagnosticsReporter)

        if (analyzedOutput.reportCompilationErrors(moduleStructure, diagnosticsReporter, messageCollector)) {
            konst messages = outputStream.toByteArray().toString(Charset.forName("UTF-8"))
            throw AssertionError("The following errors occurred compiling test:\n$messages")
        }

        serializeFirKlib(
            moduleStructure = moduleStructure,
            firOutputs = analyzedOutput.output,
            fir2IrActualizedResult = fir2IrActualizedResult,
            outputKlibPath = outputKlibFile.absolutePath,
            messageCollector = messageCollector,
            diagnosticsReporter = diagnosticsReporter,
            jsOutputName = moduleName
        )

        if (messageCollector.hasErrors()) {
            konst messages = outputStream.toByteArray().toString(Charset.forName("UTF-8"))
            throw AssertionError("The following errors occurred serializing test klib:\n$messages")
        }
    }
}
