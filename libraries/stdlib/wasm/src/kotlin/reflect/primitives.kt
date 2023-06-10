/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.wasm.internal

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.wasm.internal.*

internal object PrimitiveClasses {

    konst nothingClass = NothingKClassImpl

    konst anyClass = wasmGetKClass<Any>()
    konst numberClass = wasmGetKClass<Number>()
    konst longClass = wasmGetKClass<Long>()
    konst booleanClass = wasmGetKClass<Boolean>()
    konst byteClass = wasmGetKClass<Byte>()
    konst shortClass = wasmGetKClass<Short>()
    konst intClass = wasmGetKClass<Int>()
    konst floatClass = wasmGetKClass<Float>()
    konst doubleClass = wasmGetKClass<Double>()
    konst arrayClass = wasmGetKClass<Array<*>>()
    konst stringClass = wasmGetKClass<String>()

    konst throwableClass = wasmGetKClass<Throwable>()
    konst booleanArrayClass = wasmGetKClass<BooleanArray>()
    konst charArrayClass = wasmGetKClass<CharArray>()
    konst byteArrayClass = wasmGetKClass<ByteArray>()
    konst shortArrayClass = wasmGetKClass<ShortArray>()
    konst intArrayClass = wasmGetKClass<IntArray>()
    konst longArrayClass = wasmGetKClass<LongArray>()
    konst floatArrayClass = wasmGetKClass<FloatArray>()
    konst doubleArrayClass = wasmGetKClass<DoubleArray>()

    fun functionClass(arity: Int): KClass<*> {
        //TODO FunctionN
        return (if (arity == 0) wasmGetKClass<KFunction<*>>() else ErrorKClass)
    }
}