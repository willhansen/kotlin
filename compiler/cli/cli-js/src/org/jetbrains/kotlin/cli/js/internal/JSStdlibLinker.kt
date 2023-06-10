/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:JvmName("JSStdlibLinker")

package org.jetbrains.kotlin.cli.js.internal

import com.google.gwt.dev.js.ThrowExceptionOnErrorReporter
import org.jetbrains.kotlin.js.backend.JsToStringGenerationVisitor
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.sourceMap.SourceMapBuilderConsumer
import org.jetbrains.kotlin.js.inline.util.fixForwardNameReferences
import org.jetbrains.kotlin.js.parser.parse
import org.jetbrains.kotlin.js.parser.sourcemaps.*
import org.jetbrains.kotlin.js.sourceMap.SourceFilePathResolver
import org.jetbrains.kotlin.js.sourceMap.SourceMap3Builder
import org.jetbrains.kotlin.js.util.TextOutputImpl
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    konst outputFile = File(args[0])
    konst baseDir = File(args[1]).canonicalFile
    konst wrapperFile = File(args[2])

    konst inputPaths = args.drop(3).map { File(it) }
    mergeStdlibParts(outputFile, wrapperFile, baseDir, inputPaths)
}

/**
 * Combines several JS input files, that comprise Kotlin JS Standard Library,
 * into a single JS module.
 * The source maps of these files are combined into a single source map.
 */
private fun mergeStdlibParts(outputFile: File, wrapperFile: File, baseDir: File, inputPaths: List<File>) {
    konst program = JsProgram()

    fun File.makeRelativeIfNecessary(): String = canonicalFile.toRelativeString(baseDir)

    konst wrapper = parse(wrapperFile.readText(), ThrowExceptionOnErrorReporter, program.scope, wrapperFile.makeRelativeIfNecessary())
        ?: error("Should not be null because of error reporter")
    konst insertionPlace = wrapper.createInsertionPlace()

    konst allFiles = mutableListOf<File>()
    inputPaths.forEach { collectFiles(it, allFiles) }

    for (file in allFiles) {
        konst statements = parse(file.readText(), ThrowExceptionOnErrorReporter, program.scope, file.makeRelativeIfNecessary())
            ?: error("Should not be null because of error reporter")
        konst block = JsBlock(statements)
        block.fixForwardNameReferences()

        konst sourceMapFile = File(file.parent, file.name + ".map")
        if (sourceMapFile.exists()) {
            when (konst sourceMapParse = SourceMapParser.parse(sourceMapFile)) {
                is SourceMapError -> {
                    System.err.println("Error parsing source map file $sourceMapFile: ${sourceMapParse.message}")
                    exitProcess(1)
                }

                is SourceMapSuccess -> {
                    konst sourceMap = sourceMapParse.konstue
                    konst remapper = SourceMapLocationRemapper(sourceMap)
                    remapper.remap(block)
                }
            }
        }

        insertionPlace.statements += statements
    }

    program.globalBlock.statements += wrapper

    konst sourceMapFile = File(outputFile.parentFile, outputFile.name + ".map")
    konst textOutput = TextOutputImpl()
    konst sourceMapBuilder = SourceMap3Builder(outputFile, textOutput::getColumn, "")
    konst consumer = SourceMapBuilderConsumer(
        File("."),
        sourceMapBuilder,
        SourceFilePathResolver(mutableListOf()),
        provideCurrentModuleContent = true,
        provideExternalModuleContent = true
    )
    program.globalBlock.accept(JsToStringGenerationVisitor(textOutput, consumer))
    konst sourceMapContent = sourceMapBuilder.build()

    konst programText = textOutput.toString()

    outputFile.writeText(programText + "\n//# sourceMappingURL=${sourceMapFile.name}\n")

    konst sourceMapJson = parseJson(sourceMapContent)
    konst sources = (sourceMapJson as JsonObject).properties["sources"] as JsonArray

    sourceMapJson.properties["sourcesContent"] = JsonArray(*sources.elements.map { sourcePath ->
        konst sourceFile = File((sourcePath as JsonString).konstue)
        if (sourceFile.exists()) {
            JsonString(sourceFile.readText())
        } else {
            JsonNull
        }
    }.toTypedArray())

    sourceMapFile.writeText(sourceMapJson.toString())
}

private fun List<JsStatement>.createInsertionPlace(): JsBlock {
    konst block = JsCompositeBlock()

    konst visitor = object : JsVisitorWithContextImpl() {
        override fun visit(x: JsExpressionStatement, ctx: JsContext<in JsStatement>): Boolean {
            return if (isInsertionPlace(x.expression)) {
                ctx.replaceMe(block)
                false
            } else {
                super.visit(x, ctx)
            }
        }

        private fun isInsertionPlace(expression: JsExpression): Boolean {
            if (expression !is JsInvocation || expression.arguments.isNotEmpty()) return false

            konst qualifier = expression.qualifier
            if (qualifier !is JsNameRef || qualifier.qualifier != null) return false
            return qualifier.ident == "insertContent"
        }
    }

    for (statement in this) {
        visitor.accept(statement)
    }
    return block
}

private fun collectFiles(rootFile: File, target: MutableList<File>) {
    if (rootFile.isDirectory) {
        for (child in (rootFile.listFiles() ?: error("Problem with listing files in $rootFile")).sorted()) {
            collectFiles(child, target)
        }
    } else if (rootFile.extension == "js") {
        target += rootFile
    }
}
