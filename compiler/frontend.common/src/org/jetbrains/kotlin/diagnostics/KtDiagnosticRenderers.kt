/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.diagnostics

import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.diagnostics.rendering.ContextIndependentParameterRenderer
import org.jetbrains.kotlin.diagnostics.rendering.Renderer

object KtDiagnosticRenderers {
    konst NULLABLE_STRING = Renderer<String?> { it ?: "null" }

    konst TO_STRING = Renderer { element: Any? ->
        element.toString()
    }

    konst OPTIONAL_COLON_TO_STRING = Renderer { element: Any? ->
        konst string = element.toString()
        if (string.isNotEmpty()) ": $string" else ""
    }

    konst EMPTY = Renderer { _: Any? -> "" }

    konst VISIBILITY = Renderer { visibility: Visibility ->
        visibility.externalDisplayName
    }

    konst NOT_RENDERED = Renderer<Any?> {
        ""
    }

    konst FUNCTION_PARAMETERS = Renderer { hasValueParameters: Boolean -> if (hasValueParameters) "..." else "" }

    @Suppress("FunctionName")
    fun <T> COLLECTION(renderer: ContextIndependentParameterRenderer<T>): ContextIndependentParameterRenderer<Collection<T>> {
        return Renderer { list ->
            list.joinToString(prefix = "[", postfix = "]", separator = ", ", limit = 3, truncated = "...") {
                renderer.render(it)
            }
        }
    }
}
