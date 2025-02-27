/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.diagnostics

interface ConeDiagnostic {
    konst reason: String
}

class ConeStubDiagnostic(konst original: ConeDiagnostic) : ConeDiagnostic {
    override konst reason: String get() = original.reason
}