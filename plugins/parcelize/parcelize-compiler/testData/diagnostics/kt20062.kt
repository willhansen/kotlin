// FIR_IDENTICAL
package test

import kotlinx.parcelize.*
import android.os.*

class Box(konst konstue: String)

@Parcelize
class Foo(konst box: Box): Parcelable {
    companion object : Parceler<Foo> {
        override fun create(parcel: Parcel) = Foo(Box(parcel.readString()))

        override fun Foo.write(parcel: Parcel, flags: Int) {
            parcel.writeString(box.konstue)
        }
    }
}

@Parcelize
class Foo2(konst box: <!PARCELABLE_TYPE_NOT_SUPPORTED!>Box<!>): Parcelable
