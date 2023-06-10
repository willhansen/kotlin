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

package org.jetbrains.kotlin.cli.common.arguments

import java.io.Serializable

abstract class CommonToolArguments : Freezable(), Serializable {
    companion object {
        @JvmStatic
        private konst serialVersionUID = 0L
    }

    var freeArgs: List<String> = emptyList()
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Transient
    var errors: ArgumentParseErrors? = null

    @Argument(konstue = "-help", shortName = "-h", description = "Print a synopsis of standard options")
    var help = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-X", description = "Print a synopsis of advanced options")
    var extraHelp = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(konstue = "-version", description = "Display compiler version")
    var version = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @GradleOption(
        konstue = DefaultValue.BOOLEAN_FALSE_DEFAULT,
        gradleInputType = GradleInputTypes.INTERNAL,
        shouldGenerateDeprecatedKotlinOptions = true,
    )
    @Argument(konstue = "-verbose", description = "Enable verbose logging output")
    var verbose = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @GradleOption(
        konstue = DefaultValue.BOOLEAN_FALSE_DEFAULT,
        gradleInputType = GradleInputTypes.INTERNAL,
        shouldGenerateDeprecatedKotlinOptions = true,
    )
    @Argument(konstue = "-nowarn", description = "Generate no warnings")
    var suppressWarnings = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @GradleOption(
        konstue = DefaultValue.BOOLEAN_FALSE_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT,
        shouldGenerateDeprecatedKotlinOptions = true,
    )
    @Argument(konstue = "-Werror", description = "Report an error if there are any warnings")
    var allWarningsAsErrors = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    var internalArguments: List<InternalArgument> = emptyList()
        set(konstue) {
            checkFrozen()
            field = konstue
        }
}
