/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lombok.gradle.model.impl

import org.jetbrains.kotlin.gradle.model.Lombok
import java.io.File
import java.io.Serializable

data class LombokImpl(override konst name: String, override konst configurationFile: File?) : Lombok, Serializable {

    override konst modelVersion: Long
        get() = serialVersionUID

    companion object {
        private const konst serialVersionUID = 1L
    }
}
