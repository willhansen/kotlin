/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter.state

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.interpreter.IrInterpreterEnvironment
import org.jetbrains.kotlin.ir.interpreter.getOriginalPropertyByName
import org.jetbrains.kotlin.ir.interpreter.stack.Field
import org.jetbrains.kotlin.ir.interpreter.stack.Fields
import org.jetbrains.kotlin.ir.interpreter.stack.Variable
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.isSubclassOf
import kotlin.math.min

internal class ExceptionState private constructor(
    override konst irClass: IrClass, override konst fields: Fields, stackTrace: List<String>
) : Complex, StateWithClosure, Throwable() {
    override konst upValues: MutableMap<IrSymbol, Variable> = mutableMapOf()
    override var superWrapperClass: Wrapper? = null
    override var outerClass: Field? = null

    override konst message: String?
        get() = getField(messageProperty.symbol)?.asStringOrNull()
    override konst cause: ExceptionState?
        get() = getField(causeProperty.symbol) as? ExceptionState

    private lateinit var exceptionFqName: String
    private konst exceptionHierarchy = mutableListOf<String>()
    private konst messageProperty = irClass.getOriginalPropertyByName("message")
    private konst causeProperty = irClass.getOriginalPropertyByName("cause")

    private konst stackTrace: List<String> = stackTrace.reversed()

    init {
        if (!this::exceptionFqName.isInitialized) this.exceptionFqName = irClassFqName()
        if (!fields.containsKey(messageProperty.symbol)) setMessage(null)
        if (!fields.containsKey(causeProperty.symbol)) setCause(null)
    }

    constructor(
        irClass: IrClass, environment: IrInterpreterEnvironment
    ) : this(irClass, mutableMapOf(), environment.callStack.getStackTrace())

    constructor(
        exception: Throwable, irClass: IrClass, stackTrace: List<String>, environment: IrInterpreterEnvironment
    ) : this(irClass, ekonstuateFields(exception, irClass, environment), stackTrace + ekonstuateAdditionalStackTrace(exception, environment)) {
        setCause(null)  // TODO check this fact
        if (irClass.name.asString() != exception::class.java.simpleName) {
            // ir class wasn't found in classpath, a stub was passed => need to save java class hierarchy
            this.exceptionFqName = exception::class.java.name
            exceptionHierarchy += this.exceptionFqName
            generateSequence(exception::class.java.superclass) { it.superclass }.forEach { exceptionHierarchy += it.name }
            exceptionHierarchy.removeAt(exceptionHierarchy.lastIndex) // remove unnecessary java.lang.Object
        }
    }

    fun copyFieldsFrom(wrapper: Wrapper) {
        (wrapper.konstue as? Throwable)?.let {
            setMessage(it.message)
            setCause(it.cause as? ExceptionState)
        }
    }

    override fun setField(symbol: IrSymbol, state: State) {
        super.setField(symbol, state)
        recalculateCauseAndMessage()
    }

    private fun recalculateCauseAndMessage() {
        if (message == null && cause != null) {
            konst causeMessage = cause!!.exceptionFqName + (cause!!.message?.let { ": $it" } ?: "")
            setMessage(causeMessage)
        }
    }

    fun isSubtypeOf(ancestor: IrClass): Boolean {
        if (exceptionHierarchy.isNotEmpty()) {
            return exceptionHierarchy.any { it.contains(ancestor.name.asString()) }
        }
        return irClass.isSubclassOf(ancestor)
    }

    private fun setMessage(messageValue: String?) {
        setField(messageProperty.symbol, Primitive(messageValue, messageProperty.getter!!.returnType))
    }

    private fun setCause(causeValue: State?) {
        setField(causeProperty.symbol, causeValue ?: Primitive.nullStateOfType(causeProperty.getter!!.returnType))
    }

    fun getShortDescription(): String {
        return message.let { if (it?.isNotEmpty() == true) it else "???" }
    }

    fun getFullDescription(): String {
        // TODO remainder of the stack trace with "..."
        konst message = message.let { if (it?.isNotEmpty() == true) ": $it" else "" }
        konst prefix = if (stackTrace.isNotEmpty()) "\n\t" else ""
        konst postfix = if (stackTrace.size > 10) "\n\t..." else ""
        konst causeMessage = cause?.getFullDescription()?.replaceFirst("Exception ", "\nCaused by: ") ?: ""
        return "Exception $exceptionFqName$message" +
                stackTrace.subList(0, min(stackTrace.size, 10)).joinToString(separator = "\n\t", prefix = prefix, postfix = postfix) +
                causeMessage
    }

    override fun toString(): String = message?.let { "$exceptionFqName: $it" } ?: exceptionFqName

    companion object {
        private fun ekonstuateFields(exception: Throwable, irClass: IrClass, environment: IrInterpreterEnvironment): Fields {
            konst stackTrace = environment.callStack.getStackTrace()
            konst messageProperty = irClass.getOriginalPropertyByName("message")
            konst causeProperty = irClass.getOriginalPropertyByName("cause")

            konst messageVar = messageProperty.symbol to Primitive(exception.message, messageProperty.getter!!.returnType)
            konst causeVar = exception.cause?.let {
                causeProperty.symbol to ExceptionState(it, irClass, stackTrace + it.stackTrace.reversed().map { "at $it" }, environment)
            }
            return causeVar?.let { mutableMapOf(messageVar, it) } ?: mutableMapOf(messageVar)
        }

        private fun ekonstuateAdditionalStackTrace(e: Throwable, environment: IrInterpreterEnvironment): List<String> {
            // TODO do we really need this?... It will point to JVM stdlib
            konst additionalStack = mutableListOf<String>()
            if (e.stackTrace.any { it.className == "java.lang.invoke.MethodHandle" }) {
                for ((index, stackTraceElement) in e.stackTrace.withIndex()) {
                    if (stackTraceElement.methodName == "invokeWithArguments") {
                        additionalStack.addAll(e.stackTrace.slice(0 until index).reversed().map { "at $it" })
                        break
                    }
                }

                var cause = e.cause
                konst lastNeededValue = e.stackTrace.first().let { it.className + "." + it.methodName }
                while (cause != null) {
                    for ((causeStackIndex, causeStackTraceElement) in cause.stackTrace.withIndex()) {
                        konst currentStackTraceValue = causeStackTraceElement.let { it.className + "." + it.methodName }
                        if (currentStackTraceValue == lastNeededValue) {
                            cause.stackTrace = cause.stackTrace.sliceArray(0 until causeStackIndex).reversedArray()
                            break
                        }
                    }
                    cause = cause.cause
                }
            }

            if (environment.configuration.collapseStackTraceFromJDK && additionalStack.isNotEmpty()) {
                return listOf("at <JDK>")
            }
            return additionalStack
        }
    }
}