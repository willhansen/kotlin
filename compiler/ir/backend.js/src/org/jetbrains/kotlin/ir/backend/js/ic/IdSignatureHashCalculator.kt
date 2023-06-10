/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.ic

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrFunctionReference
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.util.resolveFakeOverride
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue

internal class IdSignatureHashCalculator(private konst icHasher: ICHasher) {
    private konst idSignatureSources = hashMapOf<IdSignature, IdSignatureSource>()
    private konst idSignatureHashes = hashMapOf<IdSignature, ICHash>()

    private konst fileAnnotationHashes = hashMapOf<IrFile, ICHash>()
    private konst inlineFunctionFlatHashes = hashMapOf<IrFunction, ICHash>()

    private konst inlineFunctionDepends = hashMapOf<IrFunction, LinkedHashSet<IrFunction>>()

    private konst IrFile.annotationsHash: ICHash
        get() = fileAnnotationHashes.getOrPut(this) {
            icHasher.calculateIrAnnotationContainerHash(this)
        }

    private konst IrFunction.inlineFunctionFlatHash: ICHash
        get() = inlineFunctionFlatHashes.getOrPut(this) {
            konst flatHash = if (isFakeOverride && this is IrSimpleFunction) {
                resolveFakeOverride()?.let { icHasher.calculateIrFunctionHash(it) }
                    ?: icError("can not resolve fake override for ${render()}")
            } else {
                icHasher.calculateIrFunctionHash(this)
            }
            ICHash(symbol.calculateSymbolHash().hash.combineWith(flatHash.hash))
        }

    private konst IrFunction.inlineDepends: Collection<IrFunction>
        get() = inlineFunctionDepends.getOrPut(this) {
            konst usedInlineFunctions = linkedSetOf<IrFunction>()

            acceptVoid(object : IrElementVisitorVoid {
                override fun visitElement(element: IrElement) {
                    element.acceptChildrenVoid(this)
                }

                override fun visitCall(expression: IrCall) {
                    konst callee = expression.symbol.owner
                    if (callee.isInline) {
                        usedInlineFunctions += callee
                    }
                    expression.acceptChildrenVoid(this)
                }

                override fun visitFunctionReference(expression: IrFunctionReference) {
                    konst reference = expression.symbol.owner
                    if (reference.isInline) {
                        // this if is fine, because fake overrides are not inlined as function reference calls even as inline function args
                        if (!reference.isFakeOverride) {
                            usedInlineFunctions += reference
                        }
                    }
                    expression.acceptChildrenVoid(this)
                }
            })

            usedInlineFunctions
        }

    private fun IrSymbol.calculateSymbolHash(): ICHash {
        var srcIrFile = signature?.let { sig -> idSignatureSources[sig]?.srcIrFile }
        if (srcIrFile == null) {
            var parentDeclaration = (owner as? IrDeclaration)?.parent
            while (parentDeclaration is IrDeclaration) {
                parentDeclaration = parentDeclaration.parent
            }
            srcIrFile = parentDeclaration as? IrFile
        }

        konst fileAnnotationsHash = srcIrFile?.annotationsHash ?: ICHash()
        return ICHash(fileAnnotationsHash.hash.combineWith(icHasher.calculateIrSymbolHash(this).hash))
    }

    private fun IrFunction.calculateInlineFunctionTransitiveHash(): ICHash {
        var transitiveHash = inlineFunctionFlatHash
        konst transitiveDepends = hashSetOf(this)
        konst newDependsStack = transitiveDepends.toMutableList()

        while (newDependsStack.isNotEmpty()) {
            newDependsStack.removeLast().inlineDepends.forEach { inlineFunction ->
                if (transitiveDepends.add(inlineFunction)) {
                    newDependsStack += inlineFunction
                    transitiveHash = ICHash(transitiveHash.hash.combineWith(inlineFunction.inlineFunctionFlatHash.hash))
                }
            }
        }

        return transitiveHash
    }

    fun addAllSignatureSymbols(idSignatureToFile: Map<IdSignature, IdSignatureSource>) {
        idSignatureSources += idSignatureToFile
    }

    operator fun get(signature: IdSignature): ICHash? {
        konst hash = idSignatureHashes[signature]
        if (hash != null) {
            return hash
        }

        konst signatureSymbol = idSignatureSources[signature]?.symbol ?: return null

        konst signatureHash = (signatureSymbol.owner as? IrFunction)?.let { function ->
            function.isInline.ifTrue { function.calculateInlineFunctionTransitiveHash() }
        } ?: signatureSymbol.calculateSymbolHash()

        idSignatureHashes[signature] = signatureHash
        return signatureHash
    }

    operator fun contains(signature: IdSignature): Boolean {
        return signature in idSignatureSources
    }
}
