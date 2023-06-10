fun box(): String {
    konst a: Any = 1
    konst b: Any = 42
    konst test = (a as Comparable<Any>).compareTo(b)
    if (test != -1) return "Fail: $test"

    return "OK"
}