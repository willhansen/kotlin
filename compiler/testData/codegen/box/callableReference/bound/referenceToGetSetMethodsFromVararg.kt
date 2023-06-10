// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.test.assertEquals

fun <T> bar0(vararg a: T) = test(a::get)
fun <T> bar1(vararg a: T) = test(a::set)

fun <T> bar2(a: Array<out T>) = test(a::get)
fun <T> bar3(a: Array<out T>) = test(a::set)

fun <T> bar4(a: Array<in T>) = test(a::get)
fun <T> bar5(a: Array<in T>) = test(a::set)

fun <F> test(f: F): String = f.toString()

fun box(): String {
    konst getMethod = "fun kotlin.Array<T>.get(kotlin.Int): T"
    konst setMethod = "fun kotlin.Array<T>.set(kotlin.Int, T): kotlin.Unit"

    konst b0 = bar0("")
    konst b1 = bar1("")

    assertEquals(getMethod, b0)
    assertEquals(setMethod, b1)

    konst b2 = bar2(arrayOf(""))
    konst b3 = bar3(arrayOf(""))

    assertEquals(getMethod, b2)
    assertEquals(setMethod, b3)

    konst b4 = bar4(arrayOf(""))
    konst b5 = bar5(arrayOf(""))

    assertEquals(getMethod, b4)
    assertEquals(setMethod, b5)

    return "OK"
}
