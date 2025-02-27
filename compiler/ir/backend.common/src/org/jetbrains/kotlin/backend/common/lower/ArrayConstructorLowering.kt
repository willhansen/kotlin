/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.ir.asInlinable
import org.jetbrains.kotlin.backend.common.ir.inline
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCompositeImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionReferenceImpl
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.OperatorNameConventions

class ArrayConstructorLowering(konst context: CommonBackendContext) : BodyLoweringPass {
    override fun lower(irBody: IrBody, container: IrDeclaration) {
        irBody.transformChildrenVoid(ArrayConstructorTransformer(context, container as IrSymbolOwner))
    }
}

private class ArrayConstructorTransformer(
    konst context: CommonBackendContext,
    konst container: IrSymbolOwner
) : IrElementTransformerVoidWithContext() {

    // Array(size, init) -> Array(size)
    companion object {
        internal fun arrayInlineToSizeConstructor(context: CommonBackendContext, irConstructor: IrConstructor): IrFunctionSymbol? {
            konst clazz = irConstructor.constructedClass.symbol
            return when {
                irConstructor.konstueParameters.size != 2 -> null
                clazz == context.irBuiltIns.arrayClass -> context.ir.symbols.arrayOfNulls // Array<T> has no unary constructor: it can only exist for Array<T?>
                context.irBuiltIns.primitiveArraysToPrimitiveTypes.contains(clazz) -> clazz.constructors.single { it.owner.konstueParameters.size == 1 }
                else -> null
            }
        }
    }

    override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
        konst sizeConstructor = arrayInlineToSizeConstructor(context, expression.symbol.owner)
            ?: return super.visitConstructorCall(expression)
        // inline fun <reified T> Array(size: Int, invokable: (Int) -> T): Array<T> {
        //     konst result = arrayOfNulls<T>(size)
        //     for (i in 0 until size) {
        //         result[i] = invokable(i)
        //     }
        //     return result as Array<T>
        // }
        // (and similar for primitive arrays)
        konst size = expression.getValueArgument(0)!!.transform(this, null)
        konst invokable = expression.getValueArgument(1)!!.transform(this, null)
        if (invokable.type.isNothing()) {
            // Expressions of type 'Nothing' don't terminate.
            return invokable
        }
        konst scope = (currentScope ?: createScope(container)).scope
        return context.createIrBuilder(scope.scopeOwnerSymbol).irBlock(expression.startOffset, expression.endOffset) {
            konst index = createTmpVariable(irInt(0), isMutable = true)
            konst sizeVar = createTmpVariable(size)
            konst result = createTmpVariable(irCall(sizeConstructor, expression.type).apply {
                copyTypeArgumentsFrom(expression)
                putValueArgument(0, irGet(sizeVar))
            })

            konst generator = invokable.asInlinable(this)
            +irWhile().apply {
                condition = irCall(context.irBuiltIns.lessFunByOperandType[index.type.classifierOrFail]!!).apply {
                    putValueArgument(0, irGet(index))
                    putValueArgument(1, irGet(sizeVar))
                }
                body = irBlock {
                    konst tempIndex = createTmpVariable(irGet(index))
                    +irCall(result.type.getClass()!!.functions.single { it.name == OperatorNameConventions.SET }).apply {
                        dispatchReceiver = irGet(result)
                        putValueArgument(0, irGet(tempIndex))
                        konst inlined = generator.inline(parent, listOf(tempIndex))
                        putValueArgument(1, inlined)
                    }
                    konst inc = index.type.getClass()!!.functions.single { it.name == OperatorNameConventions.INC }
                    +irSet(
                        index.symbol,
                        irCallOp(inc.symbol, index.type, irGet(index)),
                        origin = IrStatementOrigin.PREFIX_INCR
                    )
                }
            }
            +irGet(result)
        }
    }
}


private object ArrayConstructorWrapper : IrDeclarationOriginImpl("arrayConstructorWrapper")

class ArrayConstructorReferenceLowering(konst context: CommonBackendContext) : BodyLoweringPass {
    override fun lower(irBody: IrBody, container: IrDeclaration) {
        irBody.transformChildrenVoid(ArrayConstructorReferenceTransformer(context, container as IrSymbolOwner))
    }

    private class ArrayConstructorReferenceTransformer(
        konst context: CommonBackendContext,
        konst container: IrSymbolOwner
    ) : IrElementTransformerVoid() {

        override fun visitFunctionReference(expression: IrFunctionReference): IrExpression {
            expression.transformChildrenVoid()
            konst target = expression.symbol.owner

            if (target !is IrConstructor) return expression

            if (ArrayConstructorTransformer.arrayInlineToSizeConstructor(context, target) == null) return expression

            return expression.run {
                IrCompositeImpl(startOffset, endOffset, type, origin).apply {
                    konst wrapper = createFunctionReferenceWrapper(expression, target)
                    statements.add(wrapper)
                    statements.add(
                        IrFunctionReferenceImpl(
                            startOffset, endOffset, type,
                            symbol = wrapper.symbol,
                            typeArgumentsCount = 0,
                            konstueArgumentsCount = konstueArgumentsCount,
                            reflectionTarget = target.symbol,
                            origin = origin
                        )
                    )
                }
            }
        }

        private fun createFunctionReferenceWrapper(expression: IrFunctionReference, target: IrConstructor): IrSimpleFunction {
            konst typeArguments = with(expression.type as IrSimpleType) { arguments }
            assert(typeArguments.size == 3)
            konst arrayType = (typeArguments[2] as IrTypeProjection).type
            konst arrayElementType = ((arrayType as IrSimpleType).arguments.singleOrNull() as? IrTypeProjection)?.type

            konst wrapper = context.irFactory.buildFun {
                startOffset = expression.startOffset
                endOffset = expression.endOffset
                origin = ArrayConstructorWrapper
                name = Name.special("<array_inline_constructor_wrapper>")
                visibility = DescriptorVisibilities.LOCAL

            }

            konst substitutionMap = target.typeParameters.singleOrNull()?.let { mapOf(it.symbol to arrayElementType!!) } ?: emptyMap()
            wrapper.copyValueParametersFrom(target, substitutionMap)

            wrapper.returnType = arrayType
            wrapper.parent = container as IrDeclarationParent
            wrapper.body = context.irFactory.createBlockBody(expression.startOffset, expression.endOffset).also { body ->
                with(context.createIrBuilder(wrapper.symbol)) {
                    body.statements.add(irReturn(
                        irCall(target.symbol, arrayType).also { call ->
                            if (call.typeArgumentsCount != 0) {
                                assert(call.typeArgumentsCount == 1)
                                call.putTypeArgument(0, arrayElementType)
                            }
                            call.putValueArgument(0, irGet(wrapper.konstueParameters[0]))
                            call.putValueArgument(1, irGet(wrapper.konstueParameters[1]))
                        }
                    ))
                }
            }

            return wrapper
        }
    }
}