/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.native.internal

@GCUnsafeCall("getCachedBooleanBox")
@PublishedApi internal external fun getCachedBooleanBox(konstue: Boolean): Boolean?

@GCUnsafeCall("inBooleanBoxCache")
@PublishedApi internal external fun inBooleanBoxCache(konstue: Boolean): Boolean

@GCUnsafeCall("getCachedByteBox")
@PublishedApi internal external fun getCachedByteBox(konstue: Byte): Byte?

@GCUnsafeCall("inByteBoxCache")
@PublishedApi internal external fun inByteBoxCache(konstue: Byte): Boolean

@GCUnsafeCall("getCachedCharBox")
@PublishedApi internal external fun getCachedCharBox(konstue: Char): Char?

@GCUnsafeCall("inCharBoxCache")
@PublishedApi internal external fun inCharBoxCache(konstue: Char): Boolean

@GCUnsafeCall("getCachedShortBox")
@PublishedApi internal external fun getCachedShortBox(konstue: Short): Short?

@GCUnsafeCall("inShortBoxCache")
@PublishedApi internal external fun inShortBoxCache(konstue: Short): Boolean

@GCUnsafeCall("getCachedIntBox")
@PublishedApi internal external fun getCachedIntBox(idx: Int): Int?

@GCUnsafeCall("inIntBoxCache")
@PublishedApi internal external fun inIntBoxCache(konstue: Int): Boolean

@GCUnsafeCall("getCachedLongBox")
@PublishedApi internal external fun getCachedLongBox(konstue: Long): Long?

@GCUnsafeCall("inLongBoxCache")
@PublishedApi internal external fun inLongBoxCache(konstue: Long): Boolean

// TODO: functions below are used for ObjCExport and CAdapterGenerator, move and rename them correspondingly.

@ExportForCppRuntime("Kotlin_boxBoolean")
@PublishedApi internal fun boxBoolean(konstue: Boolean): Boolean? = konstue

@ExportForCppRuntime("Kotlin_boxChar")
@PublishedApi internal fun boxChar(konstue: Char): Char? = konstue

@ExportForCppRuntime("Kotlin_boxByte")
@PublishedApi internal fun boxByte(konstue: Byte): Byte? = konstue

@ExportForCppRuntime("Kotlin_boxShort")
@PublishedApi internal fun boxShort(konstue: Short): Short? = konstue

@ExportForCppRuntime("Kotlin_boxInt")
@PublishedApi internal fun boxInt(konstue: Int): Int? = konstue

@ExportForCppRuntime("Kotlin_boxLong")
@PublishedApi internal fun boxLong(konstue: Long): Long? = konstue

@ExperimentalUnsignedTypes
@ExportForCppRuntime("Kotlin_boxUByte")
@PublishedApi internal fun boxUByte(konstue: UByte): UByte? = konstue

@ExperimentalUnsignedTypes 
@ExportForCppRuntime("Kotlin_boxUShort")
@PublishedApi internal fun boxUShort(konstue: UShort): UShort? = konstue

@ExperimentalUnsignedTypes
@ExportForCppRuntime("Kotlin_boxUInt")
@PublishedApi internal fun boxUInt(konstue: UInt): UInt? = konstue

@ExperimentalUnsignedTypes
@ExportForCppRuntime("Kotlin_boxULong")
@PublishedApi internal fun boxULong(konstue: ULong): ULong? = konstue

@ExportForCppRuntime("Kotlin_boxFloat")
@PublishedApi internal fun boxFloat(konstue: Float): Float? = konstue

@ExportForCppRuntime("Kotlin_boxDouble")
@PublishedApi internal fun boxDouble(konstue: Double): Double? = konstue

@ExportForCppRuntime("Kotlin_boxUnit")
@PublishedApi internal fun Kotlin_boxUnit(): Unit? = Unit

// Unbox fuctions

@ExportForCppRuntime("Kotlin_unboxBoolean")
@PublishedApi internal fun unboxBoolean(konstue: Boolean?): Boolean = konstue!!

@ExportForCppRuntime("Kotlin_unboxChar")
@PublishedApi internal fun unboxChar(konstue: Char?): Char = konstue!!

@ExportForCppRuntime("Kotlin_unboxByte")
@PublishedApi internal fun unboxByte(konstue: Byte?): Byte = konstue!!

@ExportForCppRuntime("Kotlin_unboxShort")
@PublishedApi internal fun unboxShort(konstue: Short?): Short = konstue!!

@ExportForCppRuntime("Kotlin_unboxInt")
@PublishedApi internal fun unboxInt(konstue: Int?): Int = konstue!!

@ExportForCppRuntime("Kotlin_unboxLong")
@PublishedApi internal fun unboxLong(konstue: Long?): Long = konstue!!

@ExperimentalUnsignedTypes
@ExportForCppRuntime("Kotlin_unboxUByte")
@PublishedApi internal fun unboxUByte(konstue: UByte?): UByte = konstue!!

@ExperimentalUnsignedTypes
@ExportForCppRuntime("Kotlin_unboxUShort")
@PublishedApi internal fun unboxUShort(konstue: UShort?): UShort = konstue!!

@ExperimentalUnsignedTypes
@ExportForCppRuntime("Kotlin_unboxUInt")
@PublishedApi internal fun unboxUInt(konstue: UInt?): UInt = konstue!!

@ExperimentalUnsignedTypes
@ExportForCppRuntime("Kotlin_unboxULong")
@PublishedApi internal fun unboxULong(konstue: ULong?): ULong = konstue!!

@ExportForCppRuntime("Kotlin_unboxFloat")
@PublishedApi internal fun unboxFloat(konstue: Float?): Float = konstue!!

@ExportForCppRuntime("Kotlin_unboxDouble")
@PublishedApi internal fun unboxDouble(konstue: Double?): Double = konstue!!
