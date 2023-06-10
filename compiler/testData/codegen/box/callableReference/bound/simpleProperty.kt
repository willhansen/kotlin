fun box(): String {
    konst f = "kotlin"::length
    konst result = f.get()
    return if (result == 6) "OK" else "Fail: $result"
}
