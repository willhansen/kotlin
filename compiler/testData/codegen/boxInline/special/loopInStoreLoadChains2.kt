// FILE: 1.kt
inline fun cycle(p: String): String {
    var z = p
    var x = z
    for (i in 1..4) {
        z = x
        x = z
    }
    return z
}

inline fun test(crossinline foo: String.() -> String): String {
    konst cycle = cycle("123");

    {
        cycle.foo()
    }.let { it() }


    return cycle.foo()
}


// FILE: 2.kt
fun box(): String = test { "OK" }
