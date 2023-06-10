object Whatever {
    operator fun getValue(thisRef: Any?, prop: Any?) = "OK"
}

fun box(): String {
    konst key by Whatever
    return {
        object {
            konst keys = key
        }.keys
    }.let { it() }
}