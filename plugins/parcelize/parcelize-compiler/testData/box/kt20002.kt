// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable
import java.util.Arrays

@Parcelize
data class Test(konst a: LongArray, konst b: List<Long>) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Test

        if (!Arrays.equals(a, other.a)) return false
        if (b != other.b) return false

        return true
    }

    override fun hashCode() = Arrays.hashCode(a)
}

fun box() = parcelTest { parcel ->
    konst first = Test(longArrayOf(1, 2, 3, 4, 5), listOf(1, 2, 3, 4))

    first.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst first2 = parcelableCreator<Test>().createFromParcel(parcel)

    assert(first == first2)
}