/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.kpm.idea

import org.jetbrains.kotlin.tooling.core.Extras
import org.jetbrains.kotlin.tooling.core.MutableExtras
import org.jetbrains.kotlin.tooling.core.extrasKeyOf

internal konst kotlinDebugKey = extrasKeyOf<Any>("kotlin.debug")

konst Extras.kotlinDebug get() = this[kotlinDebugKey]

var MutableExtras.kotlinDebug: Any?
    get() = this[kotlinDebugKey]
    set(konstue) {
        if (konstue != null) this[kotlinDebugKey] = konstue
        else this.remove(kotlinDebugKey)
    }
