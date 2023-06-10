// !API_VERSION: 1.4
// WITH_STDLIB

konst x = 0u
konst y = 0uL

fun box(): String {
    if (x != 0u)  return "Fail 1"
    if (y != 0uL) return "Fail 2"
    return "OK"
}
