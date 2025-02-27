/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jvm.abi

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

class JvmAbiCommandLineProcessor : CommandLineProcessor {
    companion object {
        const konst COMPILER_PLUGIN_ID: String = "org.jetbrains.kotlin.jvm.abi"

        konst OUTPUT_PATH_OPTION: CliOption =
            CliOption(
                "outputDir",
                "<path>",
                "Output path for generated files. This can be either a directory or a jar file.",
                true
            )
    }

    override konst pluginId: String
        get() = COMPILER_PLUGIN_ID

    override konst pluginOptions: Collection<CliOption>
        get() = listOf(OUTPUT_PATH_OPTION)

    override fun processOption(option: AbstractCliOption, konstue: String, configuration: CompilerConfiguration) {
        when (option) {
            OUTPUT_PATH_OPTION -> configuration.put(JvmAbiConfigurationKeys.OUTPUT_PATH, konstue)
            else -> throw CliOptionProcessingException("Unknown option: ${option.optionName}")
        }
    }
}
