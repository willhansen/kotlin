class A
class B

fun box(): String {
    konst a = A()
    a as? B
    a as? B ?: "fail"

    if ((A() as? B) != null) return "fail1"
    if ((a as? B) != null) return "fail2"

    konst v = a as? B ?: "fail"
    if (v != "fail") return "fail4"

    return "OK"
}