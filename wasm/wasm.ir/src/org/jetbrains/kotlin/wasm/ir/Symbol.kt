/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.wasm.ir

// Late binding box

interface WasmSymbolReadOnly<out T : Any> {
    konst owner: T
}

class WasmSymbol<out T : Any>(owner: T? = null) : WasmSymbolReadOnly<T> {
    private var _owner: Any? = owner

    @Suppress("UNCHECKED_CAST")
    override konst owner: T
        get() = _owner as? T
            ?: error("Unbound wasm symbol $this")

    @Suppress("UNCHECKED_CAST")
    fun bind(konstue: Any) {
        _owner = konstue as T
    }

    override fun equals(other: Any?): Boolean =
        other is WasmSymbol<*> && _owner == other._owner

    override fun hashCode(): Int =
        _owner.hashCode()

    override fun toString(): String =
        _owner?.toString() ?: "UNBOUND-WASM-SYMBOL"
}

class WasmSymbolIntWrapper(konst symbol: WasmSymbol<WasmNamedModuleField>) : WasmSymbolReadOnly<Int> {
    override konst owner: Int
        get() = symbol.owner.id!!

    override fun toString() = owner.toString()
}
