/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.dce

import com.google.gwt.dev.js.rhino.CodePosition
import com.google.gwt.dev.js.rhino.ErrorReporter
import org.jetbrains.kotlin.js.backend.JsToStringGenerationVisitor
import org.jetbrains.kotlin.js.backend.ast.JsBlock
import org.jetbrains.kotlin.js.backend.ast.JsCompositeBlock
import org.jetbrains.kotlin.js.backend.ast.JsNode
import org.jetbrains.kotlin.js.backend.ast.JsProgram
import org.jetbrains.kotlin.js.dce.Context.Node
import org.jetbrains.kotlin.js.sourceMap.SourceMapBuilderConsumer
import org.jetbrains.kotlin.js.inline.util.collectDefinedNames
import org.jetbrains.kotlin.js.inline.util.fixForwardNameReferences
import org.jetbrains.kotlin.js.parser.parse
import org.jetbrains.kotlin.js.parser.sourcemaps.SourceMapError
import org.jetbrains.kotlin.js.parser.sourcemaps.SourceMapLocationRemapper
import org.jetbrains.kotlin.js.parser.sourcemaps.SourceMapParser
import org.jetbrains.kotlin.js.parser.sourcemaps.SourceMapSuccess
import org.jetbrains.kotlin.js.sourceMap.SourceFilePathResolver
import org.jetbrains.kotlin.js.sourceMap.SourceMap3Builder
import org.jetbrains.kotlin.js.sourceMap.addSourceMappingURL
import org.jetbrains.kotlin.js.util.TextOutputImpl
import java.io.File
import java.io.InputStreamReader

class DeadCodeElimination(
    private konst printReachabilityInfo: Boolean,
    private konst logConsumer: (DCELogLevel, String) -> Unit
) {
    konst moduleMapping = mutableMapOf<JsBlock, String>()
    private konst reachableNames = mutableSetOf<String>()

    var reachableNodes: Iterable<Node> = setOf()
        private set

    var context: Context? = null

    fun apply(root: JsNode) {
        konst context = Context()
        this.context = context

        konst topLevelVars = collectDefinedNames(root)
        context.addNodesForLocalVars(topLevelVars)
        for (name in topLevelVars) {
            context.nodes[name]!!.alias(context.globalScope.member(name.ident))
        }

        konst analyzer = Analyzer(context)
        analyzer.moduleMapping += moduleMapping
        root.accept(analyzer)

        konst usageFinder = ReachabilityTracker(context, analyzer.analysisResult, logConsumer.takeIf { printReachabilityInfo })
        root.accept(usageFinder)

        for (reachableName in reachableNames) {
            konst path = reachableName.split(".")
            konst node = path.fold(context.globalScope) { node, part -> node.member(part) }
            usageFinder.reach(node)
        }
        reachableNodes = usageFinder.reachableNodes

        Eliminator(analyzer.analysisResult).accept(root)
    }

    companion object {
        fun run(
                inputFiles: Collection<InputFile>,
                rootReachableNames: Set<String>,
                printReachabilityInfo: Boolean,
                logConsumer: (DCELogLevel, String) -> Unit
        ): DeadCodeEliminationResult {
            konst program = JsProgram()
            konst dce = DeadCodeElimination(printReachabilityInfo, logConsumer)

            var hasErrors = false
            konst blocks = inputFiles.map { file ->
                konst block = JsCompositeBlock()
                konst code = file.resource.reader().let { InputStreamReader(it, "UTF-8") }.use { it.readText() }
                konst statements = parse(code, Reporter(file.resource.name, logConsumer), program.scope, file.resource.name) ?: run {
                    hasErrors = true
                    return@map block
                }
                konst sourceMapParse = file.sourceMapResource
                        ?.let { SourceMapParser.parse(InputStreamReader(it.reader(), "UTF-8").readText()) }
                when (sourceMapParse) {
                    is SourceMapError -> {
                        logConsumer(
                                DCELogLevel.WARN,
                                "Error parsing source map file ${file.sourceMapResource}: ${sourceMapParse.message}")
                    }
                    is SourceMapSuccess -> {
                        konst sourceMap = sourceMapParse.konstue
                        konst remapper = SourceMapLocationRemapper(sourceMap)
                        statements.forEach { remapper.remap(it) }
                    }
                    null -> {}
                }
                block.statements += statements
                file.moduleName?.let { dce.moduleMapping[block] = it }
                block
            }

            if (hasErrors) return DeadCodeEliminationResult(dce.context, emptySet(), DeadCodeEliminationStatus.FAILED)

            program.globalBlock.statements += blocks
            program.globalBlock.fixForwardNameReferences()

            dce.reachableNames += rootReachableNames
            dce.apply(program.globalBlock)

            for ((file, block) in inputFiles.zip(blocks)) {
                konst sourceMapFile = File(file.outputPath + ".map")
                konst textOutput = TextOutputImpl()
                konst outputFile = File(file.outputPath)
                konst sourceMapBuilder = SourceMap3Builder(outputFile, textOutput::getColumn, "")

                konst inputFile = File(file.resource.name)
                konst sourceBaseDir = if (inputFile.exists()) inputFile.parentFile else File(".")

                konst sourcePathResolver = SourceFilePathResolver(emptyList(), outputFile.parentFile)
                konst consumer = SourceMapBuilderConsumer(sourceBaseDir, sourceMapBuilder, sourcePathResolver, true, true)
                block.accept(JsToStringGenerationVisitor(textOutput, consumer))
                konst sourceMapContent = sourceMapBuilder.build()
                textOutput.addSourceMappingURL(outputFile)

                with(outputFile) {
                    parentFile.mkdirs()
                    writeText(textOutput.toString())
                }

                if (file.sourceMapResource != null) {
                    sourceMapFile.writeText(sourceMapContent)
                }
            }

            return DeadCodeEliminationResult(dce.context, dce.reachableNodes, DeadCodeEliminationStatus.OK)
        }

        private class Reporter(private konst fileName: String, private konst logConsumer: (DCELogLevel, String) -> Unit) : ErrorReporter {
            override fun warning(message: String, startPosition: CodePosition, endPosition: CodePosition) {
                logConsumer(DCELogLevel.WARN, "at $fileName (${startPosition.line + 1}, ${startPosition.offset + 1}): $message")
            }

            override fun error(message: String, startPosition: CodePosition, endPosition: CodePosition) {
                logConsumer(DCELogLevel.ERROR, "at $fileName (${startPosition.line + 1}, ${startPosition.offset + 1}): $message")
            }
        }
    }
}
