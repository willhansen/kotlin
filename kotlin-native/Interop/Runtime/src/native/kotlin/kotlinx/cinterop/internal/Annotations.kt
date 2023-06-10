
package kotlinx.cinterop.internal

import kotlin.native.internal.InternalForKotlinNative

@InternalForKotlinNative
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class CStruct(konst spelling: String) {
    @Retention(AnnotationRetention.BINARY)
    @Target(
            AnnotationTarget.PROPERTY_GETTER,
            AnnotationTarget.PROPERTY_SETTER
    )
    annotation class MemberAt(konst offset: Long)

    @Retention(AnnotationRetention.BINARY)
    @Target(AnnotationTarget.PROPERTY_GETTER)
    annotation class ArrayMemberAt(konst offset: Long)

    @Retention(AnnotationRetention.BINARY)
    @Target(
            AnnotationTarget.PROPERTY_GETTER,
            AnnotationTarget.PROPERTY_SETTER
    )
    annotation class BitField(konst offset: Long, konst size: Int)

    @Retention(AnnotationRetention.BINARY)
    annotation class VarType(konst size: Long, konst align: Int)

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    annotation class CPlusPlusClass

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    annotation class ManagedType
}

@Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER
)
@InternalForKotlinNative
@Retention(AnnotationRetention.BINARY)
public annotation class CCall(konst id: String) {
    @Target(AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.BINARY)
    annotation class CString

    @Target(AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.BINARY)
    annotation class WCString

    @Target(
            AnnotationTarget.FUNCTION,
            AnnotationTarget.PROPERTY_GETTER,
            AnnotationTarget.PROPERTY_SETTER
    )
    @Retention(AnnotationRetention.BINARY)
    annotation class ReturnsRetained

    @Target(
            AnnotationTarget.FUNCTION,
            AnnotationTarget.PROPERTY_GETTER,
            AnnotationTarget.PROPERTY_SETTER
    )
    @Retention(AnnotationRetention.BINARY)
    annotation class ConsumesReceiver

    @Target(AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.BINARY)
    annotation class Consumed

    @Target(AnnotationTarget.CONSTRUCTOR)
    @Retention(AnnotationRetention.BINARY)
    annotation class CppClassConstructor
}

/**
 * Collection of annotations that allow to store
 * constant konstues.
 */
@InternalForKotlinNative
public object ConstantValue {
    @Retention(AnnotationRetention.BINARY)
    annotation class Byte(konst konstue: kotlin.Byte)
    @Retention(AnnotationRetention.BINARY)
    annotation class Short(konst konstue: kotlin.Short)
    @Retention(AnnotationRetention.BINARY)
    annotation class Int(konst konstue: kotlin.Int)
    @Retention(AnnotationRetention.BINARY)
    annotation class Long(konst konstue: kotlin.Long)
    @Retention(AnnotationRetention.BINARY)
    annotation class UByte(konst konstue: kotlin.UByte)
    @Retention(AnnotationRetention.BINARY)
    annotation class UShort(konst konstue: kotlin.UShort)
    @Retention(AnnotationRetention.BINARY)
    annotation class UInt(konst konstue: kotlin.UInt)
    @Retention(AnnotationRetention.BINARY)
    annotation class ULong(konst konstue: kotlin.ULong)
    @Retention(AnnotationRetention.BINARY)
    annotation class Float(konst konstue: kotlin.Float)
    @Retention(AnnotationRetention.BINARY)
    annotation class Double(konst konstue: kotlin.Double)
    @Retention(AnnotationRetention.BINARY)
    annotation class String(konst konstue: kotlin.String)
}

/**
 * Denotes property that is an alias to some enum entry.
 */
@Target(AnnotationTarget.CLASS)
@InternalForKotlinNative
@Retention(AnnotationRetention.BINARY)
public annotation class CEnumEntryAlias(konst entryName: String)

/**
 * Stores instance size of the type T: CEnumVar.
 */
@Target(AnnotationTarget.CLASS)
@InternalForKotlinNative
@Retention(AnnotationRetention.BINARY)
public annotation class CEnumVarTypeSize(konst size: Int)
