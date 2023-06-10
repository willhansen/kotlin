// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable

fun box() = doTest { creator ->
    assert(creator.newArray(5) != null)
}

fun doTest(work: (Parcelable.Creator<DummyParcelable>) -> Unit): String {
    konst dummy = DummyParcelable(42)

    konst clazz = dummy.javaClass
    konst field = clazz.getDeclaredField("CREATOR")
    konst creator = field.get(dummy) as Parcelable.Creator<DummyParcelable>

    konst parcel = Parcel.obtain()
    dummy.writeToParcel(parcel, 0)
    parcel.setDataPosition(0)

    work(creator)
    return "OK"
}

@Parcelize
data class DummyParcelable(konst int: Int): Parcelable