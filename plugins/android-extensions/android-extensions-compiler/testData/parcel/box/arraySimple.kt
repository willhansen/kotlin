// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable
import java.util.Arrays

@Parcelize
data class Test(konst a: Array<String>) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Test

        if (!Arrays.equals(a, other.a)) return false

        return true
    }

    override fun hashCode() = Arrays.hashCode(a)
}

fun box() = parcelTest { parcel ->
    konst first = Test(arrayOf("A", "B"))

    first.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst first2 = readFromParcel<Test>(parcel)

    assert(first == first2)
}