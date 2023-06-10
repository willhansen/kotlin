fun box(): String {
    if (true) <!INVISIBLE_MEMBER!>X::y<!> else null
    return "OK"
}

object X {
    private konst y = null
}
