/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FINAL_UPPER_BOUND", "NOTHING_TO_INLINE")

package kotlinx.cinterop

@ExperimentalForeignApi
@JvmName("plus\$Byte")
inline operator fun <T : ByteVarOf<*>> CPointer<T>?.plus(index: Long): CPointer<T>? =
        interpretCPointer(this.rawValue + index * 1)

@ExperimentalForeignApi
@JvmName("plus\$Byte")
inline operator fun <T : ByteVarOf<*>> CPointer<T>?.plus(index: Int): CPointer<T>? =
        this + index.toLong()

@ExperimentalForeignApi
@JvmName("get\$Byte")
inline operator fun <T : Byte> CPointer<ByteVarOf<T>>.get(index: Int): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
@JvmName("set\$Byte")
inline operator fun <T : Byte> CPointer<ByteVarOf<T>>.set(index: Int, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("get\$Byte")
inline operator fun <T : Byte> CPointer<ByteVarOf<T>>.get(index: Long): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
@JvmName("set\$Byte")
inline operator fun <T : Byte> CPointer<ByteVarOf<T>>.set(index: Long, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("plus\$Short")
inline operator fun <T : ShortVarOf<*>> CPointer<T>?.plus(index: Long): CPointer<T>? =
        interpretCPointer(this.rawValue + index * 2)

@ExperimentalForeignApi
@JvmName("plus\$Short")
inline operator fun <T : ShortVarOf<*>> CPointer<T>?.plus(index: Int): CPointer<T>? =
        this + index.toLong()

@ExperimentalForeignApi
@JvmName("get\$Short")
inline operator fun <T : Short> CPointer<ShortVarOf<T>>.get(index: Int): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
@JvmName("set\$Short")
inline operator fun <T : Short> CPointer<ShortVarOf<T>>.set(index: Int, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("get\$Short")
inline operator fun <T : Short> CPointer<ShortVarOf<T>>.get(index: Long): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
@JvmName("set\$Short")
inline operator fun <T : Short> CPointer<ShortVarOf<T>>.set(index: Long, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("plus\$Int")
inline operator fun <T : IntVarOf<*>> CPointer<T>?.plus(index: Long): CPointer<T>? =
        interpretCPointer(this.rawValue + index * 4)

@ExperimentalForeignApi
@JvmName("plus\$Int")
inline operator fun <T : IntVarOf<*>> CPointer<T>?.plus(index: Int): CPointer<T>? =
        this + index.toLong()

@ExperimentalForeignApi
@JvmName("get\$Int")
inline operator fun <T : Int> CPointer<IntVarOf<T>>.get(index: Int): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
@JvmName("set\$Int")
inline operator fun <T : Int> CPointer<IntVarOf<T>>.set(index: Int, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("get\$Int")
inline operator fun <T : Int> CPointer<IntVarOf<T>>.get(index: Long): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
@JvmName("set\$Int")
inline operator fun <T : Int> CPointer<IntVarOf<T>>.set(index: Long, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("plus\$Long")
inline operator fun <T : LongVarOf<*>> CPointer<T>?.plus(index: Long): CPointer<T>? =
        interpretCPointer(this.rawValue + index * 8)

@ExperimentalForeignApi
@JvmName("plus\$Long")
inline operator fun <T : LongVarOf<*>> CPointer<T>?.plus(index: Int): CPointer<T>? =
        this + index.toLong()

@ExperimentalForeignApi
@JvmName("get\$Long")
inline operator fun <T : Long> CPointer<LongVarOf<T>>.get(index: Int): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
@JvmName("set\$Long")
inline operator fun <T : Long> CPointer<LongVarOf<T>>.set(index: Int, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("get\$Long")
inline operator fun <T : Long> CPointer<LongVarOf<T>>.get(index: Long): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
@JvmName("set\$Long")
inline operator fun <T : Long> CPointer<LongVarOf<T>>.set(index: Long, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("plus\$UByte")
inline operator fun <T : UByteVarOf<*>> CPointer<T>?.plus(index: Long): CPointer<T>? =
        interpretCPointer(this.rawValue + index * 1)

@ExperimentalForeignApi
@JvmName("plus\$UByte")
inline operator fun <T : UByteVarOf<*>> CPointer<T>?.plus(index: Int): CPointer<T>? =
        this + index.toLong()

@ExperimentalForeignApi
@JvmName("get\$UByte")
inline operator fun <T : UByte> CPointer<UByteVarOf<T>>.get(index: Int): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
inline operator fun <T : UByte> CPointer<UByteVarOf<T>>.set(index: Int, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
inline operator fun <T : UByte> CPointer<UByteVarOf<T>>.get(index: Long): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
inline operator fun <T : UByte> CPointer<UByteVarOf<T>>.set(index: Long, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("plus\$UShort")
inline operator fun <T : UShortVarOf<*>> CPointer<T>?.plus(index: Long): CPointer<T>? =
        interpretCPointer(this.rawValue + index * 2)

@ExperimentalForeignApi
@JvmName("plus\$UShort")
inline operator fun <T : UShortVarOf<*>> CPointer<T>?.plus(index: Int): CPointer<T>? =
        this + index.toLong()

@ExperimentalForeignApi
@JvmName("get\$UShort")
inline operator fun <T : UShort> CPointer<UShortVarOf<T>>.get(index: Int): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
inline operator fun <T : UShort> CPointer<UShortVarOf<T>>.set(index: Int, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("get\$UShort")
inline operator fun <T : UShort> CPointer<UShortVarOf<T>>.get(index: Long): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
inline operator fun <T : UShort> CPointer<UShortVarOf<T>>.set(index: Long, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("plus\$UInt")
inline operator fun <T : UIntVarOf<*>> CPointer<T>?.plus(index: Long): CPointer<T>? =
        interpretCPointer(this.rawValue + index * 4)

@ExperimentalForeignApi
@JvmName("plus\$UInt")
inline operator fun <T : UIntVarOf<*>> CPointer<T>?.plus(index: Int): CPointer<T>? =
        this + index.toLong()

@ExperimentalForeignApi
@JvmName("get\$UInt")
inline operator fun <T : UInt> CPointer<UIntVarOf<T>>.get(index: Int): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
inline operator fun <T : UInt> CPointer<UIntVarOf<T>>.set(index: Int, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("get\$UInt")
inline operator fun <T : UInt> CPointer<UIntVarOf<T>>.get(index: Long): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
inline operator fun <T : UInt> CPointer<UIntVarOf<T>>.set(index: Long, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("plus\$ULong")
inline operator fun <T : ULongVarOf<*>> CPointer<T>?.plus(index: Long): CPointer<T>? =
        interpretCPointer(this.rawValue + index * 8)

@ExperimentalForeignApi
@JvmName("plus\$ULong")
inline operator fun <T : ULongVarOf<*>> CPointer<T>?.plus(index: Int): CPointer<T>? =
        this + index.toLong()

@ExperimentalForeignApi
@JvmName("get\$ULong")
inline operator fun <T : ULong> CPointer<ULongVarOf<T>>.get(index: Int): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
inline operator fun <T : ULong> CPointer<ULongVarOf<T>>.set(index: Int, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("get\$ULong")
inline operator fun <T : ULong> CPointer<ULongVarOf<T>>.get(index: Long): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
inline operator fun <T : ULong> CPointer<ULongVarOf<T>>.set(index: Long, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("plus\$Float")
inline operator fun <T : FloatVarOf<*>> CPointer<T>?.plus(index: Long): CPointer<T>? =
        interpretCPointer(this.rawValue + index * 4)

@ExperimentalForeignApi
@JvmName("plus\$Float")
inline operator fun <T : FloatVarOf<*>> CPointer<T>?.plus(index: Int): CPointer<T>? =
        this + index.toLong()

@ExperimentalForeignApi
@JvmName("get\$Float")
inline operator fun <T : Float> CPointer<FloatVarOf<T>>.get(index: Int): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
@JvmName("set\$Float")
inline operator fun <T : Float> CPointer<FloatVarOf<T>>.set(index: Int, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("get\$Float")
inline operator fun <T : Float> CPointer<FloatVarOf<T>>.get(index: Long): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
@JvmName("set\$Float")
inline operator fun <T : Float> CPointer<FloatVarOf<T>>.set(index: Long, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("plus\$Double")
inline operator fun <T : DoubleVarOf<*>> CPointer<T>?.plus(index: Long): CPointer<T>? =
        interpretCPointer(this.rawValue + index * 8)

@ExperimentalForeignApi
@JvmName("plus\$Double")
inline operator fun <T : DoubleVarOf<*>> CPointer<T>?.plus(index: Int): CPointer<T>? =
        this + index.toLong()

@ExperimentalForeignApi
@JvmName("get\$Double")
inline operator fun <T : Double> CPointer<DoubleVarOf<T>>.get(index: Int): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
@JvmName("set\$Double")
inline operator fun <T : Double> CPointer<DoubleVarOf<T>>.set(index: Int, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@JvmName("get\$Double")
inline operator fun <T : Double> CPointer<DoubleVarOf<T>>.get(index: Long): T =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
@JvmName("set\$Double")
inline operator fun <T : Double> CPointer<DoubleVarOf<T>>.set(index: Long, konstue: T) {
    (this + index)!!.pointed.konstue = konstue
}

/* Seva: Used to be generated by:

Seva: It probably means the reasoning of this API and is general applicability should be revisited

#!/bin/bash

function gen {
echo "@JvmName(\"plus\\\$$1\")"
echo "inline operator fun <T : ${1}VarOf<*>> CPointer<T>?.plus(index: Long): CPointer<T>? ="
echo "        interpretCPointer(this.rawValue + index * ${2})"
echo
echo "@JvmName(\"plus\\\$$1\")"
echo "inline operator fun <T : ${1}VarOf<*>> CPointer<T>?.plus(index: Int): CPointer<T>? ="
echo "        this + index.toLong()"
echo
echo "@JvmName(\"get\\\$$1\")"
echo "inline operator fun <T : $1> CPointer<${1}VarOf<T>>.get(index: Int): T ="
echo "        (this + index)!!.pointed.konstue"
echo
echo "@JvmName(\"set\\\$$1\")"
echo "inline operator fun <T : $1> CPointer<${1}VarOf<T>>.set(index: Int, konstue: T) {"
echo "    (this + index)!!.pointed.konstue = konstue"
echo '}'
echo
echo "@JvmName(\"get\\\$$1\")"
echo "inline operator fun <T : $1> CPointer<${1}VarOf<T>>.get(index: Long): T ="
echo "        (this + index)!!.pointed.konstue"
echo
echo "@JvmName(\"set\\\$$1\")"
echo "inline operator fun <T : $1> CPointer<${1}VarOf<T>>.set(index: Long, konstue: T) {"
echo "    (this + index)!!.pointed.konstue = konstue"
echo '}'
echo
}

gen Byte 1
gen Short 2
gen Int 4
gen Long 8
gen UByte 1
gen UShort 2
gen UInt 4
gen ULong 8
gen Float 4
gen Double 8

 */
