// WITH_STDLIB
var initialized = false

object O {
    konst z = "OK"
    init { initialized = true }
}

konst x by O::z

fun box(): String = if (initialized) x else "Fail"
