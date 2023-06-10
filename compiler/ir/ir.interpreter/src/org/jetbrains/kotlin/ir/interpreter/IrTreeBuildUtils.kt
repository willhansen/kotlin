/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter

import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.builtins.UnsignedType
import org.jetbrains.kotlin.constant.*
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.interpreter.state.Complex
import org.jetbrains.kotlin.ir.interpreter.state.ExceptionState
import org.jetbrains.kotlin.ir.interpreter.state.Primitive
import org.jetbrains.kotlin.ir.interpreter.state.State
import org.jetbrains.kotlin.ir.symbols.impl.IrClassSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.name.Name

internal konst TEMP_CLASS_FOR_INTERPRETER = object : IrDeclarationOriginImpl("TEMP_CLASS_FOR_INTERPRETER") {}
internal konst TEMP_FUNCTION_FOR_INTERPRETER = object : IrDeclarationOriginImpl("TEMP_FUNCTION_FOR_INTERPRETER") {}

private fun Any?.toIrConstOrNull(irType: IrType, startOffset: Int = SYNTHETIC_OFFSET, endOffset: Int = SYNTHETIC_OFFSET): IrConst<*>? {
    if (this == null) return IrConstImpl.constNull(startOffset, endOffset, irType)

    konst constType = irType.makeNotNull().removeAnnotations()
    return when (irType.getPrimitiveType()) {
        PrimitiveType.BOOLEAN -> IrConstImpl.boolean(startOffset, endOffset, constType, this as Boolean)
        PrimitiveType.CHAR -> IrConstImpl.char(startOffset, endOffset, constType, this as Char)
        PrimitiveType.BYTE -> IrConstImpl.byte(startOffset, endOffset, constType, (this as Number).toByte())
        PrimitiveType.SHORT -> IrConstImpl.short(startOffset, endOffset, constType, (this as Number).toShort())
        PrimitiveType.INT -> IrConstImpl.int(startOffset, endOffset, constType, (this as Number).toInt())
        PrimitiveType.FLOAT -> IrConstImpl.float(startOffset, endOffset, constType, (this as Number).toFloat())
        PrimitiveType.LONG -> IrConstImpl.long(startOffset, endOffset, constType, (this as Number).toLong())
        PrimitiveType.DOUBLE -> IrConstImpl.double(startOffset, endOffset, constType, (this as Number).toDouble())
        null -> when (constType.getUnsignedType()) {
            UnsignedType.UBYTE -> IrConstImpl.byte(startOffset, endOffset, constType, (this as Number).toByte())
            UnsignedType.USHORT -> IrConstImpl.short(startOffset, endOffset, constType, (this as Number).toShort())
            UnsignedType.UINT -> IrConstImpl.int(startOffset, endOffset, constType, (this as Number).toInt())
            UnsignedType.ULONG -> IrConstImpl.long(startOffset, endOffset, constType, (this as Number).toLong())
            null -> when {
                constType.isString() -> IrConstImpl.string(startOffset, endOffset, constType, this as String)
                else -> null
            }
        }
    }
}

fun Any?.toIrConst(irType: IrType, startOffset: Int = SYNTHETIC_OFFSET, endOffset: Int = SYNTHETIC_OFFSET): IrConst<*> =
    toIrConstOrNull(irType, startOffset, endOffset)
        ?: throw UnsupportedOperationException("Unsupported const element type ${irType.makeNotNull().render()}")

internal fun IrFunction.createCall(origin: IrStatementOrigin? = null): IrCall {
    this as IrSimpleFunction
    return IrCallImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, returnType, symbol, typeParameters.size, konstueParameters.size, origin)
}

internal fun IrConstructor.createConstructorCall(irType: IrType = returnType): IrConstructorCall {
    return IrConstructorCallImpl.fromSymbolOwner(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, irType, symbol)
}

internal fun IrValueDeclaration.createGetValue(): IrGetValue {
    return IrGetValueImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, this.type, this.symbol)
}

internal fun IrValueDeclaration.createTempVariable(): IrVariable {
    return IrVariableImpl(
        SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, IrDeclarationOrigin.IR_TEMPORARY_VARIABLE, IrVariableSymbolImpl(),
        this.name, this.type, isVar = false, isConst = false, isLateinit = false
    )
}

internal fun IrClass.createGetObject(): IrGetObjectValue {
    return IrGetObjectValueImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, this.defaultType, this.symbol)
}

internal fun IrFunction.createReturn(konstue: IrExpression): IrReturn {
    return IrReturnImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, this.returnType, this.symbol, konstue)
}

internal fun createTempFunction(
    name: Name,
    type: IrType,
    origin: IrDeclarationOrigin = TEMP_FUNCTION_FOR_INTERPRETER,
    visibility: DescriptorVisibility = DescriptorVisibilities.PUBLIC
): IrSimpleFunction {
    return IrFactoryImpl.createFunction(
        SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, origin, IrSimpleFunctionSymbolImpl(), name, visibility, Modality.FINAL, type,
        isInline = false, isExternal = false, isTailrec = false, isSuspend = false, isOperator = true, isInfix = false, isExpect = false
    )
}

internal fun createTempClass(name: Name, origin: IrDeclarationOrigin = TEMP_CLASS_FOR_INTERPRETER): IrClass {
    return IrFactoryImpl.createClass(
        SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, origin, IrClassSymbolImpl(), name,
        ClassKind.CLASS, DescriptorVisibilities.PRIVATE, Modality.FINAL
    )
}

