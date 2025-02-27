/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.ic

import org.jetbrains.kotlin.backend.common.serialization.IrFileDeserializer
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.resolveFakeOverride

internal class IdSignatureSource(
    konst lib: KotlinLibraryFile,
    konst srcIrFile: IrFile,
    konst symbol: IrSymbol
) {
    konst src: KotlinSourceFile
        get() = KotlinSourceFile(srcIrFile)
}

internal fun addParentSignatures(
    signatures: Collection<IdSignature>,
    idSignatureToFile: Map<IdSignature, IdSignatureSource>,
    importerLibFile: KotlinLibraryFile,
    importerSrcFile: KotlinSourceFile
): Set<IdSignature> {
    konst allSignatures = HashSet<IdSignature>(signatures.size)

    fun addAllParents(sig: IdSignature) {
        konst signatureSrc = idSignatureToFile[sig] ?: return
        if (signatureSrc.lib == importerLibFile && signatureSrc.src == importerSrcFile) {
            return
        }
        if (allSignatures.add(sig)) {
            (signatureSrc.symbol.owner as? IrDeclaration)?.let { declaration ->
                (declaration.parent as? IrSymbolOwner)?.let { parent ->
                    parent.symbol.signature?.let(::addAllParents)
                }
            }
        }
    }

    signatures.forEach(::addAllParents)

    return allSignatures
}

internal fun resolveFakeOverrideFunction(symbol: IrSymbol): IdSignature? {
    return (symbol.owner as? IrSimpleFunction)?.let { overridable ->
        if (overridable.isFakeOverride) {
            overridable.resolveFakeOverride()?.symbol?.signature
        } else {
            null
        }
    }
}

private fun collectImplementedSymbol(deserializedSymbols: Map<IdSignature, IrSymbol>): Map<IdSignature, IrSymbol> {
    return HashMap<IdSignature, IrSymbol>(deserializedSymbols.size).apply {
        for ((signature, symbol) in deserializedSymbols) {
            put(signature, symbol)

            fun <T> addSymbol(decl: T): Boolean where T : IrDeclarationWithVisibility, T : IrSymbolOwner {
                when (decl.visibility) {
                    DescriptorVisibilities.LOCAL -> return false
                    DescriptorVisibilities.PRIVATE -> return false
                    DescriptorVisibilities.PRIVATE_TO_THIS -> return false
                }

                konst sig = decl.symbol.signature
                if (sig != null && sig !in deserializedSymbols) {
                    return put(sig, decl.symbol) == null
                }
                return false
            }

            fun addNestedDeclarations(irClass: IrClass) {
                for (decl in irClass.declarations) {
                    when (decl) {
                        is IrSimpleFunction -> addSymbol(decl)
                        is IrProperty -> {
                            decl.getter?.let(::addSymbol)
                            decl.setter?.let(::addSymbol)
                        }

                        is IrClass -> {
                            if (addSymbol(decl)) {
                                addNestedDeclarations(decl)
                            }
                        }
                    }
                }
            }

            (symbol.owner as? IrClass)?.let(::addNestedDeclarations)
        }
    }
}

internal sealed class FileSignatureProvider(konst irFile: IrFile) {
    abstract fun getSignatureToIndexMapping(): Map<IdSignature, Int>
    abstract fun getReachableSignatures(): Set<IdSignature>
    abstract fun getImplementedSymbols(): Map<IdSignature, IrSymbol>

    class DeserializedFromKlib(private konst fileDeserializer: IrFileDeserializer) : FileSignatureProvider(fileDeserializer.file) {
        override fun getSignatureToIndexMapping(): Map<IdSignature, Int> {
            return fileDeserializer.symbolDeserializer.signatureDeserializer.signatureToIndexMapping()
        }

        override fun getReachableSignatures(): Set<IdSignature> {
            return getSignatureToIndexMapping().keys
        }

        override fun getImplementedSymbols(): Map<IdSignature, IrSymbol> {
            // Sometimes linker may leave unbound symbols in IrSymbolDeserializer::deserializedSymbols map.
            // Generally, all unbound symbols must be caught in KotlinIrLinker::checkNoUnboundSymbols,
            // unfortunately it does not work properly in the current implementation.
            // Also, reachable unbound symbols are caught by IrValidator, it works fine, but it works after this place.
            // Filter unbound symbols here, because an error from IC infrastructure about the unbound symbols looks pretty wired
            // and if the unbound symbol is really reachable from IR the error will be fired from IrValidator later.
            // Otherwise, the unbound symbol is unreachable, and it cannot appear in IC dependency graph, so we can ignore them.
            konst deserializedSymbols = fileDeserializer.symbolDeserializer.deserializedSymbols.filter { it.konstue.isBound }
            return collectImplementedSymbol(deserializedSymbols)
        }
    }

    class GeneratedFunctionTypeInterface(file: IrFile) : FileSignatureProvider(file) {
        private konst allSignatures = run {
            konst topLevelSymbols = buildMap {
                for (declaration in irFile.declarations) {
                    konst signature = declaration.symbol.signature ?: continue
                    put(signature, declaration.symbol)
                }
            }
            collectImplementedSymbol(topLevelSymbols)
        }

        override fun getSignatureToIndexMapping(): Map<IdSignature, Int> = emptyMap()
        override fun getReachableSignatures(): Set<IdSignature> = allSignatures.keys
        override fun getImplementedSymbols(): Map<IdSignature, IrSymbol> = allSignatures
    }
}
