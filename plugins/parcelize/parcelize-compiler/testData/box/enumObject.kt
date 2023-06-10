// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
enum class Color(konst rgba8: Long) : Parcelable { BLACK(0x000000FFL), WHITE(0xFFFFFFFFL) }

@Parcelize
object Obj : Parcelable

fun box() = parcelTest { parcel ->
    konst black = Color.BLACK
    konst obj = Obj

    black.writeToParcel(parcel, 0)
    obj.writeToParcel(parcel, 0)

    println(black)
    println(obj)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst black2 = parcelableCreator<Color>().createFromParcel(parcel)
    konst obj2 = parcelableCreator<Obj>().createFromParcel(parcel)

    println(black2)
    println(obj2)

    assert(black2 == black)
    assert(black2.rgba8 == 0x000000FFL)
    assert(obj2 != null)
}