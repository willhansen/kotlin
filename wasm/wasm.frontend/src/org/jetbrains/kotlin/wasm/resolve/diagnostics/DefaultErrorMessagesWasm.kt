/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.wasm.resolve.diagnostics

import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.Renderers

private konst DIAGNOSTIC_FACTORY_TO_RENDERER by lazy {
    DiagnosticFactoryToRendererMap("Wasm").apply {
        put(
            ErrorsWasm.NON_EXTERNAL_TYPE_EXTENDS_EXTERNAL_TYPE,
            "Non-external type extends external type {0}",
            Renderers.RENDER_TYPE
        )

        put(
            ErrorsWasm.WRONG_JS_INTEROP_TYPE, "Type {1} cannot be used in {0}. " +
                    "Only external, primitive, string and function types are supported in Kotlin/Wasm JS interop.",
            CommonRenderers.STRING,
            Renderers.RENDER_TYPE
        )

        put(ErrorsWasm.NESTED_WASM_IMPORT, "Only top-level functions can be imported with @WasmImport")
        put(ErrorsWasm.WASM_IMPORT_ON_NON_EXTERNAL_DECLARATION, "Functions annotated with @WasmImport must be external")
        put(ErrorsWasm.WASM_IMPORT_PARAMETER_DEFAULT_VALUE, "Default parameter konstues are not supported with @WasmImport")
        put(ErrorsWasm.WASM_IMPORT_VARARG_PARAMETER, "Vararg parameters are not supported with @WasmImport")
        put(ErrorsWasm.WASM_IMPORT_UNSUPPORTED_PARAMETER_TYPE, "Unsupported @WasmImport parameter type {0}", Renderers.RENDER_TYPE)
        put(ErrorsWasm.WASM_IMPORT_UNSUPPORTED_RETURN_TYPE, "Unsupported @WasmImport return type {0}", Renderers.RENDER_TYPE)

        put(ErrorsWasm.WRONG_JS_FUN_TARGET, "Only top-level external functions can be implemented using @JsFun")

        put(
            ErrorsWasm.JSCODE_WRONG_CONTEXT,
            "Calls to js(code) should be a single expression inside a top-level function body or a property initializer in Kotlin/Wasm"
        )
        put(
            ErrorsWasm.JSCODE_UNSUPPORTED_FUNCTION_KIND,
            "Calls to js(code) are not supported in {0} in Kotlin/Wasm",
            CommonRenderers.STRING
        )
        put(
            ErrorsWasm.JSCODE_INVALID_PARAMETER_NAME,
            "Parameters passed to js(code) should have a konstid JavaScript name"
        )

    }
}

class DefaultErrorMessagesWasm : DefaultErrorMessages.Extension {
    override fun getMap(): DiagnosticFactoryToRendererMap = DIAGNOSTIC_FACTORY_TO_RENDERER
}