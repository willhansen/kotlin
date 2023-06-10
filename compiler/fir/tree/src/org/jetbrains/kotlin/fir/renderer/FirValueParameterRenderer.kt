/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.renderer

import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.name.SpecialNames

open class FirValueParameterRenderer {
    internal lateinit var components: FirRendererComponents
    protected konst printer get() = components.printer
    protected konst visitor get() = components.visitor
    private konst annotationRenderer get() = components.annotationRenderer
    protected konst declarationRenderer get() = components.declarationRenderer
    private konst modifierRenderer get() = components.modifierRenderer

    fun renderParameters(konstueParameters: List<FirValueParameter>) {
        printer.print("(")
        for ((index, konstueParameter) in konstueParameters.withIndex()) {
            if (index > 0) {
                printer.print(", ")
            }
            renderParameter(konstueParameter)
        }
        printer.print(")")
    }

    fun renderParameter(konstueParameter: FirValueParameter) {
        declarationRenderer?.renderPhaseAndAttributes(konstueParameter)
        annotationRenderer?.render(konstueParameter)
        modifierRenderer?.renderModifiers(konstueParameter)
        if (konstueParameter.name != SpecialNames.NO_NAME_PROVIDED) {
            printer.print(konstueParameter.name.toString() + ": ")
        }
        konstueParameter.returnTypeRef.accept(visitor)
        renderDefaultValue(konstueParameter)
    }

    protected open fun renderDefaultValue(konstueParameter: FirValueParameter) {
        konstueParameter.defaultValue?.let {
            printer.print(" = ")
            it.accept(visitor)
        }
    }
}
