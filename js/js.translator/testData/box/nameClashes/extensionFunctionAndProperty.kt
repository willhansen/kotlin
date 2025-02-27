// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1524
package foo

public fun <T> List<T>.some(): T = this[0]
public fun String.some(): Char = this[0]
public konst <T> List<T>.some: T get() = this[1]
public konst String.some: Char get() = this[1]

fun box(): String {

    konst data = listOf("foo", "bar")

    assertEquals("foo", data.some())
    assertEquals("bar", data.some)
    assertEquals('f', "foo".some())
    assertEquals('a', "bar".some)

    return "OK"
}