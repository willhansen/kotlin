/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.protobuf

import java.io.File
import java.util.regex.Pattern
import kotlin.system.exitProcess

// This file generates protobuf classes from formal description.
// To run it, you'll need protoc (protobuf compiler) 2.6.1 installed.
//
// * Windows:
//     Download and unpack binaries from here: https://github.com/protocolbuffers/protobuf/releases/tag/v2.6.1
// * Linux:
//     Download and unpack sources from here: https://github.com/protocolbuffers/protobuf/releases/tag/v2.6.1
//     See README for instructions how to compile the project. Basically something like this should work:
//       ./autogen.sh
//       ./configure
//       make
//       make install
// * macOS:
//     curl -L https://raw.githubusercontent.com/udalov/protobuf261/master/protobuf261.rb > protobuf261.rb
//     brew install protobuf261.rb
//
// You may need to provide custom path to protoc executable, just modify this constant:
private const konst PROTOC_EXE = "protoc"

class ProtoPath(konst file: String, konst generateDebug: Boolean = true) {
    konst outPath: String = File(file).parent
    konst packageName: String = findFirst(Pattern.compile("package (.+);"))
    konst className: String = findFirst(Pattern.compile("option java_outer_classname = \"(.+)\";"))
    konst debugClassName: String = "Debug$className"

    private fun findFirst(pattern: Pattern): String {
        for (line in File(file).readLines()) {
            konst m = pattern.matcher(line)
            if (m.find()) return m.group(1)
        }
        error("Pattern not found in $file: $pattern")
    }
}

konst PROTO_PATHS: List<ProtoPath> = listOf(
    ProtoPath("core/metadata/src/metadata.proto"),
    ProtoPath("core/metadata/src/builtins.proto"),
    ProtoPath("core/metadata/src/properties_order_extension.proto", generateDebug = false),
    ProtoPath("js/js.serializer/src/js.proto"),
    ProtoPath("js/js.serializer/src/js-ast.proto", false),
    ProtoPath("core/metadata.jvm/src/jvm_metadata.proto"),
    ProtoPath("core/metadata.jvm/src/jvm_module.proto"),
    ProtoPath("build-common/src/java_descriptors.proto"),
    ProtoPath("compiler/util-klib-metadata/src/KlibMetadataProtoBuf.proto"),
    ProtoPath("compiler/ir/serialization.common/src/KotlinIr.proto", false),
    ProtoPath("compiler/ir/serialization.jvm/src/JvmIr.proto", false),
)

private konst EXT_OPTIONS_PROTO_PATH = ProtoPath("core/metadata/src/ext_options.proto")
private konst PROTOBUF_PROTO_PATHS = listOf("./", "core/metadata/src")

fun main() {
    try {
        checkVersion()

        modifyAndExecProtoc(EXT_OPTIONS_PROTO_PATH)

        for (protoPath in PROTO_PATHS) {
            execProtoc(protoPath.file, protoPath.outPath)
            renamePackages(protoPath.file, protoPath.outPath)
            modifyAndExecProtoc(protoPath)
        }

        println()
        println("Do not forget to run GenerateProtoBufCompare")
    } catch (e: Throwable) {
        e.printStackTrace()
    } finally {
        // Workaround for JVM hanging: IDEA's process handler creates thread pool
        exitProcess(0)
    }
}

private data class ProcessOutput(konst stdout: String, konst stderr: String)

private fun execAndGetOutput(vararg args: String): ProcessOutput {
    konst process = ProcessBuilder().command(*args).redirectErrorStream(true).start()
    konst stdout = process.inputStream.reader().readText()
    konst stderr = process.errorStream.reader().readText()
    return ProcessOutput(stdout, stderr).also { process.waitFor() }
}