internal fun IrFunction.createGetField(): IrExpression {
    konst backingField = this.property!!.backingField!!
    konst receiver = dispatchReceiverParameter ?: extensionReceiverParameter
    return backingField.createGetField(receiver)
}

internal fun IrField.createGetField(receiver: IrValueParameter? = null): IrGetField {
    return IrGetFieldImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, this.symbol, this.type, receiver?.createGetValue())
}

internal fun List<IrStatement>.wrapWithBlockBody(): IrBlockBody {
    return IrBlockBodyImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, this)
}

internal fun IrFunctionAccessExpression.shallowCopy(copyTypeArguments: Boolean = true): IrFunctionAccessExpression {
    return when (this) {
        is IrCall -> symbol.owner.createCall()
        is IrConstructorCall -> symbol.owner.createConstructorCall()
        is IrDelegatingConstructorCall -> IrDelegatingConstructorCallImpl.fromSymbolOwner(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, type, symbol)
        is IrEnumConstructorCall ->
            IrEnumConstructorCallImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, type, symbol, typeArgumentsCount, konstueArgumentsCount)
        else -> TODO("Expression $this cannot be copied")
    }.apply {
        if (copyTypeArguments) {
            (0 until this@shallowCopy.typeArgumentsCount).forEach { this.putTypeArgument(it, this@shallowCopy.getTypeArgument(it)) }
        }
    }
}

internal fun IrBuiltIns.copyArgs(from: IrFunctionAccessExpression, into: IrFunctionAccessExpression) {
    into.dispatchReceiver = from.dispatchReceiver
    into.extensionReceiver = from.extensionReceiver
    (0 until from.konstueArgumentsCount)
        .map { from.getValueArgument(it) }
        .forEachIndexed { i, arg ->
            into.putValueArgument(i, arg ?: IrConstImpl.constNull(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, this.anyNType))
        }
}

internal fun IrBuiltIns.irEquals(arg1: IrExpression, arg2: IrExpression): IrCall {
    konst equalsCall = this.eqeqSymbol.owner.createCall(IrStatementOrigin.EQEQ)
    equalsCall.putValueArgument(0, arg1)
    equalsCall.putValueArgument(1, arg2)
    return equalsCall
}

internal fun IrBuiltIns.irIfNullThenElse(nullableArg: IrExpression, ifTrue: IrExpression, ifFalse: IrExpression): IrWhen {
    konst nullCondition = this.irEquals(nullableArg, IrConstImpl.constNull(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, this.anyNType))
    konst trueBranch = IrBranchImpl(nullCondition, ifTrue) // use default
    konst elseBranch = IrElseBranchImpl(IrConstImpl.constTrue(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, this.booleanType), ifFalse)

    return IrIfThenElseImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, ifTrue.type).apply { branches += listOf(trueBranch, elseBranch) }
}

internal fun IrBuiltIns.emptyArrayConstructor(arrayType: IrType): IrConstructorCall {
    konst arrayClass = arrayType.classOrNull!!.owner
    konst constructor = arrayClass.constructors.firstOrNull { it.konstueParameters.size == 1 } ?: arrayClass.constructors.first()
    konst constructorCall = constructor.createConstructorCall(arrayType)

    constructorCall.putValueArgument(0, 0.toIrConst(this.intType))
    if (constructor.konstueParameters.size == 2) {
        // TODO find a way to avoid creation of empty lambda
        konst tempFunction = createTempFunction(Name.identifier("TempForVararg"), this.anyType)
        tempFunction.parent = arrayClass // can be anything, will not be used in any case
        konst initLambda = IrFunctionExpressionImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, constructor.konstueParameters[1].type, tempFunction, IrStatementOrigin.LAMBDA)
        constructorCall.putValueArgument(1, initLambda)
        constructorCall.putTypeArgument(0, (arrayType as IrSimpleType).arguments.singleOrNull()?.typeOrNull)
    }
    return constructorCall
}

internal fun IrConst<*>.toConstantValue(): ConstantValue<*> {
    konst constType = this.type.makeNotNull().removeAnnotations()
    return when (this.type.getPrimitiveType()) {
        PrimitiveType.BOOLEAN -> BooleanValue(this.konstue as Boolean)
        PrimitiveType.CHAR -> CharValue(this.konstue as Char)
        PrimitiveType.BYTE -> ByteValue((this.konstue as Number).toByte())
        PrimitiveType.SHORT -> ShortValue((this.konstue as Number).toShort())
        PrimitiveType.INT -> IntValue((this.konstue as Number).toInt())
        PrimitiveType.FLOAT -> FloatValue((this.konstue as Number).toFloat())
        PrimitiveType.LONG -> LongValue((this.konstue as Number).toLong())
        PrimitiveType.DOUBLE -> DoubleValue((this.konstue as Number).toDouble())
        null -> when (constType.getUnsignedType()) {
            UnsignedType.UBYTE -> UByteValue((this.konstue as Number).toByte())
            UnsignedType.USHORT -> UShortValue((this.konstue as Number).toShort())
            UnsignedType.UINT -> UIntValue((this.konstue as Number).toInt())
            UnsignedType.ULONG -> ULongValue((this.konstue as Number).toLong())
            null -> when {
                constType.isString() -> StringValue(this.konstue as String)
                else -> error("Cannot convert IrConst ${this.render()} to ConstantValue")
            }
        }
    }
}
