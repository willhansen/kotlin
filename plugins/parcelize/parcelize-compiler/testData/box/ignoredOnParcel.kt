// WITH_STDLIB
// IGNORE_BACKEND: JVM

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

class T(konst konstue: Int)

@Parcelize
class A(
    konst x: String,
    @IgnoredOnParcel
    konst i1: String = "default",
    @IgnoredOnParcel
    konst i2: T = T(10),
    konst d: String = "default",
) : Parcelable {
    @IgnoredOnParcel
    konst a: String = x
}

@Parcelize
object B : Parcelable {
    @IgnoredOnParcel
    konst x: T = T(2)
}

fun box() = parcelTest { parcel ->
    konst a1 = A("X", "i1", T(0), "d")

    a1.writeToParcel(parcel, 0)
    B.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst a2 = parcelableCreator<A>().createFromParcel(parcel)
    konst b = parcelableCreator<B>().createFromParcel(parcel)

    assert(a1.x == a2.x)
    assert(a2.i1 == "default")
    assert(a2.i2.konstue == 10)
    assert(a1.d == a2.d)
    assert(a1.a == a2.a)
    assert(b == B)
}
