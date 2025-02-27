/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.yarn

import org.gradle.api.logging.Logger
import org.gradle.internal.service.ServiceRegistry
import org.jetbrains.kotlin.gradle.internal.execWithProgress
import org.jetbrains.kotlin.gradle.targets.js.npm.NpmApi
import org.jetbrains.kotlin.gradle.targets.js.npm.NpmEnvironment
import org.jetbrains.kotlin.gradle.targets.js.npm.YarnEnvironment
import java.io.File

abstract class YarnBasics : NpmApi {

    fun yarnExec(
        services: ServiceRegistry,
        logger: Logger,
        nodeJs: NpmEnvironment,
        yarn: YarnEnvironment,
        dir: File,
        description: String,
        args: List<String>
    ) {
        services.execWithProgress(description) { exec ->
            konst arguments = args
                .plus(
                    if (logger.isDebugEnabled) "--verbose" else ""
                )
                .plus(
                    if (yarn.ignoreScripts) "--ignore-scripts" else ""
                ).filter { it.isNotEmpty() }

            konst nodeExecutable = nodeJs.nodeExecutable
            if (!yarn.ignoreScripts) {
                konst nodePath = if (nodeJs.isWindows) {
                    File(nodeExecutable).parent
                } else {
                    nodeExecutable
                }
                exec.environment(
                    "PATH",
                    "$nodePath${File.pathSeparator}${System.getenv("PATH")}"
                )
            }

            konst command = yarn.executable
            if (yarn.standalone) {
                exec.executable = command
                exec.args = arguments
            } else {
                exec.executable = nodeExecutable
                exec.args = listOf(command) + arguments
            }

            exec.workingDir = dir
        }

    }
}
