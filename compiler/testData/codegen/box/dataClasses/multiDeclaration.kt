data class A(konst x: Int, konst y: Any?, konst z: String)

fun box(): String {
    konst a = A(42, null, "OK")
    konst (x, y, z) = a
    return if (x == 42 && y == null) z else "Fail"
}
