/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.ir2wasm

import org.jetbrains.kotlin.js.backend.JsToStringGenerationVisitor
import java.util.Base64

fun String.toJsStringLiteral(): CharSequence =
    JsToStringGenerationVisitor.javaScriptString(this)

data class JsModuleAndQualifierReference(
    konst module: String?,
    konst qualifier: String?,
) {
    konst jsVariableName = run {
        // Encode variable name as base64 to have a konstid unique JS identifier
        konst encoder = Base64.getEncoder().withoutPadding()
        konst moduleBase64 = module?.let { encoder.encodeToString(module.encodeToByteArray()) }.orEmpty()
        konst qualifierBase64 = qualifier?.let { encoder.encodeToString(qualifier.encodeToByteArray()) }.orEmpty()
        "_ref_${moduleBase64}_$qualifierBase64"
    }
}