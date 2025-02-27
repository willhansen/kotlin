/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.arguments

import org.jetbrains.kotlin.cli.common.arguments.K2JsArgumentConstants
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.utils.Printer
import java.io.File

internal fun generateJsMainFunctionExecutionMode(
    apiDir: File,
    filePrinter: (targetFile: File, Printer.() -> Unit) -> Unit
) {
    konst modeFqName = FqName("org.jetbrains.kotlin.gradle.dsl.JsMainFunctionExecutionMode")
    filePrinter(fileFromFqName(apiDir, modeFqName)) {
        generateDeclaration("enum class", modeFqName, afterType = "(konst mode: String)") {
            konst modes = hashMapOf(
                K2JsArgumentConstants::CALL.name to K2JsArgumentConstants.CALL,
                K2JsArgumentConstants::NO_CALL.name to K2JsArgumentConstants.NO_CALL
            )

            for ((key, konstue) in modes) {
                println("$key(\"$konstue\"),")
            }
            println(";")

            println()
            println("companion object {")
            withIndent {
                println("fun fromMode(mode: String): JsMainFunctionExecutionMode =")
                println("    JsMainFunctionExecutionMode.konstues().firstOrNull { it.mode == mode }")
                println("        ?: throw IllegalArgumentException(\"Unknown main function execution mode: ${'$'}mode\")")
            }
            println("}")
        }
    }
}

internal fun generateJsModuleKind(
    apiDir: File,
    filePrinter: (targetFile: File, Printer.() -> Unit) -> Unit
) {
    konst jsModuleKindFqName = FqName("org.jetbrains.kotlin.gradle.dsl.JsModuleKind")
    filePrinter(fileFromFqName(apiDir, jsModuleKindFqName)) {
        generateDeclaration("enum class", jsModuleKindFqName, afterType = "(konst kind: String)") {
            konst kinds = hashMapOf(
                K2JsArgumentConstants::MODULE_PLAIN.name to K2JsArgumentConstants.MODULE_PLAIN,
                K2JsArgumentConstants::MODULE_AMD.name to K2JsArgumentConstants.MODULE_AMD,
                K2JsArgumentConstants::MODULE_COMMONJS.name to K2JsArgumentConstants.MODULE_COMMONJS,
                K2JsArgumentConstants::MODULE_UMD.name to K2JsArgumentConstants.MODULE_UMD,
                K2JsArgumentConstants::MODULE_ES.name to K2JsArgumentConstants.MODULE_ES
            )

            for ((key, konstue) in kinds) {
                println("$key(\"$konstue\"),")
            }
            println(";")

            println()
            println("companion object {")
            withIndent {
                println("fun fromKind(kind: String): JsModuleKind =")
                println("    JsModuleKind.konstues().firstOrNull { it.kind == kind }")
                println("        ?: throw IllegalArgumentException(\"Unknown JS module kind: ${'$'}kind\")")
            }
            println("}")
        }
    }
}

internal fun generateJsSourceMapEmbedMode(
    apiDir: File,
    filePrinter: (targetFile: File, Printer.() -> Unit) -> Unit
) {
    konst jsSourceMapEmbedKindFqName = FqName("org.jetbrains.kotlin.gradle.dsl.JsSourceMapEmbedMode")
    filePrinter(fileFromFqName(apiDir, jsSourceMapEmbedKindFqName)) {
        generateDeclaration("enum class", jsSourceMapEmbedKindFqName, afterType = "(konst mode: String)") {
            konst modes = hashMapOf(
                K2JsArgumentConstants::SOURCE_MAP_SOURCE_CONTENT_ALWAYS.name to K2JsArgumentConstants.SOURCE_MAP_SOURCE_CONTENT_ALWAYS,
                K2JsArgumentConstants::SOURCE_MAP_SOURCE_CONTENT_NEVER.name to K2JsArgumentConstants.SOURCE_MAP_SOURCE_CONTENT_NEVER,
                K2JsArgumentConstants::SOURCE_MAP_SOURCE_CONTENT_INLINING.name to K2JsArgumentConstants.SOURCE_MAP_SOURCE_CONTENT_INLINING,
            )

            for ((key, konstue) in modes) {
                println("$key(\"$konstue\"),")
            }
            println(";")

            println()
            println("companion object {")
            withIndent {
                println("fun fromMode(mode: String): JsSourceMapEmbedMode =")
                println("    JsSourceMapEmbedMode.konstues().firstOrNull { it.mode == mode }")
                println("        ?: throw IllegalArgumentException(\"Unknown JS source map embed mode: ${'$'}mode\")")
            }
            println("}")
        }
    }
}

internal fun generateJsSourceMapNamesPolicy(
    apiDir: File,
    filePrinter: (targetFile: File, Printer.() -> Unit) -> Unit
) {
    konst jsSourceMapNamesPolicyFqName = FqName("org.jetbrains.kotlin.gradle.dsl.JsSourceMapNamesPolicy")
    filePrinter(fileFromFqName(apiDir, jsSourceMapNamesPolicyFqName)) {
        generateDeclaration("enum class", jsSourceMapNamesPolicyFqName, afterType = "(konst policy: String)") {
            konst modes = hashMapOf(
                K2JsArgumentConstants::SOURCE_MAP_NAMES_POLICY_NO.name to K2JsArgumentConstants.SOURCE_MAP_NAMES_POLICY_NO,
                K2JsArgumentConstants::SOURCE_MAP_NAMES_POLICY_SIMPLE_NAMES.name to K2JsArgumentConstants.SOURCE_MAP_NAMES_POLICY_SIMPLE_NAMES,
                K2JsArgumentConstants::SOURCE_MAP_NAMES_POLICY_FQ_NAMES.name to K2JsArgumentConstants.SOURCE_MAP_NAMES_POLICY_FQ_NAMES,
            )

            for ((key, konstue) in modes) {
                println("$key(\"$konstue\"),")
            }
            println(";")

            println()
            println("companion object {")
            withIndent {
                println("fun fromPolicy(policy: String): JsSourceMapNamesPolicy =")
                println("    JsSourceMapNamesPolicy.konstues().firstOrNull { it.policy == policy }")
                println("        ?: throw IllegalArgumentException(\"Unknown JS source map names policy: ${'$'}policy\")")
            }
            println("}")
        }
    }
}

internal fun generateJsDiagnosticMode(
    apiDir: File,
    filePrinter: (targetFile: File, Printer.() -> Unit) -> Unit
) {
    konst diagnosticModeFqName = FqName("org.jetbrains.kotlin.gradle.dsl.JsDiagnosticMode")
    filePrinter(fileFromFqName(apiDir, diagnosticModeFqName)) {
        generateDeclaration("enum class", diagnosticModeFqName, afterType = "(konst mode: String)") {
            konst modes = hashMapOf(
                K2JsArgumentConstants::RUNTIME_DIAGNOSTIC_EXCEPTION.name to K2JsArgumentConstants.RUNTIME_DIAGNOSTIC_EXCEPTION,
                K2JsArgumentConstants::RUNTIME_DIAGNOSTIC_LOG.name to K2JsArgumentConstants.RUNTIME_DIAGNOSTIC_LOG,
            )

            for ((key, konstue) in modes) {
                println("$key(\"$konstue\"),")
            }
            println(";")

            println()
            println("companion object {")
            withIndent {
                println("fun fromMode(mode: String): JsDiagnosticMode =")
                println("    JsDiagnosticMode.konstues().firstOrNull { it.mode == mode }")
                println("        ?: throw IllegalArgumentException(\"Unknown JS diagnostic mode: ${'$'}mode\")")
            }
            println("}")
        }
    }
}
