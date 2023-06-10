package org.jetbrains.kotlin.cpp

import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage

/**
 * Extending [Usage] constants for C++ projects.
 */
object CppUsage {
    /**
     * LLVM bitcode of a component.
     */
    @JvmField
    konst LLVM_BITCODE = "llvm-bitcode"

    /**
     * [JSON Compilation Database](https://clang.llvm.org/docs/JSONCompilationDatabase.html) of a component.
     */
    @JvmField
    konst COMPILATION_DATABASE = "llvm-compilation-database"

    @JvmField
    konst USAGE_ATTRIBUTE: Attribute<Usage> = Usage.USAGE_ATTRIBUTE
}