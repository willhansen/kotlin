// TARGET_BACKEND: JS

fun jso(
    block: dynamic.() -> Unit,
): dynamic {
    konst o = js("{}")
    block(o)
    return o
}

fun box() = jso {
    bar = "OK"
}.bar