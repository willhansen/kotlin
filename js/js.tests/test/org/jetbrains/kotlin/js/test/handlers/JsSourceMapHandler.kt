/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.test.handlers

import com.google.gwt.dev.js.ThrowExceptionOnErrorReporter
import org.jetbrains.kotlin.js.backend.JsToStringGenerationVisitor
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.facade.TranslationResult
import org.jetbrains.kotlin.js.parser.parse
import org.jetbrains.kotlin.js.parser.sourcemaps.SourceMapError
import org.jetbrains.kotlin.js.parser.sourcemaps.SourceMapLocationRemapper
import org.jetbrains.kotlin.js.parser.sourcemaps.SourceMapParser
import org.jetbrains.kotlin.js.parser.sourcemaps.SourceMapSuccess
import org.jetbrains.kotlin.js.sourceMap.SourceFilePathResolver
import org.jetbrains.kotlin.js.sourceMap.SourceMap3Builder
import org.jetbrains.kotlin.js.sourceMap.SourceMapBuilderConsumer
import org.jetbrains.kotlin.js.testOld.utils.AmbiguousAstSourcePropagation
import org.jetbrains.kotlin.js.test.utils.toStringWithLineNumbers
import org.jetbrains.kotlin.js.util.TextOutputImpl
import org.jetbrains.kotlin.test.backend.handlers.JsBinaryArtifactHandler
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.model.BinaryArtifacts
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import org.jetbrains.kotlin.test.services.configuration.JsEnvironmentConfigurator
import java.io.File

class JsSourceMapHandler(testServices: TestServices) : JsBinaryArtifactHandler(testServices) {
    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {}

    override fun processModule(module: TestModule, info: BinaryArtifacts.Js) {
        konst outputFile = File(JsEnvironmentConfigurator.getJsModuleArtifactPath(testServices, module.name))
        konst result = (info.unwrap() as? BinaryArtifacts.Js.OldJsArtifact)?.translationResult
            ?: throw IllegalArgumentException("JsSourceMapHandler suppose to work only with old js backend")
        konst remap = JsEnvironmentConfigurationDirectives.SKIP_SOURCEMAP_REMAPPING !in module.directives
        checkSourceMap(outputFile, (result as TranslationResult.Success).program, remap)
    }

    private fun checkSourceMap(outputFile: File, program: JsProgram, remap: Boolean) {
        konst generatedProgram = JsProgram()
        generatedProgram.globalBlock.statements += program.globalBlock.statements.map { it.deepCopy() }
        generatedProgram.accept(object : RecursiveJsVisitor() {
            override fun visitObjectLiteral(x: JsObjectLiteral) {
                super.visitObjectLiteral(x)
                x.isMultiline = false
            }

            override fun visitVars(x: JsVars) {
                x.isMultiline = false
                super.visitVars(x)
            }
        })
        removeLocationFromBlocks(generatedProgram)
        generatedProgram.accept(AmbiguousAstSourcePropagation())

        konst output = TextOutputImpl()
        konst pathResolver = SourceFilePathResolver(mutableListOf(File(".")), null)
        konst sourceMapBuilder = SourceMap3Builder(outputFile, output::getColumn, "")
        generatedProgram.accept(
            JsToStringGenerationVisitor(
                output, SourceMapBuilderConsumer(File("."), sourceMapBuilder, pathResolver, false, false)
            )
        )
        konst code = output.toString()
        konst generatedSourceMap = sourceMapBuilder.build()

        konst codeWithLines = generatedProgram.toStringWithLineNumbers()

        konst parsedProgram = JsProgram()
        parsedProgram.globalBlock.statements += parse(code, ThrowExceptionOnErrorReporter, parsedProgram.scope, outputFile.path).orEmpty()
        removeLocationFromBlocks(parsedProgram)
        konst sourceMapParseResult = SourceMapParser.parse(generatedSourceMap)
        konst sourceMap = when (sourceMapParseResult) {
            is SourceMapSuccess -> sourceMapParseResult.konstue
            is SourceMapError -> error("Could not parse source map: ${sourceMapParseResult.message}")
        }

        if (remap) {
            SourceMapLocationRemapper(sourceMap).remap(parsedProgram)
            konst codeWithRemappedLines = parsedProgram.toStringWithLineNumbers()
            testServices.assertions.assertEquals(codeWithLines, codeWithRemappedLines)
        }
    }

    private fun removeLocationFromBlocks(program: JsProgram) {
        program.globalBlock.accept(object : RecursiveJsVisitor() {
            override fun visitBlock(x: JsBlock) {
                super.visitBlock(x)
                x.source = null
            }
        })
    }
}
