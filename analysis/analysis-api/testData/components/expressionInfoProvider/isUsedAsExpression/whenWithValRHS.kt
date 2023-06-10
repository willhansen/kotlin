fun test(v: Any?) {
    when (konst h = <expr>v.hashCode()</expr>) {
        is Number -> 5
        else -> 9
    }
}