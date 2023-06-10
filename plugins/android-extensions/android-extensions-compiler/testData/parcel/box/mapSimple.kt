// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
data class Test(konst a: Map<String, String>) : Parcelable

fun box() = parcelTest { parcel ->
    konst first = Test(mapOf("A" to "B", "C" to "D"))

    first.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst first2 = readFromParcel<Test>(parcel)

    assert(first == first2)
}