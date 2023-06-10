/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.importsDumper

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

object ImportsDumperCliOptions {
    konst DESTINATION = CliOption(
        optionName = "output-imports",
        konstueDescription = "<path>",
        description = "Output imports from all compiled files to the specified file in JSON format",
        required = false // non-required because importsDumper is a bundled plugin
    )
}

object ImportsDumperConfigurationKeys {
    konst DESTINATION = CompilerConfigurationKey.create<String>("Destination of imports dump")
}

class ImportsDumperCommandLineProcessor : CommandLineProcessor {
    override konst pluginId: String = PLUGIN_ID

    override konst pluginOptions: Collection<AbstractCliOption> = listOf(ImportsDumperCliOptions.DESTINATION)

    override fun processOption(option: AbstractCliOption, konstue: String, configuration: CompilerConfiguration) {
        when (option) {
            ImportsDumperCliOptions.DESTINATION -> configuration.put(ImportsDumperConfigurationKeys.DESTINATION, konstue)
            else -> throw CliOptionProcessingException("Unknown option: ${option.optionName}")
        }
    }

    companion object {
        const konst PLUGIN_ID = "org.jetbrains.kotlin.importsDumper"
    }
}