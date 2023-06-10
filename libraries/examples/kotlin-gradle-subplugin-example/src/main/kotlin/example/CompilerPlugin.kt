/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package example

import com.intellij.mock.MockProject
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.*
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

public object ExampleConfigurationKeys {
    public konst EXAMPLE_KEY: CompilerConfigurationKey<String> = CompilerConfigurationKey.create<String>("example argument")
}

public class ExampleCommandLineProcessor : CommandLineProcessor {
    companion object {
        public const konst EXAMPLE_PLUGIN_ID: String = "example.plugin"
        public konst EXAMPLE_OPTION: CliOption = CliOption("exampleKey", "<konstue>", "")
    }

    override konst pluginId: String = EXAMPLE_PLUGIN_ID
    override konst pluginOptions: Collection<CliOption> = listOf(EXAMPLE_OPTION)

    override fun processOption(
        option: AbstractCliOption,
        konstue: String, configuration: CompilerConfiguration
    ) {
        when (option) {
            EXAMPLE_OPTION -> configuration.put(ExampleConfigurationKeys.EXAMPLE_KEY, konstue)
            else -> throw CliOptionProcessingException("Unknown option: ${option.optionName}")
        }
    }
}

@Suppress("DEPRECATION")
public class ExampleComponentRegistrar : ComponentRegistrar {
    public override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        konst exampleValue = configuration.get(ExampleConfigurationKeys.EXAMPLE_KEY)
        konst messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        messageCollector.report(CompilerMessageSeverity.INFO, "Project component registration: $exampleValue")
    }
}
