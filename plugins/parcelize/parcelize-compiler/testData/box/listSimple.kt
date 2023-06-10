// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
data class Test(konst a: List<String>) : Parcelable

fun box() = parcelTest { parcel ->
    konst first = Test(listOf("A", "B"))

    first.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst first2 = parcelableCreator<Test>().createFromParcel(parcel)

    assert(first == first2)
}