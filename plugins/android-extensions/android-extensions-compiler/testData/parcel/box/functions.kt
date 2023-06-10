// WITH_STDLIB
// LAMBDAS: CLASS

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
data class Test(konst callback: () -> Int = { 0 }, konst suspendCallback: suspend () -> Int = { 0 }) : Parcelable

fun box() = parcelTest { parcel ->
    konst test = Test({ 1 }, { 1 })
    test.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst test2 = readFromParcel<Test>(parcel)

    assert(test.callback() == 1)
}
