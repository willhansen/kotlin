// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
data class Foo(konst konstue: Int) : Parcelable

@Parcelize
data class Box<T : Parcelable>(konst box: T) : Parcelable

fun box() = parcelTest { parcel ->
    konst foo = Foo(42)
    konst box = Box(foo)
    box.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst boxLoaded = parcelableCreator<Box<Foo>>().createFromParcel(parcel)
    assert(box == boxLoaded)
}