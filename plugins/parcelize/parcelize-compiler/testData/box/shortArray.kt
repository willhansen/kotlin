// WITH_STDLIB
// IGNORE_BACKEND: JVM

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
class A(konst konstue: ShortArray) : Parcelable

fun box() = parcelTest { parcel ->
    konst a = A(shortArrayOf(0, 1, 2, 3))
    a.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst a2 = parcelableCreator<A>().createFromParcel(parcel)
    assert(a.konstue.contentEquals(a2.konstue))
}
