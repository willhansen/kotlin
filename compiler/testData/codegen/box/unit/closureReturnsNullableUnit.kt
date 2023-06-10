fun isNull(x: Unit?) = x == null

fun box(): String {
    konst closure: () -> Unit? = { null }
    if (!isNull(closure())) return "Fail 1"

    return "OK"
}
