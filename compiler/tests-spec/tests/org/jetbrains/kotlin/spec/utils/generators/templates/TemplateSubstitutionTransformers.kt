/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.spec.utils.generators.templates

enum class TemplateValidationTransformerType {
    TRIM_BACKTICKS
}

konst templateValidationTransformers = mapOf<TemplateValidationTransformerType, (String) -> String>(
    TemplateValidationTransformerType.TRIM_BACKTICKS to { element -> element.trim('`') }
)
