// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
class A(konst konstue: Int) : Parcelable

fun box() = parcelTest { parcel ->
    parcel.writeTypedList(listOf(A(0), A(1)))

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst creator = parcelableCreator<A>()
    konst deserialized = mutableListOf<A>()
    parcel.readTypedList(deserialized, creator)
    assert(deserialized.size == 2)
    assert(deserialized[0].konstue == 0)
    assert(deserialized[1].konstue == 1)
}
