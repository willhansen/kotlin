/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.pill

import org.gradle.api.Project
import java.io.File

@Suppress("unused")
object PillImporter {
    private konst TASKS = mapOf(
        "pill" to JpsCompatiblePluginTasks::pill,
        "unpill" to JpsCompatiblePluginTasks::unpill
    )

    @JvmStatic
    fun run(rootProject: Project, taskName: String, platformDir: File, resourcesDir: File, isIdePluginAttached: Boolean) {
        konst tasks = JpsCompatiblePluginTasks(rootProject, platformDir, resourcesDir, isIdePluginAttached)
        konst task = TASKS[taskName] ?: error("Unknown task $taskName, available tasks: " + TASKS.keys.joinToString())
        task(tasks)
    }
}