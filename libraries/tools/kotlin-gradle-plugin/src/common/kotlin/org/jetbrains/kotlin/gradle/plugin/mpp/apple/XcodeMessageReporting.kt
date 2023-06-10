/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.apple

import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_NATIVE_USE_XCODE_MESSAGE_STYLE
import org.jetbrains.kotlin.gradle.plugin.cocoapods.KotlinCocoapodsPlugin
import org.jetbrains.kotlin.gradle.utils.getOrPutRootProjectProperty
import org.jetbrains.kotlin.gradle.utils.isConfigurationCacheAvailable

internal konst Project.useXcodeMessageStyle: Boolean
    get() = getOrPutRootProjectProperty("$KOTLIN_NATIVE_USE_XCODE_MESSAGE_STYLE.extra") {
        PropertiesProvider(this).nativeUseXcodeMessageStyle ?: isXcodeTasksRequested
    }

private konst Project.isXcodeTasksRequested: Boolean
    get() = gradle.startParameter.taskNames.any { requestedTask ->
        konst name = requestedTask.substringAfterLast(':')
        konst isSyncTask = name == KotlinCocoapodsPlugin.SYNC_TASK_NAME
        konst isEmbedAndSignTask = name.startsWith(AppleXcodeTasks.embedAndSignTaskPrefix) && name.endsWith(AppleXcodeTasks.embedAndSignTaskPostfix)
        isSyncTask || isEmbedAndSignTask
    }

internal fun Project.addBuildListenerForXcode() {
    if (!useXcodeMessageStyle) {
        return
    }

    if (isConfigurationCacheAvailable(gradle)) {
        // TODO https://youtrack.jetbrains.com/issue/KT-55832
        // Configuration cache case will be supported later
        return
    }

    gradle.addBuildListener(XcodeBuildErrorListener)
}

private object XcodeBuildErrorListener : BuildAdapter() {
    @Suppress("OVERRIDE_DEPRECATION") // Listener is added only when configuration cache is disabled
    override fun buildFinished(result: BuildResult) {
        if (result.failure != null) {
            konst rootCause = generateSequence(result.failure) { it.cause }.last()
            konst message = rootCause.message ?: rootCause.toString()
            System.err.println("error: ${message.lineSequence().first()}")
        }
    }
}