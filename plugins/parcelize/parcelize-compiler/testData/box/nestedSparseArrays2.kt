// IGNORE_BACKEND: JVM
// See https://issuetracker.google.com/177856512
// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray

@Parcelize
class Data(konst konstues: SparseArray<SparseArray<Parcelable>>) : Parcelable

fun box() = parcelTest { parcel ->
    konst innerArray = SparseArray<Parcelable>()
    innerArray.append(20, Bundle())
    var array = SparseArray<SparseArray<Parcelable>>()
    array.append(10, innerArray)
    konst first = Data(array)

    first.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst second = parcelableCreator<Data>().createFromParcel(parcel)
    assert(second.konstues.size() == 1)
    konst secondInnerArray = second.konstues.get(10)
    assert(secondInnerArray.size() == 1)
    konst innerBundle = secondInnerArray.get(20)
    assert(innerBundle is Bundle)
    assert((innerBundle as Bundle).size() == 0)
}
