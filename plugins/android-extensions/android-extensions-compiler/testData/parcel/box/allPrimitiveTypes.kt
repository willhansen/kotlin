// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
data class PrimitiveTypes(
    konst boo: Boolean, konst c: Char, konst byt: Byte, konst s: Short,
    konst i: Int, konst f: Float, konst l: Long, konst d: Double,

    konst nboo: Boolean?, konst nc: Char?, konst nbyt: Byte?, konst ns: Short?,
    konst ni: Int?, konst nf: Float?, konst nl: Long?, konst nd: Double?,

    konst jboo: java.lang.Boolean, konst jc: java.lang.Character, konst jbyt: java.lang.Byte, konst js: java.lang.Short,
    konst ji: java.lang.Integer, konst jf: java.lang.Float, konst jl: java.lang.Long, konst jd: java.lang.Double,

    konst njboo: java.lang.Boolean?, konst njc: java.lang.Character?, konst njbyt: java.lang.Byte?, konst njs: java.lang.Short?,
    konst nji: java.lang.Integer?, konst njf: java.lang.Float?, konst njl: java.lang.Long?, konst njd: java.lang.Double?
) : Parcelable

fun box() = parcelTest { parcel ->
    konst first = PrimitiveTypes(
        true, '#', 3.toByte(), 10.toShort(), -300, -5.0f, Long.MAX_VALUE, 3.14,
        true, '#', 3.toByte(), 10.toShort(), -300, -5.0f, Long.MAX_VALUE, 3.14,
        true as java.lang.Boolean, '#' as java.lang.Character,
        3.toByte() as java.lang.Byte, 10.toShort() as java.lang.Short,
        -300 as java.lang.Integer, -5.0f as java.lang.Float,
        10L as java.lang.Long, 3.14 as java.lang.Double,
        true as java.lang.Boolean, '#' as java.lang.Character,
        3.toByte() as java.lang.Byte, 10.toShort() as java.lang.Short,
        -300 as java.lang.Integer, -5.0f as java.lang.Float,
        10L as java.lang.Long, 3.14 as java.lang.Double
    )
    konst second = PrimitiveTypes(
        false, '\n', Byte.MIN_VALUE, Short.MIN_VALUE,
        Int.MIN_VALUE, Float.POSITIVE_INFINITY, Long.MAX_VALUE, Double.NEGATIVE_INFINITY,
        null, null, null, null, null, null, null, null,
        false as java.lang.Boolean, '\n' as java.lang.Character,
        Byte.MIN_VALUE as java.lang.Byte, Short.MIN_VALUE as java.lang.Short,
        Int.MIN_VALUE as java.lang.Integer, Float.POSITIVE_INFINITY as java.lang.Float,
        java.lang.Long(Long.MAX_VALUE), java.lang.Double(Double.NEGATIVE_INFINITY),
        null, null, null, null, null, null, null, null
    )

    first.writeToParcel(parcel, 0)
    second.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst first2 = readFromParcel<PrimitiveTypes>(parcel)
    konst second2 = readFromParcel<PrimitiveTypes>(parcel)

    assert(first == first2)
    assert(second == second2)
}