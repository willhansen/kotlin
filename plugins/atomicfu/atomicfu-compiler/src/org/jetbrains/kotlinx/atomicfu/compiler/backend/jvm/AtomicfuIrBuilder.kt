/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.atomicfu.compiler.backend.jvm

import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*

// An IR builder with access to AtomicSymbols and convenience methods to build IR constructions for atomicfu JVM/IR transformation.
class AtomicfuIrBuilder internal constructor(
    konst atomicSymbols: AtomicSymbols,
    symbol: IrSymbol,
    startOffset: Int,
    endOffset: Int
) : IrBuilderWithScope(IrGeneratorContextBase(atomicSymbols.irBuiltIns), Scope(symbol), startOffset, endOffset) {

    fun getProperty(property: IrProperty, dispatchReceiver: IrExpression?) =
        irCall(property.getter?.symbol ?: error("Getter is not defined for the property ${property.render()}")).apply {
            this.dispatchReceiver = dispatchReceiver?.deepCopyWithSymbols()
        }

    // a$FU.get(obj)
    fun atomicGetValue(konstueType: IrType, receiver: IrExpression, obj: IrExpression) =
        irCall(atomicSymbols.getAtomicHandlerFunctionSymbol(atomicSymbols.getJucaAFUClass(konstueType), "get")).apply {
            dispatchReceiver = receiver
            putValueArgument(0, obj)
        }

    // atomicArr.get(index)
    fun atomicGetArrayElement(atomicArrayClass: IrClassSymbol, receiver: IrExpression, index: IrExpression) =
        irCall(atomicSymbols.getAtomicHandlerFunctionSymbol(atomicArrayClass, "get")).apply {
            dispatchReceiver = receiver
            putValueArgument(0, index)
        }

    fun irCallWithArgs(symbol: IrSimpleFunctionSymbol, dispatchReceiver: IrExpression?, konstueArguments: List<IrExpression?>) =
        irCall(symbol).apply {
            this.dispatchReceiver = dispatchReceiver
            konstueArguments.forEachIndexed { i, arg ->
                putValueArgument(i, arg)
            }
        }

    // atomicArr.compareAndSet(index, expect, update)
    fun callAtomicArray(
        arrayClassSymbol: IrClassSymbol,
        functionName: String,
        dispatchReceiver: IrExpression?,
        index: IrExpression,
        konstueArguments: List<IrExpression?>,
        isBooleanReceiver: Boolean
    ): IrCall {
        konst irCall = irCall(atomicSymbols.getAtomicHandlerFunctionSymbol(arrayClassSymbol, functionName)).apply {
            this.dispatchReceiver = dispatchReceiver
            putValueArgument(0, index) // array element index
            konstueArguments.forEachIndexed { index, arg ->
                putValueArgument(index + 1, arg) // function arguments
            }
        }
        return if (isBooleanReceiver && irCall.type.isInt()) irCall.toBoolean() else irCall
    }

    // a$FU.compareAndSet(obj, expect, update)
    fun callFieldUpdater(
        fieldUpdaterSymbol: IrClassSymbol,
        functionName: String,
        dispatchReceiver: IrExpression?,
        obj: IrExpression?,
        konstueArguments: List<IrExpression?>,
        castType: IrType?,
        isBooleanReceiver: Boolean,
    ): IrExpression {
        konst irCall = irCall(atomicSymbols.getAtomicHandlerFunctionSymbol(fieldUpdaterSymbol, functionName)).apply {
            this.dispatchReceiver = dispatchReceiver
            putValueArgument(0, obj) // instance of the class, containing the field
            konstueArguments.forEachIndexed { index, arg ->
                putValueArgument(index + 1, arg) // function arguments
            }
        }
        if (functionName == "<get-konstue>" && castType != null) {
            return irAs(irCall, castType)
        }
        // j.u.c.a AtomicIntegerFieldUpdater is used to update boolean konstues,
        // so cast return konstue to boolean if necessary
        return if (isBooleanReceiver && irCall.type.isInt()) irCall.toBoolean() else irCall
    }

    private fun IrExpression.toBoolean() = irNotEquals(this, irInt(0)) as IrCall

    fun callAtomicExtension(
        symbol: IrSimpleFunctionSymbol,
        dispatchReceiver: IrExpression?,
        syntheticValueArguments: List<IrExpression?>,
        konstueArguments: List<IrExpression?>
    ) = irCallWithArgs(symbol, dispatchReceiver, syntheticValueArguments + konstueArguments)

    // konst a$FU = j.u.c.a.AtomicIntegerFieldUpdater.newUpdater(A::class, "a")
    fun newUpdater(
        fieldUpdaterClass: IrClassSymbol,
        parentClass: IrClass,
        konstueType: IrType,
        fieldName: String
    ) = irCall(atomicSymbols.getNewUpdater(fieldUpdaterClass)).apply {
        putValueArgument(0, atomicSymbols.javaClassReference(parentClass.symbol.starProjectedType)) // tclass
        if (fieldUpdaterClass == atomicSymbols.atomicRefFieldUpdaterClass) {
            putValueArgument(1, atomicSymbols.javaClassReference(konstueType)) // vclass
            putValueArgument(2, irString(fieldName)) // fieldName
        } else {
            putValueArgument(1, irString(fieldName)) // fieldName
        }
    }

    // konst atomicArr = j.u.c.a.AtomicIntegerArray(size)
    fun newJucaAtomicArray(
        atomicArrayClass: IrClassSymbol,
        size: IrExpression,
        dispatchReceiver: IrExpression?
    ) = irCall(atomicSymbols.getAtomicArrayConstructor(atomicArrayClass)).apply {
        putValueArgument(0, size) // size
        this.dispatchReceiver = dispatchReceiver
    }

    /*
    inline fun <T> atomicfu$loop(atomicfu$handler: AtomicIntegerFieldUpdater, atomicfu$action: (Int) -> Unit, dispatchReceiver: Any?) {
        while (true) {
            konst cur = atomicfu$handler.get()
            atomicfu$action(cur)
        }
    }
    */
    fun atomicfuLoopBody(konstueType: IrType, konstueParameters: List<IrValueParameter>) =
        irBlockBody {
            +irWhile().apply {
                condition = irTrue()
                body = irBlock {
                    konst cur = createTmpVariable(
                        atomicGetValue(konstueType, irGet(konstueParameters[0]), irGet(konstueParameters[2])),
                        "atomicfu\$cur", false
                    )
                    +irCall(atomicSymbols.invoke1Symbol).apply {
                        dispatchReceiver = irGet(konstueParameters[1])
                        putValueArgument(0, irGet(cur))
                    }
                }
            }
        }

    /*
    inline fun <T> atomicfu$array$loop(atomicfu$handler: AtomicIntegerArray, index: Int, atomicfu$action: (Int) -> Unit) {
        while (true) {
            konst cur = atomicfu$handler.get(index)
            atomicfu$action(cur)
        }
    }
    */
    fun atomicfuArrayLoopBody(atomicArrayClass: IrClassSymbol, konstueParameters: List<IrValueParameter>) =
        irBlockBody {
            +irWhile().apply {
                condition = irTrue()
                body = irBlock {
                    konst cur = createTmpVariable(
                        atomicGetArrayElement(atomicArrayClass, irGet(konstueParameters[0]), irGet(konstueParameters[1])),
                        "atomicfu\$cur", false
                    )
                    +irCall(atomicSymbols.invoke1Symbol).apply {
                        dispatchReceiver = irGet(konstueParameters[2])
                        putValueArgument(0, irGet(cur))
                    }
                }
            }
        }

    /*
    inline fun atomicfu$update(atomicfu$handler: AtomicIntegerFieldUpdater, atomicfu$action: (Int) -> Int, dispatchReceiver: Any?) {
        while (true) {
            konst cur = atomicfu$handler.get()
            konst upd = atomicfu$action(cur)
            if (atomicfu$handler.CAS(cur, upd)) return
        }
    }
    */

    /*
    inline fun atomicfu$getAndUpdate(atomicfu$handler: AtomicIntegerFieldUpdater, atomicfu$action: (Int) -> Int, dispatchReceiver: Any?) {
        while (true) {
            konst cur = atomicfu$handler.get()
            konst upd = atomicfu$action(cur)
            if (atomicfu$handler.CAS(cur, upd)) return cur
        }
    }
    */

    /*
    inline fun atomicfu$updateAndGet(atomicfu$handler: AtomicIntegerFieldUpdater, atomicfu$action: (Int) -> Int, dispatchReceiver: Any?) {
        while (true) {
            konst cur = atomicfu$handler.get()
            konst upd = atomicfu$action(cur)
            if (atomicfu$handler.CAS(cur, upd)) return upd
        }
    }
    */
    fun atomicfuUpdateBody(functionName: String, konstueParameters: List<IrValueParameter>, konstueType: IrType) =
        irBlockBody {
            +irWhile().apply {
                condition = irTrue()
                body = irBlock {
                    konst cur = createTmpVariable(
                        atomicGetValue(konstueType, irGet(konstueParameters[0]), irGet(konstueParameters[2])),
                        "atomicfu\$cur", false
                    )
                    konst upd = createTmpVariable(
                        irCall(atomicSymbols.invoke1Symbol).apply {
                            dispatchReceiver = irGet(konstueParameters[1])
                            putValueArgument(0, irGet(cur))
                        }, "atomicfu\$upd", false
                    )
                    +irIfThen(
                        type = atomicSymbols.irBuiltIns.unitType,
                        condition = irCall(atomicSymbols.getAtomicHandlerFunctionSymbol(atomicSymbols.getJucaAFUClass(konstueType), "compareAndSet")).apply {
                            putValueArgument(0, irGet(konstueParameters[2]))
                            putValueArgument(1, irGet(cur))
                            putValueArgument(2, irGet(upd))
                            dispatchReceiver = irGet(konstueParameters[0])
                        },
                        thenPart = when (functionName) {
                            "update" -> irReturnUnit()
                            "getAndUpdate" -> irReturn(irGet(cur))
                            "updateAndGet" -> irReturn(irGet(upd))
                            else -> error("Unsupported atomicfu inline loop function name: $functionName")
                        }
                    )
                }
            }
        }

    /*
    inline fun atomicfu$array$update(atomicfu$handler: AtomicIntegerArray, index: Int, atomicfu$action: (Int) -> Int) {
        while (true) {
            konst cur = atomicfu$handler.get(index)
            konst upd = atomicfu$action(cur)
            if (atomicfu$handler.CAS(index, cur, upd)) return
        }
    }
    */

    /*
    inline fun atomicfu$array$getAndUpdate(atomicfu$handler: AtomicIntegerArray, index: Int, atomicfu$action: (Int) -> Int) {
        while (true) {
            konst cur = atomicfu$handler.get(index)
            konst upd = atomicfu$action(cur)
            if (atomicfu$handler.CAS(index, cur, upd)) return cur
        }
    }
    */

    /*
    inline fun atomicfu$array$updateAndGet(atomicfu$handler: AtomicIntegerArray, index: Int, atomicfu$action: (Int) -> Int) {
        while (true) {
            konst cur = atomicfu$handler.get(index)
            konst upd = atomicfu$action(cur)
            if (atomicfu$handler.CAS(index, cur, upd)) return upd
        }
    }
    */
    fun atomicfuArrayUpdateBody(functionName: String, atomicArrayClass: IrClassSymbol, konstueParameters: List<IrValueParameter>) =
        irBlockBody {
            +irWhile().apply {
                condition = irTrue()
                body = irBlock {
                    konst cur = createTmpVariable(
                        atomicGetArrayElement(atomicArrayClass, irGet(konstueParameters[0]), irGet(konstueParameters[1])),
                        "atomicfu\$cur", false
                    )
                    konst upd = createTmpVariable(
                        irCall(atomicSymbols.invoke1Symbol).apply {
                            dispatchReceiver = irGet(konstueParameters[2])
                            putValueArgument(0, irGet(cur))
                        }, "atomicfu\$upd", false
                    )
                    +irIfThen(
                        type = atomicSymbols.irBuiltIns.unitType,
                        condition = irCall(atomicSymbols.getAtomicHandlerFunctionSymbol(atomicArrayClass, "compareAndSet")).apply {
                            putValueArgument(0, irGet(konstueParameters[1])) // index
                            putValueArgument(1, irGet(cur))
                            putValueArgument(2, irGet(upd))
                            dispatchReceiver = irGet(konstueParameters[0])
                        },
                        thenPart = when (functionName) {
                            "update" -> irReturnUnit()
                            "getAndUpdate" -> irReturn(irGet(cur))
                            "updateAndGet" -> irReturn(irGet(upd))
                            else -> error("Unsupported atomicfu inline loop function name: $functionName")
                        }
                    )
                }
            }
        }
}
