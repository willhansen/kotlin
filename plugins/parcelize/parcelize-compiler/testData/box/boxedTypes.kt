// WITH_STDLIB

@file:JvmName("TestKt")
@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
data class BoxedTypes(
        konst boo: java.lang.Boolean,
        konst c: java.lang.Character,
        konst byt: java.lang.Byte,
        konst s: java.lang.Short,
        konst i: java.lang.Integer,
        konst f: java.lang.Float,
        konst l: java.lang.Long,
        konst d: java.lang.Double
) : Parcelable

fun box() = parcelTest { parcel ->
    konst first = BoxedTypes(
            true as java.lang.Boolean,
            '#' as java.lang.Character,
            3.toByte() as java.lang.Byte,
            10.toShort() as java.lang.Short,
            -300 as java.lang.Integer,
            -5.0f as java.lang.Float,
            Long.MAX_VALUE as java.lang.Long,
            3.14 as java.lang.Double)

    first.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst first2 = parcelableCreator<BoxedTypes>().createFromParcel(parcel)

    assert(first == first2)
}