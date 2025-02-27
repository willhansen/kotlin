/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.renderer

import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.isCatchParameter
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

open class FirDeclarationRenderer {

    internal lateinit var components: FirRendererComponents
    protected konst printer get() = components.printer
    private konst resolvePhaseRenderer get() = components.resolvePhaseRenderer
    private konst typeRenderer get() = components.typeRenderer

    fun render(declaration: FirDeclaration) {
        renderPhaseAndAttributes(declaration)
        if (declaration is FirConstructor) {
            declaration.dispatchReceiverType?.let {
                typeRenderer.render(it)
                printer.print(".")
            }
            printer.print("constructor")
            return
        }
        printer.print(
            when (declaration) {
                is FirRegularClass -> declaration.classKind.name.toLowerCaseAsciiOnly().replace("_", " ")
                is FirTypeAlias -> "typealias"
                is FirAnonymousFunction -> (declaration.label?.let { "${it.name}@" } ?: "") + "fun"
                is FirSimpleFunction -> "fun"
                is FirProperty -> {
                    if (declaration.isCatchParameter == true) {
                        ""
                    } else {
                        konst prefix = if (declaration.isLocal) "l" else ""
                        prefix + if (declaration.isVal) "konst" else "var"
                    }
                }
                is FirPropertyAccessor -> if (declaration.isGetter) "get" else "set"
                is FirField -> "field"
                is FirEnumEntry -> "enum entry"
                else -> "unknown"
            }
        )
    }

    internal fun renderPhaseAndAttributes(declaration: FirDeclaration) {
        resolvePhaseRenderer?.render(declaration)
        with(declaration) {
            renderDeclarationAttributes()
        }
    }

    protected open fun FirDeclaration.renderDeclarationAttributes() {
    }
}