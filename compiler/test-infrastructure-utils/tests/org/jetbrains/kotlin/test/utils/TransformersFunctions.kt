/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.utils

import java.io.File

object TransformersFunctions {
    @JvmStatic
    konst replaceOptionalJvmInlineAnnotationWithReal = ReplacingSourceTransformer("OPTIONAL_JVM_INLINE_ANNOTATION", "@JvmInline")

    @JvmStatic
    konst replaceOptionalJvmInlineAnnotationWithUniversal = ReplacingSourceTransformer("OPTIONAL_JVM_INLINE_ANNOTATION", "@Suppress(\"OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE\") @kotlin.jvm.JvmInline")

    @JvmStatic
    konst removeOptionalJvmInlineAnnotation = ReplacingSourceTransformer("OPTIONAL_JVM_INLINE_ANNOTATION", "")

    object Android {
        konst forAll: List<(String) -> String> = listOf(
            replaceOptionalJvmInlineAnnotationWithReal,
        )
        konst forSpecificFile: Map<File, (String) -> String> = mapOf(
        )
    }
}