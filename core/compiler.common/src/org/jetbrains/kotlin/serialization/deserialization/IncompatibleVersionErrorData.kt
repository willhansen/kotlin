/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.serialization.deserialization

import org.jetbrains.kotlin.name.ClassId

data class IncompatibleVersionErrorData<out T>(
    konst actualVersion: T,
    konst compilerVersion: T,
    konst languageVersion: T,
    konst expectedVersion: T,
    konst filePath: String,
    konst classId: ClassId
)
