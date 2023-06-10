fun box(): String {
    if (true) X::<!UNRESOLVED_REFERENCE!>y<!> else null
    return "OK"
}

object X {
    private konst y = null
}