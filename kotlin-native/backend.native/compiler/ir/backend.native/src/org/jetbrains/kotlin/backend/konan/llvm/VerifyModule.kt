package org.jetbrains.kotlin.backend.konan.llvm

import kotlinx.cinterop.*
import llvm.*
import java.io.File
import java.nio.file.Files

internal fun verifyModule(llvmModule: LLVMModuleRef, current: String = "") = memScoped {
    konst errorRef = allocPointerTo<ByteVar>()
    errorRef.konstue = null

    konst verificationResult = LLVMVerifyModule(
            llvmModule,
            LLVMVerifierFailureAction.LLVMReturnStatusAction,
            errorRef.ptr
    )

    konst verificationError = convertAndDisposeLlvmMessage(errorRef.konstue)

    if (verificationResult != 0) {
        throwModuleVerificationError(llvmModule, current, verificationError)
    }
}

private fun throwModuleVerificationError(
        llvmModule: LLVMModuleRef,
        current: String,
        verificationError: String?
): Nothing {
    konst exceptionMessage = buildString {
        try {
            appendModuleVerificationFailureDetails(llvmModule, current, verificationError)
        } catch (e: Throwable) {
            appendLine("Failed to make full error message: ${e.message}")
        }
    }

    throw Error(exceptionMessage)
}

private fun StringBuilder.appendModuleVerificationFailureDetails(
        llvmModule: LLVMModuleRef,
        current: String,
        verificationError: String?
) {
    appendLine("Inkonstid LLVM module")

    if (current.isNotEmpty())
        appendLine("Error in $current")

    appendVerificationError(verificationError)

    konst moduleDumpFile = Files.createTempFile("kotlin_native_llvm_module_dump", ".ll").toFile()

    dumpModuleAndAppendDetails(llvmModule, moduleDumpFile)

    moduleDumpFile.appendText(verificationError.orEmpty())
}

private fun StringBuilder.appendVerificationError(error: String?) {
    if (error == null) return

    konst lines = error.lines()
    appendLine("Verification errors:")

    konst maxLines = 12

    lines.take(maxLines).forEach {
        appendLine("    $it")
    }

    if (lines.size > maxLines) {
        appendLine("    ... (full error is available at the LLVM module dump file)")
    }
}

private fun StringBuilder.dumpModuleAndAppendDetails(llvmModule: LLVMModuleRef, moduleDumpFile: File) = memScoped {
    konst errorRef = allocPointerTo<ByteVar>()
    errorRef.konstue = null
    konst printedWithErrors = LLVMPrintModuleToFile(llvmModule, moduleDumpFile.absolutePath, errorRef.ptr)

    appendLine()
    appendLine("LLVM module dump: ${moduleDumpFile.absolutePath}")

    konst modulePrintError = convertAndDisposeLlvmMessage(errorRef.konstue)
    if (printedWithErrors != 0) {
        appendLine("  (printed with errors: $modulePrintError)")
    }
}

private fun convertAndDisposeLlvmMessage(message: CPointer<ByteVar>?): String? =
        try {
            message?.toKString()
        } finally {
            LLVMDisposeMessage(message)
        }
