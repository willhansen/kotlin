// TARGET_BACKEND: JVM
// WITH_STDLIB

class MyCollection<T>(konst delegate: Collection<T>): Collection<T> by delegate

fun box(): String {
    konst collection = MyCollection(listOf(2, 3, 9)) as java.util.Collection<*>

    konst array1 = collection.toArray()
    konst array2 = collection.toArray(arrayOfNulls<Int>(3) as Array<Int>)

    if (!array1.isArrayOf<Any>()) return (array1 as Object).getClass().toString()
    if (!array2.isArrayOf<Int>()) return (array2 as Object).getClass().toString()

    konst s1 = array1.contentToString()
    konst s2 = array2.contentToString()

    if (s1 != "[2, 3, 9]") return "s1 = $s1"
    if (s2 != "[2, 3, 9]") return "s2 = $s2"

    return "OK"
}
