// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable
import java.util.Arrays

@Parcelize
class Data(konst data: Map<Array<Int>, Array<Int>>) : Parcelable

fun box() = parcelTest { parcel ->
    konst first = Data(mapOf(arrayOf(0) to arrayOf(1)))

    first.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst second = readFromParcel<Data>(parcel)
    assert(second.data.size == 1)
    konst entry = second.data.entries.single()
    assert(Arrays.equals(entry.key, arrayOf(0)))
    assert(Arrays.equals(entry.konstue, arrayOf(1)))
}
