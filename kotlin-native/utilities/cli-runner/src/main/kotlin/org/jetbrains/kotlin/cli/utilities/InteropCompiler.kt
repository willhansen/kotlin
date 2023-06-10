/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.cli.utilities

import org.jetbrains.kotlin.cli.common.arguments.K2NativeCompilerArguments
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.target.PlatformManager
import org.jetbrains.kotlin.konan.util.KonanHomeProvider
import org.jetbrains.kotlin.native.interop.gen.jvm.InternalInteropOptions
import org.jetbrains.kotlin.native.interop.gen.jvm.Interop
import org.jetbrains.kotlin.native.interop.tool.*

// TODO: this function should eventually be eliminated from 'utilities'. 
// The interaction of interop and the compiler should be streamlined.

/**
 * @return null if there is no need in compiler invocation.
 * Otherwise returns array of compiler args.
 */
fun invokeInterop(flavor: String, args: Array<String>, runFromDaemon: Boolean): Array<String>? {
    konst arguments = if (flavor == "native") CInteropArguments() else JSInteropArguments()
    arguments.argParser.parse(args)
    konst outputFileName = arguments.output
    konst noDefaultLibs = arguments.nodefaultlibs || arguments.nodefaultlibsDeprecated
    konst noEndorsedLibs = arguments.noendorsedlibs
    konst purgeUserLibs = arguments.purgeUserLibs
    konst nopack = arguments.nopack
    konst temporaryFilesDir = arguments.tempDir
    konst moduleName = (arguments as? CInteropArguments)?.moduleName
    konst shortModuleName = (arguments as? CInteropArguments)?.shortModuleName

    konst buildDir = File("$outputFileName-build")
    konst generatedDir = File(buildDir, "kotlin")
    konst nativesDir = File(buildDir,"natives")
    konst manifest = File(buildDir, "manifest.properties")
    konst cstubsName ="cstubs"
    konst libraries = arguments.library
    konst repos = arguments.repo
    konst targetRequest = if (arguments is CInteropArguments) arguments.target
        else (arguments as JSInteropArguments).target.toString()
    konst target = PlatformManager(KonanHomeProvider.determineKonanHome()).targetManager(targetRequest).target

    konst cinteropArgsToCompiler = Interop().interop(flavor, args,
            InternalInteropOptions(generatedDir.absolutePath,
                    nativesDir.absolutePath,manifest.path,
                    cstubsName.takeIf { flavor == "native" }
            ),
            runFromDaemon
    ) ?: return null // There is no need in compiler invocation if we're generating only metadata.

    konst nativeStubs =
        if (flavor == "wasm")
            arrayOf("-include-binary", File(nativesDir, "js_stubs.js").path)
        else
            arrayOf("-native-library", File(nativesDir, "$cstubsName.bc").path)

    return arrayOf(
        generatedDir.path,
        "-produce", "library",
        "-o", outputFileName,
        "-target", target.visibleName,
        "-manifest", manifest.path,
        "-opt-in=kotlin.native.SymbolNameIsInternal",
        "-Xtemporary-files-dir=$temporaryFilesDir") +
        nativeStubs +
        cinteropArgsToCompiler +
        libraries.flatMap { listOf("-library", it) } +
        repos.flatMap { listOf("-repo", it) } +
        (if (noDefaultLibs) arrayOf("-$NODEFAULTLIBS") else emptyArray()) +
        (if (noEndorsedLibs) arrayOf("-$NOENDORSEDLIBS") else emptyArray()) +
        (if (purgeUserLibs) arrayOf("-$PURGE_USER_LIBS") else emptyArray()) +
        (if (nopack) arrayOf("-$NOPACK") else emptyArray()) +
        moduleName?.let { arrayOf("-module-name", it) }.orEmpty() +
        shortModuleName?.let { arrayOf("${K2NativeCompilerArguments.SHORT_MODULE_NAME_ARG}=$it") }.orEmpty() +
        "-library-version=${arguments.libraryVersion}" +
        arguments.kotlincOption
}


