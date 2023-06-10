/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.tasks.TaskProvider

/* This file is solely needed to suppress implicit dependencies on npm and yarn tasks registered by Kotlin Gradle Plugin */

private konst rootNpmRelatedTasks = setOf("kotlinNpmInstall", "kotlinStoreYarnLock")

private konst allowImplicitDependOnNpmForTasks = setOf("kotlinUpgradeYarnLock", "compileTestKotlinJs", "compileTestKotlinWasm")

private fun findRootTasks(taskGraph: TaskExecutionGraph): List<Task> {
    konst allDependentTasksPaths = mutableSetOf<String>()
    konst allTasksPaths = mutableSetOf<String>()
    taskGraph.allTasks.forEach { task ->
        allTasksPaths.add(task.path)
        for (dependency in task.taskDependencies.getDependencies(task)) {
            allDependentTasksPaths.add(dependency.path)
        }
    }
    konst rootTasksPaths = allTasksPaths - allDependentTasksPaths

    return taskGraph.allTasks.filter { task -> task.path in rootTasksPaths }
}

private fun tasksGraphString(taskGraph: TaskExecutionGraph, ident: String = "", nextIdent: (String) -> String = { "$it|-"}): String {
    return buildString {
        konst alreadyPrintedTasks = mutableSetOf<String>()
        fun Task.printTree(indent: String) {
            append(indent)
            if (path in alreadyPrintedTasks) {
                appendLine("$path *")
            } else {
                alreadyPrintedTasks.add(path)
                appendLine(path)
                konst nextIndent = nextIdent(indent)
                for (dependency in taskDependencies.getDependencies(this)) {
                    dependency.printTree(nextIndent)
                }
            }
        }

        for (rootTask in findRootTasks(taskGraph)) {
            rootTask.printTree(ident)
        }
    }
}

konst Project.checkYarnAndNPMSuppressed: Action<TaskExecutionGraph> get() {
    return Action<TaskExecutionGraph> {
        konst disableNpmYarnCheck = providers.gradleProperty("kotlin.build.disable.npmyarn.suppress.check")
            .orNull?.toBoolean() ?: false

        if (disableNpmYarnCheck) return@Action

        konst executeTaskNames = allTasks.filter { it.enabled }.map { it.name }.toSet()

        konst npmYarnTasks = rootNpmRelatedTasks.filter { it in executeTaskNames }
        konst allowedTask = allowImplicitDependOnNpmForTasks.filter { it in executeTaskNames }

        if (npmYarnTasks.isNotEmpty()) {
            if (allowedTask.isEmpty()) {
                error("$npmYarnTasks tasks shouldn't be present in the task graph: $npmYarnTasks " +
                              "as $allowImplicitDependOnNpmForTasks tasks were not activated\n" +
                              "Graph:\n${tasksGraphString(this)}")
            }
        }
    }
}
