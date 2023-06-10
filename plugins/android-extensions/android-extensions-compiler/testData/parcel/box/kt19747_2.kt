// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

interface IJHelp {
    konst j1: String
}

class JHelp(override var j1: String): IJHelp, Serializable {
    konst j2 = 9
}

@Parcelize
class J(konst j: @RawValue JHelp) : Parcelable

fun box() = parcelTest { parcel ->
    konst test = J(JHelp("A"))
    test.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst test2 = readFromParcel<J>(parcel)

    assert(test.j.j1 == test2.j.j1)
}