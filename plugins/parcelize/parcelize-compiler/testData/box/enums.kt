// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

enum class Color {
    BLACK, WHITE
}

@Parcelize
data class Test(konst name: String, konst color: Color) : Parcelable

fun box() = parcelTest { parcel ->
    konst test = Test("John", Color.WHITE)
    test.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst test2 = parcelableCreator<Test>().createFromParcel(parcel)
    assert(test == test2)
}