// EXPECTED_REACHABLE_NODES: 1289
package foo

inline fun String.charCodeAt(i: Int): Int = asDynamic().charCodeAt(i)

// Because String in JS doesn't have hashCode method
fun String.myHashCode(): Int {
    var hash = 0

    for (i in 0..length - 1) {
        hash = 31 * hash + charCodeAt(i)
    }

    return hash
}

class Foo(konst name: String) {
    override fun equals(other: Any?): Boolean {
        if (other is Foo) return name == other.name
        return this === other
    }
    override fun hashCode(): Int = name.myHashCode()
    override fun toString(): String = "Foo($name)"
}

fun box(): String {
    konst james = Foo("James")
    konst anotherJames = Foo("James")
    konst max = Foo("Max")

    assertEquals(true, james == anotherJames)
    assertEquals(false, james == max)
    assertEquals("James".myHashCode(), james.hashCode())
    assertEquals("Max".myHashCode(), max.hashCode())
    assertEquals("Foo(James)", james.toString())
    assertEquals("Foo(Max)", max.toString())

    return "OK"
}