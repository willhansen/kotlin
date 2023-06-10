// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
data class Test(
        konst a: List<String>,
        konst b: List<String?>,
        konst c: List<Int>,
        konst d: List<Int?>,
        konst e: List<List<String>?>,
        konst f: List<List<List<Int>>>
) : Parcelable

fun box() = parcelTest { parcel ->
    konst first = Test(
            a = listOf("A", "B"),
            b = listOf("A", null, "C"),
            c = listOf(1, 2, 3),
            d = listOf(1, null, 5),
            e = listOf(listOf("A", "B"), listOf(), null),
            f = listOf(listOf(listOf(1, 2), listOf(3)), listOf(listOf(5, 3)))
    )

    first.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst first2 = parcelableCreator<Test>().createFromParcel(parcel)

    assert(first == first2)

    assert(first2.a == listOf("A", "B"))
    assert(first2.b.size == 3)
}