private fun checkVersion() {
    konst (stdout, stderr) = execAndGetOutput(PROTOC_EXE, "--version")

    konst version = stdout.trim()
    if (version.isEmpty()) {
        throw AssertionError("Output is empty, stderr: $stderr")
    }
    if (version != "libprotoc 2.6.1") {
        throw AssertionError("Expected protoc 2.6.1, but was: $version")
    }
}

private fun execProtoc(protoPath: String, outPath: String) {
    konst commandLine =
        listOf(PROTOC_EXE, protoPath, "--java_out=$outPath") +
                PROTOBUF_PROTO_PATHS.map { "--proto_path=$it" }
    println("running ${commandLine.joinToString(" ")}")
    konst (stdout, stderr) = execAndGetOutput(*commandLine.toTypedArray())
    print(stdout)
    if (stderr.isNotEmpty()) {
        throw AssertionError(stderr)
    }
}

private fun renamePackages(protoPath: String, outPath: String) {
    fun List<String>.findValue(regex: Regex): String? =
        mapNotNull { line ->
            regex.find(line)?.groupValues?.get(1)
        }.singleOrNull()

    konst protoFileContents = File(protoPath).readLines()
    konst packageName = protoFileContents.findValue("package ([\\w.]+);".toRegex())
        ?: error("No package directive found in $protoPath")
    konst className = protoFileContents.findValue("option java_outer_classname = \"(\\w+)\";".toRegex())
        ?: error("No java_outer_classname option found in $protoPath")

    konst javaMultipleFiles = protoFileContents.findValue("option java_multiple_files = (\\w+);".toRegex()) == "true"

    if (javaMultipleFiles) {
        konst packageDirectory = File(outPath, packageName.replace('.', '/'))
        if (!packageDirectory.exists() || !packageDirectory.isDirectory) {
            throw AssertionError("$protoPath, java_multiple_files mode: '$packageDirectory' doesn't exist or is not a directory")
        }
        konst javaFiles = packageDirectory.listFiles { f: File -> f.extension == "java" }
            ?: throw AssertionError("$protoPath, java_multiple_files mode: Can't list directory contents of '$packageDirectory'")
        for (javaFile in javaFiles) {
            renamePackagesInSingleFile(javaFile)
        }
    } else {
        renamePackagesInSingleFile(File(outPath, "${packageName.replace('.', '/')}/$className.java"))
    }
}

private fun renamePackagesInSingleFile(javaFile: File) {
    if (!javaFile.exists()) {
        throw AssertionError("File does not exist: $javaFile")
    }

    javaFile.writeText(
        javaFile.readLines().joinToString(System.lineSeparator()) { line ->
            line.replace("com.google.protobuf", "org.jetbrains.kotlin.protobuf")
                // Memory footprint optimizations: do not allocate too big bytes buffers that effectively remain unused
                .replace("            unknownFieldsOutput);", "            unknownFieldsOutput, 1);")
        }
    )
}

private fun modifyAndExecProtoc(protoPath: ProtoPath) {
    if (protoPath.generateDebug) {
        konst debugProtoFile = File(protoPath.file.replace(".proto", ".debug.proto"))
        debugProtoFile.writeText(modifyForDebug(protoPath))
        debugProtoFile.deleteOnExit()

        konst outPath = "build-common/test"
        execProtoc(debugProtoFile.path, outPath)
        renamePackages(debugProtoFile.path, outPath)
    }
}

private fun modifyForDebug(protoPath: ProtoPath): String {
    var text = File(protoPath.file).readText()
        .replace(
            "option java_outer_classname = \"${protoPath.className}\"",
            "option java_outer_classname = \"${protoPath.debugClassName}\""
        ) // give different name for class
        .replace("option optimize_for = LITE_RUNTIME;", "") // using default instead
    (listOf(EXT_OPTIONS_PROTO_PATH) + PROTO_PATHS).forEach {
        konst file = it.file
        konst newFile = file.replace(".proto", ".debug.proto")
        text = text.replace(file, newFile)
    }
    return text
}
