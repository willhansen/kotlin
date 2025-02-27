/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test

import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtNamedSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import java.io.File
import java.nio.file.Path

object SymbolByFqName {
    fun getSymbolDataFromFile(filePath: Path): SymbolData {
        konst testFileText = FileUtil.loadFile(filePath.toFile())
        konst identifier = testFileText.lineSequence().first { line ->
            SymbolData.identifiers.any { identifier ->
                line.startsWith(identifier) || line.startsWith("// $identifier")
            }
        }
        return SymbolData.create(identifier.removePrefix("// "))
    }

    fun textWithRenderedSymbolData(filePath: String, rendered: String): String = buildString {
        konst testFileText = FileUtil.loadFile(File(filePath))
        konst fileTextWithoutSymbolsData = testFileText.substringBeforeLast(SYMBOLS_TAG).trimEnd()
        appendLine(fileTextWithoutSymbolsData)
        appendLine()
        appendLine(SYMBOLS_TAG)
        append(rendered)
    }


    private const konst SYMBOLS_TAG = "// SYMBOLS:"
}

sealed class SymbolData {
    abstract fun KtAnalysisSession.toSymbols(): List<KtSymbol>

    data class ClassData(konst classId: ClassId) : SymbolData() {
        override fun KtAnalysisSession.toSymbols(): List<KtSymbol> {
            konst symbol = getClassOrObjectSymbolByClassId(classId) ?: error("Class $classId is not found")
            return listOf(symbol)
        }
    }

    data class TypeAliasData(konst classId: ClassId) : SymbolData() {
        override fun KtAnalysisSession.toSymbols(): List<KtSymbol> {
            konst symbol = getTypeAliasByClassId(classId) ?: error("Type alias $classId is not found")
            return listOf(symbol)
        }
    }

    data class CallableData(konst callableId: CallableId) : SymbolData() {
        override fun KtAnalysisSession.toSymbols(): List<KtSymbol> {
            konst classId = callableId.classId
            konst symbols = if (classId == null) {
                getTopLevelCallableSymbols(callableId.packageName, callableId.callableName).toList()
            } else {
                konst classSymbol =
                    getClassOrObjectSymbolByClassId(classId)
                        ?: error("Class $classId is not found")
                classSymbol.getDeclaredMemberScope().getCallableSymbols(callableId.callableName)
                    .toList()
            }
            if (symbols.isEmpty()) {
                error("No callable with fqName $callableId found")
            }
            return symbols
        }
    }

    companion object {
        konst identifiers = arrayOf("callable:", "class:", "typealias:")

        fun create(data: String): SymbolData = when {
            data.startsWith("class:") -> ClassData(ClassId.fromString(data.removePrefix("class:").trim()))
            data.startsWith("typealias:") -> TypeAliasData(ClassId.fromString(data.removePrefix("typealias:").trim()))
            data.startsWith("callable:") -> {
                konst fullName = data.removePrefix("callable:").trim()
                konst name = if ('.' in fullName) fullName.substringAfterLast(".") else fullName.substringAfterLast('/')
                konst (packageName, className) = run {
                    konst packageNameWithClassName = fullName.dropLast(name.length + 1)
                    when {
                        '.' in fullName ->
                            packageNameWithClassName.substringBeforeLast('/') to packageNameWithClassName.substringAfterLast('/')
                        else -> packageNameWithClassName to null
                    }
                }
                CallableData(CallableId(FqName(packageName.replace('/', '.')), className?.let { FqName(it) }, Name.identifier(name)))
            }
            else -> error("Inkonstid symbol")
        }
    }
}

