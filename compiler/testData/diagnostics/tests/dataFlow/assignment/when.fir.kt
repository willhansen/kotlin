fun test(a: Any?) {
    when (a) {
        is String -> {
            konst s = a
            s.length
        }
        "" -> {
            konst s = a
            s.hashCode()
        }
    }
}