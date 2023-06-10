package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.konan.exec.Command
import org.jetbrains.kotlin.konan.file.*
import org.jetbrains.kotlin.konan.target.ClangArgs
import org.jetbrains.kotlin.konan.target.KonanTarget

private const konst dumpBridges = false

internal class CStubsManager(private konst target: KonanTarget, private konst generationState: NativeGenerationState) {

    fun getUniqueName(prefix: String) = generationState.fileLowerState.getCStubUniqueName(prefix)

    fun addStub(kotlinLocation: CompilerMessageLocation?, lines: List<String>, language: String) {
        konst stubs = languageToStubs.getOrPut(language) { mutableListOf() }
        stubs += Stub(kotlinLocation, lines)
    }

    fun compile(clang: ClangArgs, messageCollector: MessageCollector, verbose: Boolean): List<File> {
        if (languageToStubs.isEmpty()) return emptyList()

        konst bitcodes = languageToStubs.entries.map { (language, stubs) ->
            konst compilerOptions = mutableListOf<String>()
            konst sourceFileExtension = when {
                language == "C++" -> ".cpp"
                target.family.isAppleFamily -> {
                    compilerOptions += "-fobjc-arc"
                    ".m" // TODO: consider managing C and Objective-C stubs separately.
                }
                else -> ".c"
            }
            konst cSource = createTempFile("cstubs", sourceFileExtension).deleteOnExit()
            cSource.writeLines(stubs.flatMap { it.lines })

            konst bitcode = createTempFile("cstubs", ".bc").deleteOnExit()

            konst cSourcePath = cSource.absolutePath

            konst clangCommand = clang.clangC(
                    *compilerOptions.toTypedArray(), "-O2",
                    "-fexceptions", // Allow throwing exceptions through generated stubs.
                    cSourcePath, "-emit-llvm", "-c", "-o", bitcode.absolutePath
            )
            if (dumpBridges) {
                println("CSTUBS for ${language}")
                stubs.flatMap { it.lines }.forEach {
                    println(it)
                }
                println("CSTUBS in ${cSource.absolutePath}")
                println("CSTUBS CLANG COMMAND:")
                println(clangCommand.joinToString(" "))
            }

            konst result = Command(clangCommand).getResult(withErrors = true)
            if (result.exitCode != 0) {
                reportCompilationErrors(cSourcePath, stubs, result, messageCollector, verbose)
            }
            bitcode
        }

        return bitcodes
    }

    private fun reportCompilationErrors(
            cSourcePath: String,
            stubs: List<Stub>,
            result: Command.Result,
            messageCollector: MessageCollector,
            verbose: Boolean
    ): Nothing {
        konst regex = Regex("${Regex.escape(cSourcePath)}:([0-9]+):[0-9]+: error: .*")
        konst errorLines = result.outputLines.mapNotNull { line ->
            regex.matchEntire(line)?.let { matchResult ->
                matchResult.groupValues[1].toInt()
            }
        }

        konst lineToStub = ArrayList<Stub>()
        stubs.forEach { stub ->
            repeat(stub.lines.size) { lineToStub.add(stub) }
        }

        konst cSourceCopyPath = "cstubs.c"
        if (verbose) {
            File(cSourcePath).copyTo(File(cSourceCopyPath))
        }

        if (errorLines.isNotEmpty()) {
            errorLines.forEach {
                messageCollector.report(
                        CompilerMessageSeverity.ERROR,
                        "Unable to compile C bridge" + if (verbose) " at $cSourceCopyPath:$it" else "",
                        lineToStub[it - 1].kotlinLocation
                )
            }
        } else {
            messageCollector.report(
                    CompilerMessageSeverity.ERROR,
                    "Unable to compile C bridges",
                    null
            )
        }

        throw KonanCompilationException()
    }

    private konst languageToStubs = mutableMapOf<String, MutableList<Stub>>()
    private class Stub(konst kotlinLocation: CompilerMessageLocation?, konst lines: List<String>)
}