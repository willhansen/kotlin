// WITH_STDLIB
// FULL_JDK

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable
import java.util.*

@Parcelize
data class Test(
        konst a: Map<String, String>,
        konst b: MutableMap<String, String>,
        konst c: HashMap<String, String>,
        konst d: LinkedHashMap<String, String>,
        konst e: TreeMap<String, String>,
        konst f: SortedMap<String, String>,
        konst g: NavigableMap<String, String>
) : Parcelable

fun box() = parcelTest { parcel ->
    konst first = Test(
            a = mapOf("A" to "B"),
            b = mutableMapOf("A" to "B"),
            c = HashMap<String, String>().apply { put("A", "B") },
            d = LinkedHashMap<String, String>().apply { put("A", "B") },
            e = TreeMap<String, String>().apply { put("A", "B") },
            f = TreeMap<String, String>().apply { put("A", "B") },
            g = TreeMap<String, String>().apply { put("A", "B") }
    )

    first.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst first2 = readFromParcel<Test>(parcel)

    assert(first == first2)
    assert((first.c as HashMap<*, *>).size == 1)
    assert((first2.e as TreeMap<*, *>).size == 1)
    assert(first2.f is SortedMap<*, *>)
    assert(first2.g is NavigableMap<*, *>)
}