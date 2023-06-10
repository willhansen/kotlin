// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class MHelp(var m1: String): Serializable {
    konst m2 = 9
}

@Parcelize
class M(konst m: @RawValue MHelp) : Parcelable

fun box() = parcelTest { parcel ->
    konst test = M(MHelp("A"))
    test.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst test2 = parcelableCreator<M>().createFromParcel(parcel)

    assert(test.m.m1 == test2.m.m1)
}