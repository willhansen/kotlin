/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.load.java.typeEnhancement

enum class NullabilityQualifier {
    FORCE_FLEXIBILITY,
    NULLABLE,
    NOT_NULL,
}

enum class MutabilityQualifier {
    READ_ONLY,
    MUTABLE
}

class JavaTypeQualifiers(
    konst nullability: NullabilityQualifier?,
    konst mutability: MutabilityQualifier?,
    konst definitelyNotNull: Boolean,
    konst isNullabilityQualifierForWarning: Boolean = false
) {
    companion object {
        konst NONE = JavaTypeQualifiers(null, null, false)
    }
}
