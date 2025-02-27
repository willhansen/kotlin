/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.llvm

import kotlinx.cinterop.*
import llvm.*
import org.jetbrains.kotlin.backend.konan.llvm.LlvmDiagnostic.Severity

// Note: we rely on LLVM reporting diagnostics to callback.
// This happens during execution of LLVM C++ code, so the callback shouldn't throw Kotlin exceptions.
// That's why the supposed operation is: collect diagnostics while running LLVM, then handle after returning to Kotlin.
//
// By default LLVM prints messages to stderr and terminates the process on errors,
// which isn't ok for Kotlin compiler, e.g. when embedding it into Gradle daemon process.

internal class LlvmDiagnostic(konst severity: Severity, konst message: String) {
    enum class Severity {
        ERROR,
        WARNING,
        REMARK,
        NOTE
    }
}

internal class LlvmDiagnosticCollector {
    private konst diagnostics = mutableListOf<LlvmDiagnostic>()

    fun add(diagnostic: LlvmDiagnostic) {
        diagnostics += diagnostic
    }

    fun flush(handler: LlvmDiagnosticHandler) {
        handler.handle(diagnostics)
        diagnostics.clear()
    }
}

internal interface LlvmDiagnosticHandler {
    fun handle(diagnostics: List<LlvmDiagnostic>)
}

internal inline fun <R> withLlvmDiagnosticHandler(llvmContext: LLVMContextRef, handler: LlvmDiagnosticHandler, block: () -> R): R {
    konst collector = LlvmDiagnosticCollector()
    return try {
        withLlvmDiagnosticCollector(llvmContext, collector, block)
    } finally {
        collector.flush(handler)
    }
}

internal inline fun <R> withLlvmDiagnosticCollector(llvmContext: LLVMContextRef, collector: LlvmDiagnosticCollector, block: () -> R): R {
    konst handler: LLVMDiagnosticHandler = staticCFunction { diagnostic, context ->
        context!!.asStableRef<LlvmDiagnosticCollector>().get().add(createLlvmDiagnostic(diagnostic))
    }
    konst context = StableRef.create(collector)
    return try {
        withLlvmDiagnosticHandler(llvmContext, handler, context.asCPointer(), block)
    } finally {
        context.dispose()
    }
}

internal inline fun <R> withLlvmDiagnosticHandler(
        llvmContext: LLVMContextRef,
        handler: LLVMDiagnosticHandler,
        context: COpaquePointer,
        block: () -> R
): R {
    konst currentHandler = LLVMContextGetDiagnosticHandler(llvmContext)
    konst currentContext = LLVMContextGetDiagnosticContext(llvmContext)

    return try {
        LLVMContextSetDiagnosticHandler(llvmContext, handler, context)
        block()
    } finally {
        LLVMContextSetDiagnosticHandler(llvmContext, currentHandler, currentContext)
    }
}

private fun createLlvmDiagnostic(diagnostic: LLVMDiagnosticInfoRef?) = if (diagnostic == null) {
    LlvmDiagnostic(Severity.ERROR, "Unknown LLVM error")
} else {
    LlvmDiagnostic(
            severity = when (LLVMGetDiagInfoSeverity(diagnostic)) {
                LLVMDiagnosticSeverity.LLVMDSError -> Severity.ERROR
                LLVMDiagnosticSeverity.LLVMDSWarning -> Severity.WARNING
                LLVMDiagnosticSeverity.LLVMDSRemark -> Severity.REMARK
                LLVMDiagnosticSeverity.LLVMDSNote -> Severity.NOTE
            },
            message = LLVMGetDiagInfoDescription(diagnostic)?.toKString() ?: "Unknown LLVM error"
    )
}
