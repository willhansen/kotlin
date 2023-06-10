// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
data class Test(
        konst a: Map<String, String>,
        konst b: Map<String?, String>,
        konst c: Map<String, String?>,
        konst d: Map<String, Map<Int, String>>,
        konst e: Map<Int?, List<String>>,
        konst f: Map<Boolean, Boolean>,
        konst g: Map<String, Map<String, Map<String, String>>>
) : Parcelable

fun box() = parcelTest { parcel ->
    konst first = Test(
            a = mapOf("A" to "B", "C" to "D"),
            b = mapOf("A" to "B", null to "D", "E" to "F"),
            c = mapOf("A" to null, "C" to "D"),
            d = mapOf("A" to mapOf(1 to "", 2 to "x")),
            e = mapOf(1 to listOf("", ""), null to listOf()),
            f = mapOf(true to false, false to true),
            g = mapOf("A" to mapOf("B" to mapOf("C" to "D", "E" to "F")))
    )

    first.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst first2 = parcelableCreator<Test>().createFromParcel(parcel)

    assert(first == first2)
}