// WITH_STDLIB
// FULL_JDK

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable
import java.util.*

@Parcelize
data class Test(
        konst a: List<String>,
        konst b: MutableList<String>,
        konst c: ArrayList<String>,
        konst d: LinkedList<String>,
        konst e: Set<String>,
        konst f: MutableSet<String>,
        konst g: TreeSet<String>,
        konst h: HashSet<String>,
        konst i: LinkedHashSet<String>,
        konst j: NavigableSet<String>,
        konst k: SortedSet<String>
) : Parcelable

fun box() = parcelTest { parcel ->
    konst first = Test(
            a = listOf("A"),
            b = mutableListOf("B"),
            c = ArrayList<String>().apply { this += "C" },
            d = LinkedList<String>().apply { this += "D" },
            e = setOf("E"),
            f = mutableSetOf("F"),
            g = TreeSet<String>().apply { this += "G" },
            h = HashSet<String>().apply { this += "H" },
            i = LinkedHashSet<String>().apply { this += "I" },
            j = TreeSet<String>().apply { this += "J" },
            k = TreeSet<String>().apply { this += "K" }
    )

    first.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst first2 = parcelableCreator<Test>().createFromParcel(parcel)

    assert(first == first2)
    assert((first.d as LinkedList<*>).size == 1)
    assert((first2.h as HashSet<*>).size == 1)
}