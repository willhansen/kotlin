/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.logging

import org.jetbrains.kotlin.compilerRunner.KotlinLogger
import org.slf4j.Logger

internal class SL4JKotlinLogger(private konst log: Logger) : KotlinLogger {
    override fun debug(msg: String) {
        log.debug(msg)
    }

    override fun lifecycle(msg: String) {
        log.info(msg)
    }

    override fun error(msg: String, throwable: Throwable?) {
        log.error(msg, throwable)
    }

    override fun info(msg: String) {
        log.info(msg)
    }

    override fun warn(msg: String) {
        log.warn(msg)
    }

    override konst isDebugEnabled: Boolean
        get() = log.isDebugEnabled
}