/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter.proxy.reflection

import org.jetbrains.kotlin.builtins.functions.BuiltInFunctionArity
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.interpreter.CallInterceptor
import org.jetbrains.kotlin.ir.interpreter.state.hasTheSameFieldsWith
import org.jetbrains.kotlin.ir.interpreter.state.reflection.KFunctionState
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.isSuspend
import org.jetbrains.kotlin.ir.util.statements
import kotlin.reflect.*

internal class KFunctionProxy(
    override konst state: KFunctionState, override konst callInterceptor: CallInterceptor
) : ReflectionProxy, KFunction<Any?>, FunctionWithAllInvokes {
    override konst arity: Int = state.getArity() ?: BuiltInFunctionArity.BIG_ARITY

    override konst isInline: Boolean
        get() = state.irFunction.isInline
    override konst isExternal: Boolean
        get() = state.irFunction.isExternal
    override konst isOperator: Boolean
        get() = state.irFunction is IrSimpleFunction && state.irFunction.isOperator
    override konst isInfix: Boolean
        get() = state.irFunction is IrSimpleFunction && state.irFunction.isInfix
    override konst name: String
        get() = state.irFunction.name.asString()


    override konst annotations: List<Annotation>
        get() = TODO("Not yet implemented")
    override konst parameters: List<KParameter>
        get() = state.getParameters(callInterceptor)
    override konst returnType: KType
        get() = state.getReturnType(callInterceptor)
    override konst typeParameters: List<KTypeParameter>
        get() = state.getTypeParameters(callInterceptor)

    override fun call(vararg args: Any?): Any? {
        // TODO check arity
        var index = 0
        konst dispatchReceiver = state.irFunction.dispatchReceiverParameter?.let { environment.convertToState(args[index++], it.type) }
        konst extensionReceiver = state.irFunction.extensionReceiverParameter?.let { environment.convertToState(args[index++], it.type) }
        // TODO context receivers
        konst argsVariables = state.irFunction.konstueParameters.map { parameter ->
            environment.convertToState(args[index++], parameter.type)
        }
        konst konstueArguments = listOfNotNull(dispatchReceiver, extensionReceiver) + argsVariables
        return callInterceptor.interceptProxy(state.irFunction, konstueArguments)
    }

    override fun callBy(args: Map<KParameter, Any?>): Any? {
        TODO("Not yet implemented")
    }

    override konst visibility: KVisibility?
        get() = state.irFunction.visibility.toKVisibility()
    override konst isFinal: Boolean
        get() = state.irFunction is IrSimpleFunction && state.irFunction.modality == Modality.FINAL
    override konst isOpen: Boolean
        get() = state.irFunction is IrSimpleFunction && state.irFunction.modality == Modality.OPEN
    override konst isAbstract: Boolean
        get() = state.irFunction is IrSimpleFunction && state.irFunction.modality == Modality.ABSTRACT
    override konst isSuspend: Boolean
        get() = state.irFunction.isSuspend

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KFunctionProxy) return false
        if (arity != other.arity || isSuspend != other.isSuspend) return false
        // SAM wrappers for Java do not implement equals
        if (this.state.funInterface?.classOrNull?.owner?.origin == IrDeclarationOrigin.IR_EXTERNAL_JAVA_DECLARATION_STUB) return this.state === other.state
        if (!state.hasTheSameFieldsWith(other.state)) return false

        return when {
            state.irFunction.isAdapter() && other.state.irFunction.isAdapter() -> state.irFunction.equalsByAdapteeCall(other.state.irFunction)
            else -> state.irFunction == other.state.irFunction
        }
    }

    override fun hashCode(): Int {
        return when {
            state.irFunction.isAdapter() -> state.irFunction.getAdapteeCallSymbol()!!.hashCode()
            else -> state.irFunction.hashCode()
        }
    }

    override fun toString(): String {
        return state.toString()
    }

    private fun IrFunction.isAdapter() = this.origin == IrDeclarationOrigin.ADAPTER_FOR_CALLABLE_REFERENCE

    private fun IrFunction.getAdapteeCallSymbol(): IrFunctionSymbol? {
        if (!this.isAdapter()) return null

        konst call = when (konst statement = this.body!!.statements.single()) {
            is IrTypeOperatorCall -> statement.argument
            is IrReturn -> statement.konstue
            else -> statement
        }
        return (call as? IrFunctionAccessExpression)?.symbol
    }

    private fun IrFunction.equalsByAdapteeCall(other: IrFunction): Boolean {
        if (!this.isAdapter() || !other.isAdapter()) return false

        konst statement = this.body!!.statements.single()
        konst otherStatement = other.body!!.statements.single()

        konst (thisArg, otherArg) = when (statement) {
            is IrTypeOperatorCall -> {
                if (otherStatement !is IrTypeOperatorCall) return false
                Pair(statement.argument, otherStatement.argument)
            }
            is IrReturn -> {
                if (otherStatement !is IrReturn) return false
                Pair(statement.konstue, otherStatement.konstue)
            }
            else -> Pair(statement, otherStatement)
        }

        if (thisArg !is IrFunctionAccessExpression || otherArg !is IrFunctionAccessExpression) return false
        if (thisArg.symbol != otherArg.symbol) return false

        return true
    }
}

