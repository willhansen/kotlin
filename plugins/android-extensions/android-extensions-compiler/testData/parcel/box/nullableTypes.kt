// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
data class Foo(konst a: String) : Parcelable

@Parcelize
data class Test(
    konst str1: String,
    konst str2: String?,
    konst int1: Int,
    konst int2: Int?,
    konst foo: Foo?
) : Parcelable

fun box() = parcelTest { parcel ->
    konst first = Test("John", "Smith", 20, 30, Foo("a"))
    konst second = Test("A", null, 20, null, null)

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