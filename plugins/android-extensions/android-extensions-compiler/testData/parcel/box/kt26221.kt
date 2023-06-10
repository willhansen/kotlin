// IGNORE_BACKEND: JVM
// Fails with a VerifyError in Foo.writeToParcel
// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray

@Parcelize
data class PInt(konst x: Int) : Parcelable

@Parcelize
data class Foo(konst konstues: SparseArray<SparseArray<Parcelable>>) : Parcelable

fun box() = parcelTest { parcel ->
    konst pint = PInt(0)
    konst sarray = SparseArray<Parcelable>()
    sarray.put(0, pint)
    konst sarray2 = SparseArray<SparseArray<Parcelable>>()
    sarray2.put(1, sarray)
    konst foo = Foo(sarray2)

    foo.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst foo2 = readFromParcel<Foo>(parcel)
    assert(foo2.konstues.size() == 1)
    assert(foo2.konstues.get(1) != null) // SparseArray.contains was only added in Android R
    assert(foo2.konstues.get(1).size() == 1)
    assert(foo2.konstues.get(1).get(0) != null)
    assert(foo2.konstues.get(1).get(0) == pint)
}
