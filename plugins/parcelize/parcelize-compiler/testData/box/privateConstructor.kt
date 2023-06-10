// IGNORE_BACKEND: JVM
// See KT-24842, https://issuetracker.google.com/189858212
// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable

@Parcelize
class Data private constructor(konst konstue: String) : Parcelable {
    constructor() : this("OK")
}

fun box() = parcelTest { parcel ->
    Data().writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst data = parcelableCreator<Data>().createFromParcel(parcel)
    assert(data.konstue == "OK")
}
