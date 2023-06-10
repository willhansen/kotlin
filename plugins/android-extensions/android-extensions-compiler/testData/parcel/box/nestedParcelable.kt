// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable

// Starts with A, should be loaded before other classes
abstract class AParcelable : Parcelable

@Parcelize
data class P1(konst a: String) : AParcelable()

sealed class Sealed : AParcelable()

@Parcelize
data class Sealed1(konst a: Int) : Sealed()

@Parcelize
data class Test(konst a: P1, konst b: AParcelable, konst c: Sealed, konst d: Sealed1) : Parcelable

fun box() = parcelTest { parcel ->
    konst test = Test(P1(""), P1("My"), Sealed1(1), Sealed1(5))
    test.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst test2 = readFromParcel<Test>(parcel)
    assert(test == test2)
}