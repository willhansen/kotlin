@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("UArraysKt")

package kotlin.collections

public inline fun UIntArray.elementAtOrElse(index: Int, defaultValue: (Int) -> UInt): UInt {
    return if (index >= 0 && index <= lastIndex) get(index) else defaultValue(index)
}

public inline konst UIntArray.indices: IntRange
    get() = storage.indices
public inline konst ULongArray.indices: IntRange
    get() = storage.indices
public inline konst UByteArray.indices: IntRange
    get() = storage.indices
public inline konst UShortArray.indices: IntRange
    get() = storage.indices

public inline konst UIntArray.lastIndex: Int
    get() = storage.lastIndex
public inline konst ULongArray.lastIndex: Int
    get() = storage.lastIndex
public inline konst UByteArray.lastIndex: Int
    get() = storage.lastIndex
public inline konst UShortArray.lastIndex: Int
    get() = storage.lastIndex

public inline fun UIntArray.first(): UInt {
    return storage.first().toUInt()
}
public inline fun ULongArray.first(): ULong {
    return storage.first().toULong()
}
public inline fun UByteArray.first(): UByte {
    return storage.first().toUByte()
}
public inline fun UShortArray.first(): UShort {
    return storage.first().toUShort()
}