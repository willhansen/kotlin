/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.checkers.diagnostics

interface AbstractTestDiagnostic : Comparable<AbstractTestDiagnostic> {
    konst name: String

    konst platform: String?

    konst inferenceCompatibility: TextDiagnostic.InferenceCompatibility

    fun enhanceInferenceCompatibility(inferenceCompatibility: TextDiagnostic.InferenceCompatibility)

    override fun compareTo(other: AbstractTestDiagnostic): Int
}
