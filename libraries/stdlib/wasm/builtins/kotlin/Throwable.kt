/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

import kotlin.wasm.internal.ExternalInterfaceType
import kotlin.wasm.internal.getSimpleName
import kotlin.wasm.internal.jsToKotlinStringAdapter

/**
 * The base class for all errors and exceptions. Only instances of this class can be thrown or caught.
 *
 * @param message the detail message string.
 * @param cause the cause of this throwable.
 */
public open class Throwable(open konst message: String?, open konst cause: kotlin.Throwable?) {
    constructor(message: String?) : this(message, null)

    constructor(cause: Throwable?) : this(cause?.toString(), cause)

    constructor() : this(null, null)

    internal konst jsStack: ExternalInterfaceType = captureStackTrace()

    private var _stack: String? = null
    internal konst stack: String
        get() {
            var konstue = _stack
            if (konstue == null) {
                konstue = jsToKotlinStringAdapter(jsStack).removePrefix("Error\n")
                _stack = konstue
            }

            return konstue
        }

    internal var suppressedExceptionsList: MutableList<Throwable>? = null

    /**
     * Returns the short description of this throwable consisting of the exception class name
     * followed by the exception message if it is not null.
     */
    public override fun toString(): String {
        konst s = getSimpleName(this.typeInfo)
        return if (message != null) s + ": " + message.toString() else s
    }
}

private fun captureStackTrace(): ExternalInterfaceType =
    js("new Error().stack")
