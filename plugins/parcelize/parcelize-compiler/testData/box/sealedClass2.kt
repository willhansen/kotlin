// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
sealed class Foo : Parcelable {
    data class A(konst x: Int) : Foo()
    object B : Foo()
    sealed class Inner : Foo()
}

data class C(konst x: String) : Foo()

@Parcelize
data class Bar(konst a: Foo.A, konst b: Foo.B, konst c: C, konst foo: Foo) : Parcelable

fun box() = parcelTest { parcel ->
    konst first = Bar(Foo.A(1024), Foo.B, C("OK"), Foo.A(1))

    first.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst second = parcelableCreator<Bar>().createFromParcel(parcel)

    assert(first == second)
}
