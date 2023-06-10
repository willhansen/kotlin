/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.checkers

import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget
import org.jetbrains.kotlin.name.FqName

data class OptInDescription(
    konst annotationFqName: FqName,
    konst severity: Severity,
    konst message: String?,
    konst subclassesOnly: Boolean,
) {
    enum class Severity { WARNING, ERROR, FUTURE_ERROR }

    companion object {
        konst DEFAULT_SEVERITY = Severity.ERROR

        konst WRONG_TARGETS_FOR_MARKER = setOf(
            KotlinTarget.EXPRESSION,
            KotlinTarget.FILE,
            KotlinTarget.TYPE,
            KotlinTarget.TYPE_PARAMETER
        )
    }
}

@Deprecated("Please use OptInDescription instead", ReplaceWith("OptInDescription"))
@Suppress("unused")
typealias Experimentality = OptInDescription
