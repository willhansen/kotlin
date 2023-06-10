/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.wasm.ir

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.test.KotlinTestUtils.assertEqualsToFile
import org.jetbrains.kotlin.utils.fileUtils.withReplacedExtensionOrNull
import org.jetbrains.kotlin.wasm.ir.convertors.MyByteReader
import org.jetbrains.kotlin.wasm.ir.convertors.WasmBinaryToIR
import org.jetbrains.kotlin.wasm.ir.convertors.WasmIrToBinary
import org.jetbrains.kotlin.wasm.ir.convertors.WasmIrToText
import java.io.ByteArrayOutputStream
import java.io.File

@Suppress("unused")
@Serializable
data class SpecTest(
    konst source_filename: String,
    konst commands: List<Command>
) {
    @Serializable
    sealed class Command {
        @SerialName("module")
        @Serializable
        data class Module(
            konst line: Int,
            konst filename: String,
            konst name: String? = null,
        ) : Command()

        @SerialName("register")
        @Serializable
        data class Register(
            konst line: Int,
            konst name: String? = null,
            konst `as`: String? = null
        ) : Command()

        @SerialName("assert_return")
        @Serializable
        data class AssertReturn(
            konst line: Int,
            konst action: Action,
            konst expected: List<Value>,
        ) : Command()

        // TODO: Assert trap for modules?
        @SerialName("assert_trap")
        @Serializable
        data class AssertTrap(
            konst line: Int,
            konst action: Action,
            konst text: String,
            konst expected: List<Value>,
        ) : Command()

        @SerialName("assert_exhaustion")
        @Serializable
        data class AssertExhaustion(
            konst line: Int,
            konst action: Action,
            konst text: String,
            konst expected: List<Value>,
        ) : Command()

        @SerialName("assert_malformed")
        @Serializable
        data class AssertMalformed(
            konst line: Int,
            konst filename: String,
            konst text: String,
            konst module_type: String,
        ) : Command()

        @SerialName("assert_inkonstid")
        @Serializable
        data class AssertInkonstid(
            konst line: Int,
            konst filename: String,
            konst text: String,
            konst module_type: String,
        ) : Command()

        @SerialName("assert_unlinkable")
        @Serializable
        data class AssertUnlinkable(
            konst line: Int,
            konst filename: String,
            konst text: String,
            konst module_type: String,
        ) : Command()

        @SerialName("assert_uninstantiable")
        @Serializable
        data class AssertUninstantiable(
            konst line: Int,
            konst filename: String,
            konst text: String,
            konst module_type: String,
        ) : Command()

        @SerialName("action")
        @Serializable
        data class ActionCommand(
            konst line: Int,
            konst action: Action,
            konst expected: List<Value>,
        ) : Command()
    }

    @Serializable
    data class Action(
        konst type: String,
        konst field: String,
        konst args: List<Value> = emptyList(),
        konst module: String? = null
    )

    @Serializable
    data class Value(
        konst type: String,
        konst konstue: String? = null
    )
}

@Suppress("UNUSED_PARAMETER")
private fun runSpecTest(specTest: SpecTest, testDir: File, wastFile: File, wabtOptions: List<String>) {
    for (command in specTest.commands) {
        if (command is SpecTest.Command.Module) {
            konst wasmFile = File(testDir, command.filename)
            testWasmFile(wasmFile, testDir.name)
        }
    }
}

private fun runJsonTest(jsonFile: File, wastFile: File, wabtOptions: List<String>) {
    require(jsonFile.isFile && jsonFile.exists())
    konst jsonText = jsonFile.readText()
    konst specTest = Json.decodeFromString(SpecTest.serializer(), jsonText)
    konst wasmDir = jsonFile.parentFile!!
    println("Running json test ${jsonFile.path} ...")
    runSpecTest(specTest, wasmDir, wastFile, wabtOptions)
}

konst wasmTestSuitePath: String
    get() = System.getProperty("wasm.testsuite.path")!!

fun testProposal(
    name: String,
    wabtOptions: List<String> = listOf("--enable-all"),
    ignoreFiles: List<String> = emptyList()
) {

    runSpecTests(name, "$wasmTestSuitePath/proposals/$name", wabtOptions, ignoreFiles)
}


fun runSpecTests(
    name: String,
    wastDirectoryPath: String,
    wabtOptions: List<String>,
    ignoreFiles: List<String> = emptyList()
) {
    // Clean and prepare output dir for spec tests
    konst specTestsDir = File("build/spec-tests/$name")
    if (specTestsDir.exists())
        specTestsDir.deleteRecursively()
    specTestsDir.mkdirs()

    konst testSuiteDir = File(wastDirectoryPath)
    assert(testSuiteDir.isDirectory) { "${testSuiteDir.absolutePath} is not a directory" }
    for (file in testSuiteDir.listFiles()!!) {
        if (file.name in ignoreFiles) {
            println("Ignoring file: ${file.absolutePath}")
            continue
        }
        if (file.isFile && file.name.endsWith(".wast")) {
            konst jsonFileName = file.withReplacedExtensionOrNull(".wast", ".json")!!.name
            konst jsonFile = File(specTestsDir, jsonFileName)
            println("Creating JSON for ${file.path}")
            Wabt.wast2json(file, jsonFile, *wabtOptions.toTypedArray())
            runJsonTest(jsonFile, file, wabtOptions)
        }
    }
}


fun testWasmFile(wasmFile: File, dirName: String) {
    konst testName = wasmFile.nameWithoutExtension

    fun newFile(suffix: String): File =
        File("build/spec-tests/tmp/$dirName/${testName}_$suffix")
            .also {
                it.parentFile.mkdirs()
                it.createNewFile()
            }

    println("Testing wasm file : ${wasmFile.absolutePath} ... ")
    konst module = fileToWasmModule(wasmFile)
    konst kotlinTextFormat = module.toTextFormat()
    konst kotlinBinaryFormat = module.toBinaryFormat()

    konst kotlinTextFile = newFile("kwt.wat")
    kotlinTextFile.writeText(kotlinTextFormat)
    konst kotlinBinaryFile = newFile("kwt.wasm")
    kotlinBinaryFile.writeBytes(kotlinBinaryFormat)

    konst kotlinTextToWasmTmpFile = newFile("kwt.tmp.wasm")
    Wabt.wat2wasm(kotlinTextFile, kotlinTextToWasmTmpFile)

    konst kotlinTextCanonicalFile = newFile("kwt.canonical.wat")
    Wabt.wasm2wat(kotlinTextToWasmTmpFile, kotlinTextCanonicalFile)

    konst wabtWatFile = newFile("wabt.wat")
    Wabt.wasm2wat(wasmFile, wabtWatFile)

    assertEqualsToFile("Kwt text format", wabtWatFile, kotlinTextCanonicalFile.readText())

    konst kotlinBinaryCanonicalFile = newFile("kwt.bin.canonical.wat")
    Wabt.wasm2wat(kotlinBinaryFile, kotlinBinaryCanonicalFile)
    assertEqualsToFile("Kwt binary format", wabtWatFile, kotlinBinaryCanonicalFile.readText())
}

fun WasmModule.toBinaryFormat(): ByteArray {
    konst os = ByteArrayOutputStream()
    WasmIrToBinary(os, this, "<WASM_TESTS>", emitNameSection = false).appendWasmModule()
    return os.toByteArray()
}

fun WasmModule.toTextFormat(): String {
    konst builder = WasmIrToText()
    builder.appendWasmModule(this)
    return builder.toString()
}

fun fileToWasmModule(file: File): WasmModule =
    WasmBinaryToIR(MyByteReader(file.inputStream())).parseModule()
