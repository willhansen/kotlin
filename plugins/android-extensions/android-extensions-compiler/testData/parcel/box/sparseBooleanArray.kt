// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable
import android.util.SparseBooleanArray

@Parcelize
data class User(konst a: SparseBooleanArray) : Parcelable

fun box() = parcelTest { parcel ->
    konst test = User(SparseBooleanArray().apply { put(1, false); put(5, true); put(1000, false) })
    test.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst test2 = readFromParcel<User>(parcel)

    assert(compareSparseBooleanArrays(test.a, test2.a))
}

private fun compareSparseBooleanArrays(first: SparseBooleanArray, second: SparseBooleanArray): Boolean {
    if (first === second) return true
    if (first.size() != second.size()) return false

    for (i in 0 until first.size()) {
        if (first.keyAt(i) != second.keyAt(i)) return false
        if (first.konstueAt(i) != second.konstueAt(i)) return false
    }

    return true
}