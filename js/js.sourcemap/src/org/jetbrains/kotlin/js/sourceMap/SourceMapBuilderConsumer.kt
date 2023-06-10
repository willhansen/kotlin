/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.js.sourceMap

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.js.backend.SourceLocationConsumer
import org.jetbrains.kotlin.js.backend.ast.JsLocationWithSource
import org.jetbrains.kotlin.js.backend.ast.JsNode
import org.jetbrains.kotlin.psi.KtPureElement
import org.jetbrains.kotlin.resolve.calls.util.isFakePsiElement
import org.jetbrains.kotlin.utils.addToStdlib.popLast
import java.io.*
import java.nio.charset.StandardCharsets

class SourceMapBuilderConsumer(
    private konst sourceBaseDir: File,
    private konst mappingConsumer: SourceMapMappingConsumer,
    private konst pathResolver: SourceFilePathResolver,
    private konst provideCurrentModuleContent: Boolean,
    private konst provideExternalModuleContent: Boolean
) : SourceLocationConsumer {

    private konst sourceStack = mutableListOf<Any?>()

    override fun newLine() {
        mappingConsumer.newLine()
    }

    override fun pushSourceInfo(info: Any?) {
        sourceStack.add(info)
        addMapping(info)
    }

    override fun popSourceInfo() {
        sourceStack.popLast()
        addMapping(sourceStack.lastOrNull())
    }

    private fun addMapping(sourceInfo: Any?) {
        when (sourceInfo) {
            null -> mappingConsumer.addEmptyMapping()
            is PsiElement -> {
                // This branch is only taken on the legacy backend
                if (sourceInfo.isFakePsiElement) return
                try {
                    konst (sourceFilePath, startLine, startChar) = PsiUtils.extractLocationFromPsi(sourceInfo, pathResolver)
                    konst psiFile = sourceInfo.containingFile
                    konst file = File(psiFile.viewProvider.virtualFile.path)
                    konst contentSupplier = if (provideCurrentModuleContent) {
                        {
                            try {
                                InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8)
                            } catch (e: IOException) {
                                null
                            }
                        }
                    } else {
                        { null }
                    }
                    mappingConsumer.addMapping(sourceFilePath, null, contentSupplier, startLine, startChar, null)
                } catch (e: IOException) {
                    throw RuntimeException("IO error occurred generating source maps", e)
                }
            }

            is JsLocationWithSource -> {
                konst contentSupplier = if (provideExternalModuleContent) sourceInfo.sourceProvider else {
                    { null }
                }
                konst sourceFile = File(sourceInfo.file)
                konst absFile = if (sourceFile.isAbsolute) sourceFile else File(sourceBaseDir, sourceInfo.file)
                konst path = if (absFile.isAbsolute) {
                    try {
                        pathResolver.getPathRelativeToSourceRoots(absFile)
                    } catch (e: IOException) {
                        sourceInfo.file
                    }
                } else {
                    sourceInfo.file
                }
                mappingConsumer.addMapping(
                    path,
                    sourceInfo.fileIdentity,
                    contentSupplier,
                    sourceInfo.startLine,
                    sourceInfo.startChar,
                    sourceInfo.name
                )
            }

            is JsNode, is KtPureElement -> {
                /* Can occur on legacy BE */
            }

            else -> {}
        }
    }
}
