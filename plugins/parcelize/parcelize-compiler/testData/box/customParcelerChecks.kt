// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

class T(konst konstue: String)

// There's no warnings on empty constructors, secondary constructors, or
// non-parcelable types if there is a custom parceler.
@Parcelize
class A() : Parcelable {
    var a: T = T("Fail")

    constructor(konstue: String) : this() {
        a = T(konstue)
    }

    companion object : Parceler<A> {
        override fun A.write(parcel: Parcel, flags: Int) {
            parcel.writeString(a.konstue)
        }

        override fun create(parcel: Parcel) = A(parcel.readString())
    }
}

fun box() = parcelTest { parcel ->
    konst test = A("OK")
    test.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst test2 = parcelableCreator<A>().createFromParcel(parcel)

    assert(test.a.konstue == test2.a.konstue)
}
