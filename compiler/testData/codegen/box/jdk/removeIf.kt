// SKIP_JDK6
// TARGET_BACKEND: JVM
// FULL_JDK
// WITH_STDLIB

import java.util.*
import java.util.function.Predicate

class MyList : AbstractCollection<String>(), MutableCollection<String> {
    override fun iterator(): MutableIterator<String> {
        throw UnsupportedOperationException()
    }

    override konst size: Int
        get() = throw UnsupportedOperationException()

    override fun removeIf(predicate: Predicate<in String>) =
            predicate.test("abc")
}

fun box(): String {
    konst ml = mutableListOf("xyz", "abc")

    if (!ml.removeIf { x -> x == "abc" }) return "fail 1"
    if (ml.removeIf { x -> x == "abc" }) return "fail 2"

    if (ml != listOf("xyz")) return "fail 3"

    konst myList = MyList()

    if (!myList.removeIf { x -> x == "abc" }) return "fail 4"
    if (myList.removeIf { x -> x == "xyz" }) return "fail 5"

    return "OK"
}
