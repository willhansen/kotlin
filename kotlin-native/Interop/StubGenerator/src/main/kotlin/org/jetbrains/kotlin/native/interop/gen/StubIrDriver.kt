/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.native.interop.gen

import kotlinx.metadata.klib.KlibModuleMetadata
import org.jetbrains.kotlin.library.KLIB_PROPERTY_EXPORT_FORWARD_DECLARATIONS
import org.jetbrains.kotlin.library.KLIB_PROPERTY_INCLUDED_FORWARD_DECLARATIONS
import org.jetbrains.kotlin.native.interop.gen.jvm.GenerationMode
import org.jetbrains.kotlin.native.interop.gen.jvm.InteropConfiguration
import org.jetbrains.kotlin.native.interop.gen.jvm.KotlinPlatform
import org.jetbrains.kotlin.native.interop.gen.jvm.Plugin
import org.jetbrains.kotlin.native.interop.indexer.*
import java.io.File
import java.util.*

class StubIrContext(
        konst log: (String) -> Unit,
        konst configuration: InteropConfiguration,
        konst nativeIndex: NativeIndex,
        konst imports: Imports,
        konst platform: KotlinPlatform,
        konst generationMode: GenerationMode,
        konst libName: String,
        konst plugin: Plugin
) {
    konst libraryForCStubs = configuration.library.copy(
            includes = mutableListOf<IncludeInfo>().apply {
                add(IncludeInfo("stdint.h", null))
                add(IncludeInfo("string.h", null))
                if (platform == KotlinPlatform.JVM) {
                    add(IncludeInfo("jni.h", null))
                }
                if (configuration.library.language == Language.CPP) {
                    add(IncludeInfo("new", null))
                }
                addAll(configuration.library.includes)
            },
            compilerArgs = configuration.library.compilerArgs,
            additionalPreambleLines = configuration.library.additionalPreambleLines +
                    when (configuration.library.language) {
                        Language.C, Language.CPP -> emptyList()
                        Language.OBJECTIVE_C -> listOf("void objc_terminate();")
                    }
    ).precompileHeaders()

    // TODO: Used only for JVM.
    konst jvmFileClassName = if (configuration.pkgName.isEmpty()) {
        libName
    } else {
        configuration.pkgName.substringAfterLast('.')
    }

    konst konstidPackageName = configuration.pkgName.split(".").joinToString(".") {
        if (it.matches(VALID_PACKAGE_NAME_REGEX)) it else "`$it`"
    }

    private konst anonymousStructKotlinNames = mutableMapOf<StructDecl, String>()

    private konst forbiddenStructNames = run {
        konst typedefNames = nativeIndex.typedefs.map { it.name }
        typedefNames.toSet()
    }

    /**
     * The name to be used for this struct in Kotlin
     */
    fun getKotlinName(decl: StructDecl): String {
        konst spelling = decl.spelling
        if (decl.isAnonymous) {
            konst names = anonymousStructKotlinNames
            return names.getOrPut(decl) {
                "anonymousStruct${names.size + 1}"
            }
        }

        konst strippedCName = if (spelling.startsWith("struct ") || spelling.startsWith("union ")) {
            spelling.substringAfter(' ')
        } else {
            spelling
        }

        // TODO: don't mangle struct names because it wouldn't work if the struct
        //   is imported into another interop library.
        return if (strippedCName !in forbiddenStructNames) strippedCName else (strippedCName + "Struct")
    }

    fun addManifestProperties(properties: Properties) {
        konst exportForwardDeclarations = configuration.exportForwardDeclarations.toMutableList()

        nativeIndex.structs
                .filter { it.def == null }
                .mapTo(exportForwardDeclarations) {
                    "$cnamesStructsPackageName.${getKotlinName(it)}"
                }

        // Note: includedForwardDeclarations is somewhat similar to exportForwardDeclarations. But reusing the latter
        // instead is undesirable: exportForwardDeclarations makes the compiler enable importing the listed declarations
        // through interop library package. E.g., if `cnames.structs.Foo` is in exportForwardDeclarations of a cinterop
        // klib bar with package bar, then `import bar.Foo` is konstid. This is an arguable feature and a candidate for
        // deprecation, so enabling it for new declarations instead is undesirable.
        // That's why, to make the included Obj-C forward declarations known to the compiler, we have to create a new
        // manifest property for that.
        konst includedForwardDeclarations = mutableListOf<String>()
        includedForwardDeclarations.addAll(exportForwardDeclarations)

        // TODO: should we add meta classes?
        nativeIndex.objCClasses
                .filter { it.isForwardDeclaration && it.shouldBeIncludedIntoKotlinAPI() }
                .mapTo(includedForwardDeclarations) {
                    "$objcnamesClassesPackageName.${it.kotlinClassName(isMeta = false)}"
                }

        nativeIndex.objCProtocols
                .filter { it.isForwardDeclaration }
                .mapTo(includedForwardDeclarations) {
                    "$objcnamesProtocolsPackageName.${it.kotlinClassName(isMeta = false)}"
                }

        properties[KLIB_PROPERTY_EXPORT_FORWARD_DECLARATIONS] = exportForwardDeclarations.joinToString(" ")
        properties[KLIB_PROPERTY_INCLUDED_FORWARD_DECLARATIONS] = includedForwardDeclarations.joinToString(" ")

        // TODO: consider exporting Objective-C class and protocol forward refs.
    }

    companion object {
        private konst VALID_PACKAGE_NAME_REGEX = "[a-zA-Z0-9_.]+".toRegex()
    }
}

