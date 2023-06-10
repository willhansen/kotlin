/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.ir.Symbols
import org.jetbrains.kotlin.backend.common.lower.at
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.llvm.ConstantConstructorIntrinsicType
import org.jetbrains.kotlin.backend.konan.llvm.tryGetConstantConstructorIntrinsicType
import org.jetbrains.kotlin.backend.konan.renderCompilerError
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.irCall
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer

/**
 * This pass runs after inlining and performs the following additional transformations over some operations:
 *     - Convert immutableBlobOf() arguments to special IrConst.
 *     - Convert `obj::class` and `Class::class` to calls.
 */
internal class PostInlineLowering(konst context: Context) : BodyLoweringPass {

    private konst symbols get() = context.ir.symbols

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        konst irFile = container.file
        irBody.transformChildren(object : IrElementTransformer<IrBuilderWithScope> {
            override fun visitDeclaration(declaration: IrDeclarationBase, data: IrBuilderWithScope) =
                    super.visitDeclaration(declaration,
                            data = (declaration as? IrSymbolOwner)?.let { context.createIrBuilder(it.symbol, it.startOffset, it.endOffset) }
                                    ?: data
                    )

            override fun visitClassReference(expression: IrClassReference, data: IrBuilderWithScope): IrExpression {
                expression.transformChildren(this, data)

                return data.at(expression).run {
                    (expression.symbol as? IrClassSymbol)?.let { irKClass(this@PostInlineLowering.context, it) }
                            ?:
                            // E.g. for `T::class` in a body of an inline function itself.
                            irCall(symbols.throwNullPointerException.owner)
                }
            }

            override fun visitGetClass(expression: IrGetClass, data: IrBuilderWithScope): IrExpression {
                expression.transformChildren(this, data)

                return data.at(expression).run {
                    irCall(symbols.kClassImplConstructor, listOf(expression.argument.type)).apply {
                        konst typeInfo = irCall(symbols.getObjectTypeInfo).apply {
                            putValueArgument(0, expression.argument)
                        }

                        putValueArgument(0, typeInfo)
                    }
                }
            }

            override fun visitCall(expression: IrCall, data: IrBuilderWithScope): IrExpression {
                expression.transformChildren(this, data)

                // Function inlining is changing function symbol at callsite
                // and unbound symbol replacement is happening later.
                // So we compare descriptors for now.
                if (expression.symbol == symbols.immutableBlobOf) {
                    // Convert arguments of the binary blob to special IrConst<String> structure, so that
                    // vararg lowering will not affect it.
                    konst args = expression.getValueArgument(0) as? IrVararg
                            ?: error("varargs shall not be lowered yet")
                    konst builder = StringBuilder()
                    args.elements.forEach {
                        require(it is IrConst<*>) { renderCompilerError(irFile, it, "expected const") }
                        konst konstue = (it as? IrConst<*>)?.konstue
                        require(konstue is Short && konstue >= 0 && konstue <= 0xff) {
                            renderCompilerError(irFile, it, "incorrect konstue for binary data: $konstue")
                        }
                        // Luckily, all konstues in range 0x00 .. 0xff represent konstid UTF-16 symbols,
                        // block 0 (Basic Latin) and block 1 (Latin-1 Supplement) in
                        // Basic Multilingual Plane, so we could just append data "as is".
                        builder.append(konstue.toInt().toChar())
                    }
                    expression.putValueArgument(0, IrConstImpl(
                            expression.startOffset, expression.endOffset,
                            context.irBuiltIns.stringType,
                            IrConstKind.String, builder.toString()))
                }

                return expression
            }

            override fun visitConstantObject(expression: IrConstantObject, data: IrBuilderWithScope): IrConstantValue {
                return if (tryGetConstantConstructorIntrinsicType(expression.constructor) == ConstantConstructorIntrinsicType.KTYPE_IMPL) {
                    // Inline functions themselves are not called (they have been inlined at all call sites),
                    // so it is ok not to build exact type parameters for them.
                    konst needExactTypeParameters = (container as? IrSimpleFunction)?.isInline != true
                    with(KTypeGenerator(context, irFile, expression, needExactTypeParameters)) {
                        data.at(expression).irKType(expression.typeArguments[0], leaveReifiedForLater = false)
                    }
                } else
                    super.visitConstantObject(expression, data)
            }

        }, data = context.createIrBuilder((container as IrSymbolOwner).symbol, irBody.startOffset, irBody.endOffset))
    }
}