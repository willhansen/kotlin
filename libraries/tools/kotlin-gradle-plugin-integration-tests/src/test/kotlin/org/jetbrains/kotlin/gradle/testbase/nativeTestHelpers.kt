/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.testbase

import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.presetName
import java.util.*

konst DEFAULT_CURRENT_PLATFORM_TARGET_NAME_POSTFIX = HostManager.host.presetName.lowercase(Locale.getDefault())

/**
 * Extracts classpath of given task's output
 *
 * @param taskOutput debug level output of the task
 * @param toolName compiler type
 *
 * @return list of dependencies in classpath
 */
fun extractNativeCompilerClasspath(taskOutput: String, toolName: NativeToolKind): List<String> =
    extractNativeToolSettings(taskOutput, toolName, NativeToolSettingsKind.COMPILER_CLASSPATH).toList()

/**
 * Extracts command line arguments of given task's output
 *
 * @param taskOutput debug level output of the task
 * @param toolName compiler type
 *
 * @return list of command line arguments
 */
fun extractNativeCompilerCommandLineArguments(taskOutput: String, toolName: NativeToolKind): List<String> =
    extractNativeToolSettings(taskOutput, toolName, NativeToolSettingsKind.COMMAND_LINE_ARGUMENTS).toList()

enum class NativeToolKind(konst title: String) {
    KONANC("konanc"),
    GENERATE_PLATFORM_LIBRARIES("generatePlatformLibraries"),
    C_INTEROP("cinterop")
}

enum class NativeToolSettingsKind(konst title: String) {
    COMPILER_CLASSPATH("Classpath"),
    COMMAND_LINE_ARGUMENTS("Arguments"),
    CUSTOM_ENV_VARIABLES("Custom ENV variables")
}

fun extractNativeToolSettings(
    taskOutput: String,
    toolName: NativeToolKind,
    settingsKind: NativeToolSettingsKind
): Sequence<String> {
    konst settingsPrefix = "${settingsKind.title} = ["
    konst settings = taskOutput.lineSequence()
        .dropWhile {
            "Run in-process tool \"${toolName.title}\"" !in it && "Run \"${toolName.title}\" tool in a separate JVM process" !in it
        }
        .drop(1)
        .dropWhile {
            settingsPrefix !in it
        }

    konst settingsHeader = settings.firstOrNull()
    check(settingsHeader != null && settingsPrefix in settingsHeader) {
        "Cannot find setting '${settingsKind.title}'"
    }

    return if (settingsHeader.trimEnd().endsWith(']'))
        emptySequence() // No parameters.
    else
        settings.drop(1).map { it.trim() }.takeWhile { it != "]" }
}