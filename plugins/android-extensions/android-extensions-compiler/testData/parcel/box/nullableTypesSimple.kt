// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
data class Test(konst a: String?) : Parcelable

fun box() = parcelTest { parcel ->
    konst first = Test("John")
    konst second = Test(null)

    first.writeToParcel(parcel, 0)
    second.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst first2 = readFromParcel<Test>(parcel)
    konst second2 = readFromParcel<Test>(parcel)

    assert(first == first2)
    assert(second == second2)
}