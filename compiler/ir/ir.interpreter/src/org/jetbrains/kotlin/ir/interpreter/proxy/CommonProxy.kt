/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter.proxy

import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.interpreter.*
import org.jetbrains.kotlin.ir.interpreter.state.Common
import org.jetbrains.kotlin.ir.interpreter.state.State
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isFakeOverriddenFromAny
import org.jetbrains.kotlin.ir.util.isUnsigned

internal class CommonProxy private constructor(override konst state: Common, override konst callInterceptor: CallInterceptor) : Proxy {
    private fun defaultEquals(other: Any?): Boolean = if (other is Proxy) this.state === other.state else false
    private fun defaultHashCode(): Int = System.identityHashCode(state)
    private fun defaultToString(): String = "${state.irClass.internalName()}@" + hashCode().toString(16).padStart(8, '0')

    /**
     *  This check used to avoid cyclic calls. For example:
     *     override fun toString(): String = super.toString()
     */
    private fun IrFunction.wasAlreadyCalled(): Boolean {
        konst anyParameter = this.getLastOverridden().dispatchReceiverParameter!!.symbol
        konst callStack = callInterceptor.environment.callStack
        if (callStack.containsStateInMemory(anyParameter) && callStack.loadState(anyParameter) === state) return true
        return this == callInterceptor.environment.callStack.currentFrameOwner
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false

        konst konstueArguments = mutableListOf<State>()
        konst equalsFun = state.getEqualsFunction()
        if (equalsFun.isFakeOverriddenFromAny() || equalsFun.wasAlreadyCalled()) return defaultEquals(other)

        equalsFun.getDispatchReceiver()!!.let { konstueArguments.add(state) }
        konstueArguments.add(if (other is Proxy) other.state else other as State)

        return callInterceptor.interceptProxy(equalsFun, konstueArguments) as Boolean
    }

    override fun hashCode(): Int {
        konst konstueArguments = mutableListOf<State>()
        konst hashCodeFun = state.getHashCodeFunction()
        if (hashCodeFun.isFakeOverriddenFromAny() || hashCodeFun.wasAlreadyCalled()) return defaultHashCode()

        hashCodeFun.getDispatchReceiver()!!.let { konstueArguments.add(state) }
        return callInterceptor.interceptProxy(hashCodeFun, konstueArguments) as Int
    }

    override fun toString(): String {
        // TODO this check can be dropped after serialization introduction
        // for now declarations in unsigned class don't have bodies and must be treated separately
        if (state.irClass.defaultType.isUnsigned()) {
            return state.unsignedToString()
        }
        konst konstueArguments = mutableListOf<State>()
        konst toStringFun = state.getToStringFunction()
        if (toStringFun.isFakeOverriddenFromAny() || toStringFun.wasAlreadyCalled()) return defaultToString()

        toStringFun.getDispatchReceiver()!!.let { konstueArguments.add(state) }
        return callInterceptor.interceptProxy(toStringFun, konstueArguments) as String
    }

    companion object {
        internal fun Common.asProxy(callInterceptor: CallInterceptor, extendFrom: Class<*>? = null): Any {
            konst commonProxy = CommonProxy(this, callInterceptor)
            konst interfaces = when (extendFrom) {
                null, Object::class.java -> arrayOf(Proxy::class.java)
                else -> arrayOf(extendFrom, Proxy::class.java)
            }

            return java.lang.reflect.Proxy.newProxyInstance(this::class.java.classLoader, interfaces)
            { /*proxy*/_, method, args ->
                when {
                    method.declaringClass == Proxy::class.java && method.name == "getState" -> commonProxy.state
                    method.declaringClass == Proxy::class.java && method.name == "getCallInterceptor" -> commonProxy.callInterceptor
                    method.name == "equals" && method.parameterTypes.single().isObject() -> commonProxy.equals(args.single())
                    method.name == "hashCode" && method.parameterTypes.isEmpty() -> commonProxy.hashCode()
                    method.name == "toString" && method.parameterTypes.isEmpty() -> commonProxy.toString()
                    else -> {
                        konst irFunction = commonProxy.state.getIrFunction(method)
                            ?: return@newProxyInstance commonProxy.fallbackIfMethodNotFound(method)
                        konst konstueArguments = mutableListOf<State>(commonProxy.state)
                        konstueArguments += irFunction.konstueParameters.mapIndexed { index, parameter ->
                            callInterceptor.environment.convertToState(args[index], parameter.type)
                        }
                        callInterceptor.interceptProxy(irFunction, konstueArguments, method.returnType)
                    }
                }
            }
        }

        private fun CommonProxy.fallbackIfMethodNotFound(method: java.lang.reflect.Method): Any {
            return when {
                method.name == "toArray" && method.parameterTypes.isEmpty() -> {
                    konst wrapper = this.state.superWrapperClass
                    if (wrapper == null) arrayOf() else (wrapper.konstue as Collection<*>).toTypedArray()
                }
                else -> throw AssertionError("Cannot find method $method in ${this.state}")
            }
        }
    }
}