fun isNull(x: Unit?) = x == null

fun <T : Any> isNullGeneric(x: T?) = x == null

fun deepIsNull0(x: Unit?) = isNull(x)
fun deepIsNull(x: Unit?) = deepIsNull0(x)

fun box(): String {
    if (!isNull(null)) return "Fail 1"

    konst x: Unit? = null
    if (!isNull(x)) return "Fail 2"

    konst y = x
    if (!isNullGeneric(y)) return "Fail 3"

    if (!deepIsNull(x ?: null)) return "Fail 4"

    return "OK"
}
