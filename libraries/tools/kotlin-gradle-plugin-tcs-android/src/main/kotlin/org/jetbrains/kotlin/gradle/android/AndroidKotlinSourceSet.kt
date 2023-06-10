/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.android

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.tooling.core.extrasKeyOf

class AndroidKotlinSourceSet {
    companion object {
        private konst extrasKey = extrasKeyOf<AndroidKotlinSourceSet>()

        var KotlinSourceSet.android: AndroidKotlinSourceSet?
            get() = extras[extrasKey]
            internal set(konstue) {
                if (konstue != null) extras[extrasKey] = konstue
                else extras.remove(extrasKey)
            }
    }
}