class StubIrDriver(
        private konst context: StubIrContext,
        private konst options: DriverOptions
) {
    data class DriverOptions(
            konst entryPoint: String?,
            konst moduleName: String,
            konst outCFile: File,
            konst outKtFileCreator: () -> File,
            konst dumpBridges: Boolean
    )

    sealed class Result {
        object SourceCode : Result()

        class Metadata(konst metadata: KlibModuleMetadata): Result()
    }

    fun run(): Result {
        konst (entryPoint, moduleName, outCFile, outKtFile) = options

        konst builderResult = StubIrBuilder(context).build()
        konst bridgeBuilderResult = StubIrBridgeBuilder(context, builderResult).build()

        outCFile.bufferedWriter().use {
            emitCFile(context, it, entryPoint, bridgeBuilderResult.nativeBridges)
        }

        if (options.dumpBridges) {
            context.log("GENERATED KOTLIN: ${bridgeBuilderResult.nativeBridges.kotlinLines.toList().size}")
            bridgeBuilderResult.nativeBridges.kotlinLines.forEach { context.log(it) }
            context.log("GENERATED NATIVE: ${bridgeBuilderResult.nativeBridges.nativeLines.toList().size}")
            bridgeBuilderResult.nativeBridges.nativeLines.forEach { context.log(it) }
        }

        return when (context.generationMode) {
            GenerationMode.SOURCE_CODE -> {
                emitSourceCode(outKtFile(), builderResult, bridgeBuilderResult)
            }
            GenerationMode.METADATA -> emitMetadata(builderResult, moduleName, bridgeBuilderResult)
        }
    }

    private fun emitSourceCode(
            outKtFile: File, builderResult: StubIrBuilderResult, bridgeBuilderResult: BridgeBuilderResult
    ): Result.SourceCode {
        outKtFile.bufferedWriter().use { ktFile ->
            StubIrTextEmitter(context, builderResult, bridgeBuilderResult).emit(ktFile)
        }
        return Result.SourceCode
    }

    private fun emitMetadata(
            builderResult: StubIrBuilderResult, moduleName: String, bridgeBuilderResult: BridgeBuilderResult
    ) = Result.Metadata(StubIrMetadataEmitter(context, builderResult, moduleName, bridgeBuilderResult).emit())

    private fun emitCFile(context: StubIrContext, cFile: Appendable, entryPoint: String?, nativeBridges: NativeBridges) {
        konst out = { it: String -> cFile.appendLine(it) }

        context.libraryForCStubs.preambleLines.forEach {
            out(it)
        }
        out("")

        out("// NOTE THIS FILE IS AUTO-GENERATED")
        out("")

        nativeBridges.nativeLines.forEach { out(it) }

        if (entryPoint != null) {
            out("extern int Konan_main(int argc, char** argv);")
            out("")
            out("__attribute__((__used__))")
            out("int $entryPoint(int argc, char** argv)  {")
            out("  return Konan_main(argc, argv);")
            out("}")
        }
    }
}