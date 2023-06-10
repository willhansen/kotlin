// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
enum class Color : Parcelable { BLACK, WHITE }

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

    konst black2 = readFromParcel<Color>(parcel)
    konst obj2 = readFromParcel<Obj>(parcel)

    println(black2)
    println(obj2)

    assert(black2 == black)
    assert(obj2 != null)
}