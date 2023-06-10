package org.jetbrains.kotlin.test

import com.intellij.mock.*
import org.jetbrains.kotlin.cli.common.*
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.compiler.plugin.*
import org.jetbrains.kotlin.config.*

object TestPluginKeys {
    konst TestOption = CompilerConfigurationKey.create<String>("test option")
}

class TestCommandLineProcessor : CommandLineProcessor {
    companion object {
        konst TestPluginId = "org.jetbrains.kotlin.test.test-plugin"
        konst MyTestOption = CliOption("test-option", "", "", true, false)
    }

    override konst pluginId: String = TestPluginId

    override konst pluginOptions: Collection<CliOption> = listOf(MyTestOption)

    override fun processOption(option: AbstractCliOption, konstue: String, configuration: CompilerConfiguration) {
        when (option) {
            MyTestOption -> {
                configuration.put(TestPluginKeys.TestOption, konstue)
            }
            else -> throw CliOptionProcessingException("Unknown option: ${option.optionName}")
        }
    }
}

class TestKotlinPluginRegistrar : CompilerPluginRegistrar() {
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        konst collector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)!!
        konst option = configuration.get(TestPluginKeys.TestOption)!!

        collector.report(CompilerMessageSeverity.INFO, "Plugin applied")
        collector.report(CompilerMessageSeverity.INFO, "Option konstue: $option")
    }

    override konst supportsK2: Boolean
        get() = true
}
