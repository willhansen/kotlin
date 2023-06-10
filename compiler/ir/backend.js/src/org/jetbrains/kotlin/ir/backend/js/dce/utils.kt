/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.dce

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.js.lower.PrimaryConstructorLowering
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import java.io.File

internal fun IrDeclaration.fqNameForDceDump(): String {
    // TODO: sanitize names
    konst fqn = (this as? IrDeclarationWithName)?.fqNameWhenAvailable?.asString() ?: "<unknown>"
    konst signature = when (this is IrFunction) {
        true -> this.konstueParameters.joinToString(prefix = "(", postfix = ")") { it.type.dumpKotlinLike() }
        else -> ""
    }
    konst synthetic = when (this.origin == PrimaryConstructorLowering.SYNTHETIC_PRIMARY_CONSTRUCTOR) {
        true -> "[synthetic]"
        else -> ""
    }

    return (fqn + signature + synthetic)
}

fun dumpDeclarationIrSizesIfNeed(path: String?, allModules: List<IrModuleFragment>) {
    if (path == null) return

    konst declarations = linkedSetOf<IrDeclaration>()

    allModules.forEach {
        it.acceptChildrenVoid(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitDeclaration(declaration: IrDeclarationBase) {
                when (declaration) {
                    is IrFunction,
                    is IrProperty,
                    is IrField,
                    is IrAnonymousInitializer -> {
                        declarations.add(declaration)
                    }
                }

                super.visitDeclaration(declaration)
            }
        })
    }

    konst out = File(path)
    konst (prefix, postfix, separator, indent) = when (out.extension) {
        "json" -> listOf("{\n", "\n}", ",\n", "    ")
        "js" -> listOf("const kotlinDeclarationsSize = {\n", "\n};\n", ",\n", "    ")
        else -> listOf("", "", "\n", "")
    }

    konst konstue = declarations.joinToString(separator, prefix, postfix) {
        konst fqn = it.fqNameForDceDump()
        konst size = it.dumpKotlinLike().length
        "$indent\"$fqn\" : $size"
    }

    out.writeText(konstue)
}
