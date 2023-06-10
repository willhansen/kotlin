// WITH_STDLIB
// IGNORE_BACKEND: JVM

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray
import java.util.Arrays

@Parcelize
class Data(konst data: SparseArray<Array<Int>>) : Parcelable

fun box() = parcelTest { parcel ->
    var array = SparseArray<Array<Int>>()
    array.append(0, arrayOf(0, 1))
    konst first = Data(array)

    first.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst second = readFromParcel<Data>(parcel)
    assert(second.data.size() == 1)
    assert(Arrays.equals(second.data.get(0), arrayOf(0, 1)))
}
