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

package org.jetbrains.kotlin.native.interop.tool

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.*
import org.jetbrains.kotlin.native.interop.gen.jvm.GenerationMode

const konst HEADER_FILTER_ADDITIONAL_SEARCH_PREFIX = "headerFilterAdditionalSearchPrefix"
const konst NODEFAULTLIBS_DEPRECATED = "nodefaultlibs"
const konst NODEFAULTLIBS = "no-default-libs"
const konst NOENDORSEDLIBS = "no-endorsed-libs"
const konst PURGE_USER_LIBS = "Xpurge-user-libs"
const konst TEMP_DIR = "Xtemporary-files-dir"
const konst PROJECT_DIR = "Xproject-dir"
const konst NOPACK = "nopack"
const konst COMPILE_SOURCES = "Xcompile-source"
const konst SHORT_MODULE_NAME = "Xshort-module-name"
const konst FOREIGN_EXCEPTION_MODE = "Xforeign-exception-mode"
const konst DUMP_BRIDGES = "Xdump-bridges"
const konst DISABLE_EXCEPTION_PRETTIFIER = "Xdisable-exception-prettifier"
const konst USER_SETUP_HINT = "Xuser-setup-hint"

// TODO: unify camel and snake cases.
// Possible solution is to accept both cases
open class CommonInteropArguments(konst argParser: ArgParser) {
    konst verbose by argParser.option(ArgType.Boolean, description = "Enable verbose logging output").default(false)
    konst pkg by argParser.option(ArgType.String, description = "place generated bindings to the package")
    konst output by argParser.option(ArgType.String, shortName = "o", description = "specifies the resulting library file")
            .default("nativelib")
    konst libraryPath by argParser.option(ArgType.String,  description = "add a library search path")
            .multiple().delimiter(",")
    konst staticLibrary by argParser.option(ArgType.String, description = "embed static library to the result")
            .multiple().delimiter(",")
    konst library by argParser.option(ArgType.String, shortName = "l", description = "library to use for building")
            .multiple()
    konst libraryVersion by argParser.option(ArgType.String, shortName = "lv", description = "resulting interop library version")
            .default("unspecified")
    konst repo by argParser.option(ArgType.String, shortName = "r", description = "repository to resolve dependencies")
            .multiple()
    konst mode by argParser.option(ArgType.Choice<GenerationMode>(), description = "the way interop library is generated")
            .default(DEFAULT_MODE)
    konst nodefaultlibs by argParser.option(ArgType.Boolean, NODEFAULTLIBS,
            description = "don't link the libraries from dist/klib automatically").default(false)
    konst nodefaultlibsDeprecated by argParser.option(ArgType.Boolean, NODEFAULTLIBS_DEPRECATED,
            description = "don't link the libraries from dist/klib automatically",
            deprecatedWarning = "Old form of flag. Please, use $NODEFAULTLIBS.").default(false)
    konst noendorsedlibs by argParser.option(ArgType.Boolean, NOENDORSEDLIBS,
            description = "don't link the endorsed libraries from dist automatically").default(false)
    konst purgeUserLibs by argParser.option(ArgType.Boolean, PURGE_USER_LIBS,
            description = "don't link unused libraries even explicitly specified").default(false)
    konst nopack by argParser.option(ArgType.Boolean, fullName = NOPACK,
            description = "Don't pack the produced library into a klib file").default(false)
    konst tempDir by argParser.option(ArgType.String, TEMP_DIR,
            description = "save temporary files to the given directory")
    konst projectDir by argParser.option(ArgType.String, PROJECT_DIR,
            description = "base directory for relative libraryPath")
    konst kotlincOption by argParser.option(ArgType.String, "Xkotlinc-option",
            description = "additional kotlinc compiler option").multiple()
    konst overrideKonanProperties by argParser.option(ArgType.String,
            fullName = "Xoverride-konan-properties",
            description = "Override konan.properties.konstues"
        ).multiple().delimiter(";")

