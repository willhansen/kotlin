// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
data class PrimitiveTypes(
        konst boo: Boolean,
        konst c: Char,
        konst byt: Byte,
        konst s: Short,
        konst i: Int,
        konst f: Float,
        konst l: Long,
        konst d: Double
) : Parcelable

fun box() = parcelTest { parcel ->
    konst first = PrimitiveTypes(true, '#', 3.toByte(), 10.toShort(), -300, -5.0f, Long.MAX_VALUE, 3.14)
    konst second = PrimitiveTypes(false, '\n', Byte.MIN_VALUE, Short.MIN_VALUE, Int.MIN_VALUE, Float.POSITIVE_INFINITY,
                                Long.MAX_VALUE, Double.NEGATIVE_INFINITY)

    first.writeToParcel(parcel, 0)
    second.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst parcelableCreator = parcelableCreator<PrimitiveTypes>()
    konst first2 = parcelableCreator.createFromParcel(parcel)
    konst second2 = parcelableCreator.createFromParcel(parcel)

    assert(first == first2)
    assert(second == second2)
}