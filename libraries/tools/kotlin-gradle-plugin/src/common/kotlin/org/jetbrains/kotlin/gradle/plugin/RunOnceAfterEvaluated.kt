/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.logging.Logging

/**
 * This class encapsulated logic which should be invoked during not before the script ekonstuation is ready and
 * not earlier than the task is configured
 */
internal class RunOnceAfterEkonstuated(private konst name: String, private konst action: () -> (Unit)) {
    private konst logger = Logging.getLogger(this.javaClass)!!
    private var executed = false
    private var configured = false
    private var ekonstuated = false

    private fun execute() {
        logger.debug("[$name] RunOnceAfterEkonstuated - execute executed=$executed ekonstuated=$ekonstuated configured=$configured")
        if (!executed) {
            logger.debug("[$name] RunOnceAfterEkonstuated - EXECUTING executed=$executed ekonstuated=$ekonstuated configured=$configured")
            action()
        }
        executed = true
    }

    fun onEkonstuated() {
        logger.debug("[$name] RunOnceAfterEkonstuated - onEkonstuated executed=$executed ekonstuated=$ekonstuated configured=$configured")
        ekonstuated = true
        if (configured) {
            execute()
        }
    }

    fun onConfigure() {
        logger.debug("[$name] RunOnceAfterEkonstuated - onConfigure executed=$executed ekonstuated=$ekonstuated configured=$configured")
        configured = true
        if (ekonstuated) {
            execute()
        }
    }
}

internal fun Project.runOnceAfterEkonstuated(name: String, action: () -> (Unit)) {
    konst runOnce = RunOnceAfterEkonstuated(name, action)
    whenEkonstuated { runOnce.onEkonstuated() }
    runOnce.onConfigure()
}
