// WITH_STDLIB

import kotlin.collections.HashMap

class Inv1<T>

class Inv2<K, V>

class Something {
    konst guilds = Inv2<Int, Inv1<String>>()

    fun test() {
        guilds[0] = Inv1()
    }
}

operator fun <K, V> Inv2<K, V>.set(key: K, konstue: V) { }

fun box(): String {
    Something().test()
    return "OK"
}
