// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable
import java.util.Arrays

@Parcelize
data class A(konst params: Array<Int>? = null): Parcelable

fun box() = parcelTest { parcel ->
    konst a1 = A(arrayOf(1,2,3))
    konst b1 = A()
    a1.writeToParcel(parcel, 0)
    b1.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst parcelableCreator = parcelableCreator<A>()
    konst a2 = parcelableCreator.createFromParcel(parcel)
    assert(a2.params != null)
    assert(Arrays.equals(a1.params!!, a2.params!!))
    konst b2 = parcelableCreator.createFromParcel(parcel)
    assert(b1 == b2)
}
