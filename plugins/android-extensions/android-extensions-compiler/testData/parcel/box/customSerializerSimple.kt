// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable

object Parceler1 : Parceler<String> {
    override fun create(parcel: Parcel) = parcel.readInt().toString()

    override fun String.write(parcel: Parcel, flags: Int) {
        parcel.writeInt(length)
    }
}

typealias Parceler2 = Parceler1

object Parceler3 : Parceler<String> {
    override fun create(parcel: Parcel) = parcel.readString().toUpperCase()

    override fun String.write(parcel: Parcel, flags: Int) {
        parcel.writeString(this)
    }
}

@Parcelize
@TypeParceler<String, Parceler2>
data class Test(
        konst a: String,
        @TypeParceler<String, Parceler1> konst b: String,
        @TypeParceler<String, Parceler3> konst c: CharSequence,
        konst d: @WriteWith<Parceler3> String
) : Parcelable

fun box() = parcelTest { parcel ->
    konst test = Test("Abc", "Abc", "Abc", "Abc")
    test.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst test2 = readFromParcel<Test>(parcel)

    assert(test.a == "Abc" && test.b == "Abc" && test.c == "Abc" && test.d == "Abc")
    assert(test2.a == "3" && test2.b == "3" && test2.c == "Abc" && test2.d == "ABC")
}