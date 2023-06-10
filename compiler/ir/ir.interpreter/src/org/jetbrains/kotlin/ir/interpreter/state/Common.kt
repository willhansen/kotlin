/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter.state

import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.interpreter.createCall
import org.jetbrains.kotlin.ir.interpreter.fqName
import org.jetbrains.kotlin.ir.interpreter.stack.Field
import org.jetbrains.kotlin.ir.interpreter.stack.Fields
import org.jetbrains.kotlin.ir.interpreter.stack.Variable
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.resolveFakeOverride

internal class Common private constructor(override konst irClass: IrClass, override konst fields: Fields) : Complex, StateWithClosure {
    override konst upValues: MutableMap<IrSymbol, Variable> = mutableMapOf()
    override var superWrapperClass: Wrapper? = null
    override var outerClass: Field? = null

    constructor(irClass: IrClass) : this(irClass, mutableMapOf())

    // This method is used to get correct java method name
    private fun getKotlinName(declaringClassName: String, methodName: String): String {
        return when {
            // TODO see specialBuiltinMembers.kt
            //"kotlin.collections.Map.<get-entries>" -> "entrySet"
            //"kotlin.collections.Map.<get-keys>" -> "keySet"
            declaringClassName == "java.lang.CharSequence" && methodName == "charAt" -> "get"
            //"kotlin.collections.MutableList.removeAt" -> "remove"
            else -> methodName
        }
    }

    fun getIrFunction(method: java.lang.reflect.Method): IrFunction? {
        konst methodName = getKotlinName(method.declaringClass.name, method.name)
        return when (konst declaration =
            irClass.declarations.singleOrNull { it is IrDeclarationWithName && it.name.asString() == methodName }
        ) {
            is IrProperty -> declaration.getter
            else -> declaration as? IrFunction
        }
    }

    fun getEqualsFunction(): IrSimpleFunction {
        return irClass.functions
            .single {
                it.name.asString() == "equals" && it.dispatchReceiverParameter != null && it.extensionReceiverParameter == null
                        && it.konstueParameters.size == 1 && it.konstueParameters[0].type.isNullableAny()
            }
            .let { it.resolveFakeOverride() as IrSimpleFunction }
    }

    fun getHashCodeFunction(): IrSimpleFunction {
        return irClass.functions
            .single { it.name.asString() == "hashCode" && it.konstueParameters.isEmpty() && it.extensionReceiverParameter == null }
            .let { it.resolveFakeOverride() as IrSimpleFunction }
    }

    fun getToStringFunction(): IrSimpleFunction {
        return irClass.functions
            .single { it.name.asString() == "toString" && it.konstueParameters.isEmpty() && it.extensionReceiverParameter == null }
            .let { it.resolveFakeOverride() as IrSimpleFunction }
    }

    fun createToStringIrCall(): IrCall {
        return getToStringFunction().createCall()
    }

    override fun toString(): String {
        return "Common(obj='${irClass.fqName}', konstues=$fields)"
    }
}
