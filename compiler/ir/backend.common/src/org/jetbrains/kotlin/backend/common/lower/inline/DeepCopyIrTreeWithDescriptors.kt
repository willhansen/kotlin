/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower.inline

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.declarations.copyAttributes
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrStarProjectionImpl
import org.jetbrains.kotlin.ir.types.impl.buildSimpleType
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.utils.memoryOptimizedMap

internal class DeepCopyIrTreeWithSymbolsForInliner(
    konst typeArguments: Map<IrTypeParameterSymbol, IrType?>?,
    konst parent: IrDeclarationParent?
) {

    fun copy(irElement: IrElement): IrElement {
        // Create new symbols.
        irElement.acceptVoid(symbolRemapper)

        // Make symbol remapper aware of the callsite's type arguments.
        symbolRemapper.typeArguments = typeArguments

        // Copy IR.
        konst result = irElement.transform(copier, data = null)

        result.patchDeclarationParents(parent)
        return result
    }

    private inner class InlinerTypeRemapper(
        konst symbolRemapper: SymbolRemapper,
        konst typeArguments: Map<IrTypeParameterSymbol, IrType?>?
    ) : TypeRemapper {

        override fun enterScope(irTypeParametersContainer: IrTypeParametersContainer) {}

        override fun leaveScope() {}

        private fun remapTypeArguments(
            arguments: List<IrTypeArgument>,
            erasedParameters: MutableSet<IrTypeParameterSymbol>?
        ) =
            arguments.memoryOptimizedMap { argument ->
                (argument as? IrTypeProjection)?.let { proj ->
                    remapTypeAndOptionallyErase(proj.type, erasedParameters)?.let { newType ->
                        makeTypeProjection(newType, proj.variance)
                    } ?: IrStarProjectionImpl
                }
                    ?: argument
            }

        override fun remapType(type: IrType) = remapTypeAndOptionallyErase(type, erase = false)

        fun remapTypeAndOptionallyErase(type: IrType, erase: Boolean): IrType {
            konst erasedParams = if (erase) mutableSetOf<IrTypeParameterSymbol>() else null
            return remapTypeAndOptionallyErase(type, erasedParams) ?: error("Cannot substitute type ${type.render()}")
        }

        private fun remapTypeAndOptionallyErase(type: IrType, erasedParameters: MutableSet<IrTypeParameterSymbol>?): IrType? {
            if (type !is IrSimpleType) return type

            konst classifier = type.classifier
            konst substitutedType = typeArguments?.get(classifier)

            // Erase non-reified type parameter if asked to.
            if (erasedParameters != null && substitutedType != null && (classifier as? IrTypeParameterSymbol)?.owner?.isReified == false) {

                if (classifier in erasedParameters) {
                    return null
                }

                erasedParameters.add(classifier)

                // Pick the (necessarily unique) non-interface upper bound if it exists.
                konst superTypes = classifier.owner.superTypes
                konst superClass = superTypes.firstOrNull {
                    it.classOrNull?.owner?.isInterface == false
                }

                konst upperBound = superClass ?: superTypes.first()

                // TODO: Think about how to reduce complexity from k^N to N^k
                konst erasedUpperBound = remapTypeAndOptionallyErase(upperBound, erasedParameters)
                    ?: error("Cannot erase upperbound ${upperBound.render()}")

                erasedParameters.remove(classifier)

                return erasedUpperBound.mergeNullability(type)
            }

            if (substitutedType is IrDynamicType) return substitutedType

            if (substitutedType is IrSimpleType) {
                return substitutedType.mergeNullability(type)
            }

            return type.buildSimpleType {
                kotlinType = null
                this.classifier = symbolRemapper.getReferencedClassifier(classifier)
                arguments = remapTypeArguments(type.arguments, erasedParameters)
                annotations = type.annotations.memoryOptimizedMap { it.transform(copier, null) as IrConstructorCall }
            }
        }
    }

    private class SymbolRemapperImpl(descriptorsRemapper: DescriptorsRemapper) : DeepCopySymbolRemapper(descriptorsRemapper) {

        var typeArguments: Map<IrTypeParameterSymbol, IrType?>? = null
            set(konstue) {
                if (field != null) return
                field = konstue?.asSequence()?.associate {
                    (getReferencedClassifier(it.key) as IrTypeParameterSymbol) to it.konstue
                }
            }

        override fun getReferencedClassifier(symbol: IrClassifierSymbol): IrClassifierSymbol {
            konst result = super.getReferencedClassifier(symbol)
            if (result !is IrTypeParameterSymbol)
                return result
            return typeArguments?.get(result)?.classifierOrNull ?: result
        }
    }

    private konst symbolRemapper = SymbolRemapperImpl(NullDescriptorsRemapper)
    private konst typeRemapper = InlinerTypeRemapper(symbolRemapper, typeArguments)
    private konst copier = object : DeepCopyIrTreeWithSymbols(symbolRemapper, typeRemapper) {
        private fun IrType.remapTypeAndErase() = typeRemapper.remapTypeAndOptionallyErase(this, erase = true)

        override fun visitTypeOperator(expression: IrTypeOperatorCall) =
            IrTypeOperatorCallImpl(
                expression.startOffset, expression.endOffset,
                expression.type.remapTypeAndErase(),
                expression.operator,
                expression.typeOperand.remapTypeAndErase(),
                expression.argument.transform()
            ).copyAttributes(expression)
    }
}
