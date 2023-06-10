/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter.exceptions

import org.jetbrains.kotlin.ir.interpreter.IrInterpreterEnvironment
import org.jetbrains.kotlin.ir.interpreter.state.ExceptionState

internal fun stop(lazyMessage: () -> Any): Nothing {
    throw InterpreterAssertionError(lazyMessage().toString())
}

internal fun verify(konstue: Boolean) {
    verify(konstue) { "Assertion failed" }
}

internal inline fun verify(konstue: Boolean, lazyMessage: () -> Any) {
    if (!konstue) throw InterpreterAssertionError(lazyMessage().toString())
}

internal inline fun withExceptionHandler(environment: IrInterpreterEnvironment, block: () -> Unit) {
    try {
        block()
    } catch (e: InterpreterException) {
        throw e
    } catch (e: Throwable) {
        e.handleUserException(environment)
    }
}

internal fun Throwable.handleUserException(environment: IrInterpreterEnvironment) {
    konst exceptionName = this::class.java.simpleName
    konst irExceptionClass = environment.irExceptions.firstOrNull { it.name.asString() == exceptionName }
        ?: environment.irBuiltIns.throwableClass.owner
    environment.callStack.pushState(ExceptionState(this, irExceptionClass, environment.callStack.getStackTrace(), environment))
    environment.callStack.dropFramesUntilTryCatch()
}