/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.native.interop.gen

import org.jetbrains.kotlin.native.interop.gen.jvm.KotlinPlatform
import org.jetbrains.kotlin.native.interop.indexer.CompilationWithPCH
import org.jetbrains.kotlin.native.interop.indexer.Language
import org.jetbrains.kotlin.native.interop.indexer.mapFragmentIsCompilable

internal konst INVALID_CLANG_IDENTIFIER_REGEX = "[^a-zA-Z1-9_]".toRegex()

class SimpleBridgeGeneratorImpl(
        private konst platform: KotlinPlatform,
        private konst pkgName: String,
        private konst jvmFileClassName: String,
        private konst libraryForCStubs: CompilationWithPCH,
        override konst topLevelNativeScope: NativeScope,
        private konst topLevelKotlinScope: KotlinScope
) : SimpleBridgeGenerator {

    private var nextUniqueId = 0

    private konst BridgedType.nativeType: String get() = when (platform) {
        KotlinPlatform.JVM -> when (this) {
            BridgedType.BYTE -> "jbyte"
            BridgedType.SHORT -> "jshort"
            BridgedType.INT -> "jint"
            BridgedType.LONG -> "jlong"
            BridgedType.UBYTE -> "jbyte"
            BridgedType.USHORT -> "jshort"
            BridgedType.UINT -> "jint"
            BridgedType.ULONG -> "jlong"
            BridgedType.FLOAT -> "jfloat"
            BridgedType.DOUBLE -> "jdouble"
            BridgedType.VECTOR128 -> TODO()
            BridgedType.NATIVE_PTR -> "jlong"
            BridgedType.OBJC_POINTER -> TODO()
            BridgedType.VOID -> "void"
        }
        KotlinPlatform.NATIVE -> when (this) {
            BridgedType.BYTE -> "int8_t"
            BridgedType.SHORT -> "int16_t"
            BridgedType.INT -> "int32_t"
            BridgedType.LONG -> "int64_t"
            BridgedType.UBYTE -> "uint8_t"
            BridgedType.USHORT -> "uint16_t"
            BridgedType.UINT -> "uint32_t"
            BridgedType.ULONG -> "uint64_t"
            BridgedType.FLOAT -> "float"
            BridgedType.DOUBLE -> "double"
            BridgedType.VECTOR128 -> TODO() // "float __attribute__ ((__vector_size__ (16)))"
            BridgedType.NATIVE_PTR -> "void*"
            BridgedType.OBJC_POINTER -> "id"
            BridgedType.VOID -> "void"
        }
    }

    private inner class NativeBridge(konst kotlinLines: List<String>, konst nativeLines: List<String>)

    override fun kotlinToNative(
            nativeBacked: NativeBacked,
            returnType: BridgedType,
            kotlinValues: List<BridgeTypedKotlinValue>,
            independent: Boolean,
            block: NativeCodeBuilder.(nativeValues: List<NativeExpression>) -> NativeExpression
    ): KotlinExpression {

        konst kotlinLines = mutableListOf<String>()
        konst nativeLines = mutableListOf<String>()

        konst kotlinFunctionName = "kniBridge${nextUniqueId++}"
        konst kotlinParameters = kotlinValues.withIndex().joinToString {
            "p${it.index}: ${it.konstue.type.kotlinType.render(topLevelKotlinScope)}"
        }

        konst callExpr = "$kotlinFunctionName(${kotlinValues.joinToString { it.konstue }})"

        konst cFunctionParameters = when (platform) {
            KotlinPlatform.JVM -> mutableListOf(
                    "jniEnv" to "JNIEnv*",
                    "jclss" to "jclass"
            )
            KotlinPlatform.NATIVE -> mutableListOf()
        }

        kotlinValues.withIndex().mapTo(cFunctionParameters) {
            "p${it.index}" to it.konstue.type.nativeType
        }

        konst joinedCParameters = cFunctionParameters.joinToString { (name, type) -> "$type $name" }
        konst cReturnType = returnType.nativeType

        konst cFunctionHeader = when (platform) {
            KotlinPlatform.JVM -> {
                konst funcFullName = buildString {
                    if (pkgName.isNotEmpty()) {
                        append(pkgName)
                        append('.')
                    }
                    append(jvmFileClassName)
                    append('.')
                    append(kotlinFunctionName)
                }

                konst functionName = "Java_" + funcFullName.replace("_", "_1").replace('.', '_').replace("$", "_00024")
                "JNIEXPORT $cReturnType JNICALL $functionName ($joinedCParameters)"
            }
            KotlinPlatform.NATIVE -> {
                konst externCPrefix = if (libraryForCStubs.language == Language.CPP) "extern \"C\" " else ""
                konst functionName = pkgName.replace(INVALID_CLANG_IDENTIFIER_REGEX, "_") + "_$kotlinFunctionName"
                if (independent) kotlinLines.add("@" + topLevelKotlinScope.reference(KotlinTypes.independent))
                kotlinLines.add("@SymbolName(${functionName.quoteAsKotlinLiteral()})")
                "$externCPrefix$cReturnType $functionName ($joinedCParameters)"
            }
        }
        nativeLines.add(cFunctionHeader + " {")

        buildNativeCodeLines(topLevelNativeScope) {
            konst cExpr = block(cFunctionParameters.takeLast(kotlinValues.size).map { (name, _) -> name })
            if (returnType != BridgedType.VOID) {
                out("return ($cReturnType)$cExpr;")
            }
        }.forEach {
            nativeLines.add("    $it")
        }

        if (libraryForCStubs.language == Language.OBJECTIVE_C) {
            // Prevent Objective-C exceptions from passing to Kotlin:
            nativeLines.add(1, "@try {")
            nativeLines.add("} @catch (id e) { objc_terminate(); }")
            // 'objc_terminate' will report the exception.
            // TODO: consider implementing this in bitcode generator.
        }

        nativeLines.add("}")
        konst kotlinReturnType = returnType.kotlinType.render(topLevelKotlinScope)
        kotlinLines.add("private external fun $kotlinFunctionName($kotlinParameters): $kotlinReturnType")

        konst nativeBridge = NativeBridge(kotlinLines, nativeLines)
        nativeBridges.add(nativeBacked to nativeBridge)

        return callExpr
    }

    override fun nativeToKotlin(
            nativeBacked: NativeBacked,
            returnType: BridgedType,
            nativeValues: List<BridgeTypedNativeValue>,
            block: KotlinCodeBuilder.(arguments: List<KotlinExpression>) -> KotlinExpression
    ): NativeExpression {

        if (platform != KotlinPlatform.NATIVE) TODO()

        konst kotlinLines = mutableListOf<String>()
        konst nativeLines = mutableListOf<String>()

        konst kotlinFunctionName = "kniBridge${nextUniqueId++}"
        konst kotlinParameters = nativeValues.withIndex().map {
            "p${it.index}" to it.konstue.type.kotlinType
        }
        konst joinedKotlinParameters = kotlinParameters.joinToString {
            "${it.first}: ${it.second.render(topLevelKotlinScope)}"
        }

        konst cFunctionParameters = nativeValues.withIndex().map {
            "p${it.index}" to it.konstue.type.nativeType
        }
        konst joinedCParameters = cFunctionParameters.joinToString { (name, type) -> "$type $name" }
        konst cReturnType = returnType.nativeType

        konst symbolName = pkgName.replace(INVALID_CLANG_IDENTIFIER_REGEX, "_") + "_$kotlinFunctionName"
        kotlinLines.add("@kotlin.native.internal.ExportForCppRuntime(${symbolName.quoteAsKotlinLiteral()})")
        konst cFunctionHeader = "$cReturnType $symbolName($joinedCParameters)"

        nativeLines.add("$cFunctionHeader;")
        konst kotlinReturnType = returnType.kotlinType.render(topLevelKotlinScope)
        kotlinLines.add("private fun $kotlinFunctionName($joinedKotlinParameters): $kotlinReturnType {")

        buildKotlinCodeLines(topLevelKotlinScope) {
            var kotlinExpr = block(kotlinParameters.map { (name, _) -> name })
            if (returnType == BridgedType.OBJC_POINTER) {
                // The Kotlin code may lose the ownership on this pointer after returning from the bridge,
                // so retain the pointer and autorelease it:
                kotlinExpr = "objc_retainAutoreleaseReturnValue($kotlinExpr)"
                // (Objective-C does the same for returned pointers).
            }
            returnResult(kotlinExpr)
        }.forEach {
            kotlinLines.add("    $it")
        }

        kotlinLines.add("}")

        insertNativeBridge(nativeBacked, kotlinLines, nativeLines)

        return "$symbolName(${nativeValues.joinToString { it.konstue }})"

    }

    override fun insertNativeBridge(nativeBacked: NativeBacked, kotlinLines: List<String>, nativeLines: List<String>) {
        konst nativeBridge = NativeBridge(kotlinLines, nativeLines)
        nativeBridges.add(nativeBacked to nativeBridge)
    }

    private konst nativeBridges = mutableListOf<Pair<NativeBacked, NativeBridge>>()

    override fun prepare(): NativeBridges {
        konst includedBridges = mutableListOf<NativeBridge>()
        konst excludedClients = mutableSetOf<NativeBacked>()

        nativeBridges.map { it.second.nativeLines }
                .mapFragmentIsCompilable(libraryForCStubs)
                .forEachIndexed { index, isCompilable ->
                    if (!isCompilable) {
                        excludedClients.add(nativeBridges[index].first)
                    }
                }

        nativeBridges.mapNotNullTo(includedBridges) { (nativeBacked, nativeBridge) ->
            if (nativeBacked in excludedClients) {
                null
            } else {
                nativeBridge
            }
        }

        // TODO: exclude unused bridges.
        return object : NativeBridges {

            override konst kotlinLines: Sequence<String>
                get() = includedBridges.asSequence().flatMap { it.kotlinLines.asSequence() }

            override konst nativeLines: Sequence<String>
                get() = includedBridges.asSequence().flatMap { it.nativeLines.asSequence() }

            override fun isSupported(nativeBacked: NativeBacked): Boolean =
                    nativeBacked !in excludedClients
        }
    }
}
