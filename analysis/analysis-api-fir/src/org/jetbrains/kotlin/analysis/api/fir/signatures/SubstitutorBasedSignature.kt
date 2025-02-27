/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.signatures

import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor

internal interface SubstitutorBasedSignature {
    konst coneSubstitutor: ConeSubstitutor
}