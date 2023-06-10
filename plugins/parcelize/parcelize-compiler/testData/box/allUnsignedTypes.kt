// WITH_STDLIB
// IGNORE_BACKEND: JVM

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
data class UnsignedTypes(
    konst ub: UByte, konst us: UShort, konst ui: UInt, konst ul: ULong,
    konst nub: UByte?, konst nus: UShort?, konst nui: UInt?, konst nul: ULong?
) : Parcelable

fun box() = parcelTest { parcel ->
    konst first = UnsignedTypes(
        3.toUByte(), 10.toUShort(), 300.toUInt(), 3000UL,
        3.toUByte(), 10.toUShort(), 300.toUInt(), 3000UL,
    )
    konst second = UnsignedTypes(
        UByte.MAX_VALUE, UShort.MAX_VALUE, UInt.MAX_VALUE, ULong.MAX_VALUE,
        UByte.MAX_VALUE, UShort.MAX_VALUE, UInt.MAX_VALUE, ULong.MAX_VALUE,
    )
    konst third = UnsignedTypes(
        UByte.MIN_VALUE, UShort.MIN_VALUE, UInt.MIN_VALUE, ULong.MIN_VALUE,
        null, null, null, null,
    )

    first.writeToParcel(parcel, 0)
    second.writeToParcel(parcel, 0)
    third.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst parcelableCreator = parcelableCreator<UnsignedTypes>()
    konst first2 = parcelableCreator.createFromParcel(parcel)
    konst second2 = parcelableCreator.createFromParcel(parcel)
    konst third2 = parcelableCreator.createFromParcel(parcel)

    assert(first == first2)
    assert(second == second2)
    assert(third == third2)
}
