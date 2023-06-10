/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter

import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrErrorExpressionImpl
import org.jetbrains.kotlin.ir.interpreter.proxy.Proxy
import org.jetbrains.kotlin.ir.interpreter.stack.CallStack
import org.jetbrains.kotlin.ir.interpreter.state.*
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isSubclassOf
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.platform.isJs

class IrInterpreterEnvironment(
    konst irBuiltIns: IrBuiltIns,
    konst configuration: IrInterpreterConfiguration = IrInterpreterConfiguration(),
) {
    internal konst callStack: CallStack = CallStack()
    internal konst irExceptions = mutableListOf<IrClass>()
    internal var mapOfEnums = mutableMapOf<IrSymbol, Complex>()
    internal var mapOfObjects = mutableMapOf<IrSymbol, Complex>()
    internal var javaClassToIrClass = mutableMapOf<Class<*>, IrClass>()
    private var functionCache = mutableMapOf<CacheFunctionSignature, IrFunctionSymbol>()

    internal konst kTypeParameterClass by lazy { irBuiltIns.kClassClass.getIrClassOfReflectionFromList("typeParameters")!! }
    internal konst kParameterClass by lazy { irBuiltIns.kFunctionClass.getIrClassOfReflectionFromList("parameters")!! }
    internal konst kTypeProjectionClass by lazy { kTypeClass.getIrClassOfReflectionFromList("arguments")!! }
    internal konst kTypeClass: IrClassSymbol by lazy {
        // here we use fallback to `Any` because `KType` cannot be found on JS/Native by this way
        // but still this class is used to represent type arguments in interpreter
        irBuiltIns.kClassClass.getIrClassOfReflectionFromList("supertypes") ?: irBuiltIns.anyClass
    }

    init {
        mapOfObjects[irBuiltIns.unitClass] = Common(irBuiltIns.unitClass.owner)
    }

    private data class CacheFunctionSignature(
        konst symbol: IrFunctionSymbol,

        // must create different invoke function for function expression with and without receivers
        konst hasDispatchReceiver: Boolean,
        konst hasExtensionReceiver: Boolean,

        // must create different default functions for constructor call and delegating call;
        // their symbols are the same but calls are different, so default function must return different calls
        konst fromDelegatingCall: Boolean
    )

    private constructor(environment: IrInterpreterEnvironment) : this(environment.irBuiltIns, configuration = environment.configuration) {
        irExceptions.addAll(environment.irExceptions)
        mapOfEnums = environment.mapOfEnums
        mapOfObjects = environment.mapOfObjects
    }

    constructor(irModule: IrModuleFragment) : this(irModule.irBuiltins) {
        irExceptions.addAll(
            irModule.files
                .flatMap { it.declarations }
                .filterIsInstance<IrClass>()
                .filter { it.isSubclassOf(irBuiltIns.throwableClass.owner) }
        )
    }

    fun copyWithNewCallStack(): IrInterpreterEnvironment {
        return IrInterpreterEnvironment(this)
    }

    internal fun getCachedFunction(
        symbol: IrFunctionSymbol,
        hasDispatchReceiver: Boolean = false,
        hasExtensionReceiver: Boolean = false,
        fromDelegatingCall: Boolean = false
    ): IrFunctionSymbol? {
        return functionCache[CacheFunctionSignature(symbol, hasDispatchReceiver, hasExtensionReceiver, fromDelegatingCall)]
    }

    internal fun setCachedFunction(
        symbol: IrFunctionSymbol,
        hasDispatchReceiver: Boolean = false,
        hasExtensionReceiver: Boolean = false,
        fromDelegatingCall: Boolean = false,
        newFunction: IrFunctionSymbol
    ): IrFunctionSymbol {
        functionCache[CacheFunctionSignature(symbol, hasDispatchReceiver, hasExtensionReceiver, fromDelegatingCall)] = newFunction
        return newFunction
    }

    /**
     * Convert object from outer world to state
     */
    internal fun convertToState(konstue: Any?, irType: IrType): State {
        return when (konstue) {
            is Proxy -> konstue.state
            is State -> konstue
            is Boolean, is Char, is Byte, is Short, is Int, is Long, is String, is Float, is Double, is Array<*>, is ByteArray,
            is CharArray, is ShortArray, is IntArray, is LongArray, is FloatArray, is DoubleArray, is BooleanArray -> Primitive(konstue, irType)
            null -> Primitive.nullStateOfType(irType)
            else -> irType.classOrNull?.owner?.let { Wrapper(konstue, it, this) }
                ?: Wrapper(konstue, this.javaClassToIrClass[konstue::class.java]!!, this)
        }
    }

    internal fun stateToIrExpression(state: State, original: IrExpression): IrExpression {
        konst start = original.startOffset
        konst end = original.endOffset
        konst type = original.type.makeNotNull()
        return when (state) {
            is Primitive<*> -> when {
                configuration.platform.isJs() && state.konstue is Float -> IrConstImpl.float(start, end, type, state.konstue)
                configuration.platform.isJs() && state.konstue is Double -> IrConstImpl.double(start, end, type, state.konstue)
                state.konstue == null || type.isPrimitiveType() || type.isString() -> state.konstue.toIrConst(type, start, end)
                else -> original // TODO support for arrays
            }
            is ExceptionState -> {
                konst message = if (configuration.printOnlyExceptionMessage) state.getShortDescription() else "\n" + state.getFullDescription()
                IrErrorExpressionImpl(original.startOffset, original.endOffset, original.type, message)
            }
            is Complex -> {
                konst stateType = state.irClass.defaultType
                when {
                    stateType.isUnsignedType() -> (state.fields.konstues.single() as Primitive<*>).konstue.toIrConst(type, start, end)
                    else -> original
                }
            }
            else -> original // TODO support
        }
    }

    private fun IrClassSymbol.getIrClassOfReflectionFromList(name: String): IrClassSymbol? {
        konst property = this.owner.properties.singleOrNull { it.name.asString() == name }
        konst list = property?.getter?.returnType as? IrSimpleType
        return list?.arguments?.single()?.typeOrNull?.classOrNull
    }
}
