// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable

sealed class Foo : Parcelable {
    @Parcelize
    data class A(konst x: Int) : Foo()

    @Parcelize
    data class B (konst x: String) : Foo()
}

@Parcelize
data class Bar(konst a: Foo) : Parcelable

fun box() = parcelTest { parcel ->
    konst first = Bar(Foo.B("OK"))

    first.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst second = readFromParcel<Bar>(parcel)

    assert(first == second)
}