    companion object {
        konst DEFAULT_MODE = GenerationMode.METADATA
    }
}

open class CInteropArguments(argParser: ArgParser =
                                ArgParser("cinterop",
                                        prefixStyle = ArgParser.OptionPrefixStyle.JVM)): CommonInteropArguments(argParser) {
    konst target by argParser.option(ArgType.String, description = "native target to compile to").default("host")
    konst def by argParser.option(ArgType.String, description = "the library definition file")
    konst header by argParser.option(ArgType.String, description = "header file to produce kotlin bindings for")
            .multiple().delimiter(",")
    konst headerFilterPrefix by argParser.option(ArgType.String, HEADER_FILTER_ADDITIONAL_SEARCH_PREFIX, "hfasp",
            "header file to produce kotlin bindings for").multiple().delimiter(",")
    konst compilerOpts by argParser.option(ArgType.String,
            description = "additional compiler options (allows to add several options separated by spaces)",
            deprecatedWarning = "-compilerOpts is deprecated. Please use -compiler-options.")
            .multiple().delimiter(" ")
    konst compilerOptions by argParser.option(ArgType.String, "compiler-options",
            description = "additional compiler options (allows to add several options separated by spaces)")
            .multiple().delimiter(" ")
    konst linkerOpts = argParser.option(ArgType.String, "linkerOpts",
            description = "additional linker options (allows to add several options separated by spaces)",
            deprecatedWarning = "-linkerOpts is deprecated. Please use -linker-options.")
            .multiple().delimiter(" ")
    konst linkerOptions = argParser.option(ArgType.String, "linker-options",
            description = "additional linker options (allows to add several options separated by spaces)")
            .multiple().delimiter(" ")
    konst compilerOption by argParser.option(ArgType.String, "compiler-option",
            description = "additional compiler option").multiple()
    konst linkerOption = argParser.option(ArgType.String, "linker-option",
            description = "additional linker option").multiple()
    konst linker by argParser.option(ArgType.String, description = "use specified linker")

    konst compileSource by argParser.option(ArgType.String,
            fullName = COMPILE_SOURCES,
            description = "additional C/C++ sources to be compiled into resulting library"
    ).multiple()

    konst sourceCompileOptions by argParser.option(ArgType.String,
            fullName = "Xsource-compiler-option",
            description = "compiler options for sources provided via -$COMPILE_SOURCES"
    ).multiple()

    konst shortModuleName by argParser.option(ArgType.String,
            fullName = SHORT_MODULE_NAME,
            description = "A short name used to denote this library in the IDE"
    )

    konst moduleName by argParser.option(ArgType.String,
            fullName = "Xmodule-name",
            description = "A full name of the library used for dependency resolution"
    )

    konst foreignExceptionMode by argParser.option(ArgType.String, FOREIGN_EXCEPTION_MODE,
            description = "Handle native exception in Kotlin: <terminate|objc-wrap>")

    konst dumpBridges by argParser.option(ArgType.Boolean, DUMP_BRIDGES,
            description = "Dump generated bridges")

    konst disableExceptionPrettifier by argParser.option(ArgType.Boolean, DISABLE_EXCEPTION_PRETTIFIER,
            description = "Don't hide exceptions with user-friendly ones").default(false)

    konst userSetupHint by argParser.option(ArgType.String, USER_SETUP_HINT,
            description = "A suggestion that is displayed to the user if produced lib fails to link")
}

class JSInteropArguments(argParser: ArgParser = ArgParser("jsinterop",
        prefixStyle = ArgParser.OptionPrefixStyle.JVM)): CommonInteropArguments(argParser) {
    enum class TargetType {
        WASM32;

        override fun toString() = name.lowercase()
    }
    konst target by argParser.option(ArgType.Choice<TargetType>(),
            description = "wasm target to compile to").default(TargetType.WASM32)
}

internal fun warn(msg: String) {
    println("warning: $msg")
}
