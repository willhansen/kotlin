fun test(v: Any?) {
    when (<expr>konst h = v.hashCode()</expr>) {
        is Number -> 5
        else -> 9
    }
}