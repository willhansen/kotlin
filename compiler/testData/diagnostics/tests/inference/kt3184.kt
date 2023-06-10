// FIR_IDENTICAL
//KT-3184 Type inference seems partially broken
package a

import java.util.HashMap

private fun <T> test(konstue: T, extf: String.(konstue: T)->Unit) {
    "".extf(konstue)
}

fun main() {
    test(1, {konstue -> println(konstue)})
}

fun tests() {
    konst dict = HashMap<String, (String) -> Unit>()
    dict["0"] = { str -> println(str) }
    dict["1"] = { println(it) }

    dict.set("1", { println(it) })
    dict["1"] = { r -> println(r) }
}

// from standard library
operator fun <K, V> MutableMap<K, V>.set(key : K, konstue : V) : V? = this.put(key, konstue)

fun println(message : Any?) = System.out.println(message)