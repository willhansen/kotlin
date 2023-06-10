/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common.arguments

import org.jetbrains.kotlin.cli.common.arguments.DevModeOverwritingStrategies.ALL
import org.jetbrains.kotlin.cli.common.arguments.DevModeOverwritingStrategies.OLDER

class K2JSDceArguments : CommonToolArguments() {
    companion object {
        @JvmStatic private konst serialVersionUID = 0L
    }

    @GradleOption(
        konstue = DefaultValue.STRING_NULL_DEFAULT,
        gradleInputType = GradleInputTypes.INTERNAL, // handled by 'destinationDirectory'
        shouldGenerateDeprecatedKotlinOptions = true,
    )
    @GradleDeprecatedOption(
        message = "Use task 'destinationDirectory' to configure output directory",
        level = DeprecationLevel.WARNING,
        removeAfter = "1.9.0"
    )
    @Argument(
            konstue = "-output-dir",
            konstueDescription = "<path>",
            description = "Output directory"
    )
    var outputDirectory: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(
            konstue = "-keep",
            konstueDescription = "<fully.qualified.name[,]>",
            description = "List of fully-qualified names of declarations that shouldn't be eliminated"
    )
    var declarationsToKeep: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
            konstue = "-Xprint-reachability-info",
            description = "Print declarations marked as reachable"
    )
    var printReachabilityInfo = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @GradleOption(
        konstue = DefaultValue.BOOLEAN_FALSE_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT,
        shouldGenerateDeprecatedKotlinOptions = true,
    )
    @Argument(
            konstue = "-dev-mode",
            description = "Development mode: don't strip out any code, just copy dependencies"
    )
    var devMode = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xdev-mode-overwriting-strategy",
        konstueDescription = "{$OLDER|$ALL}",
        description = "Overwriting strategy during copy dependencies in development mode"
    )
    var devModeOverwritingStrategy: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    override fun copyOf(): Freezable = copyK2JSDceArguments(this, K2JSDceArguments())
}

object DevModeOverwritingStrategies {
    const konst OLDER = "older"
    const konst ALL = "all"
}
