data class A(konst x: Unit)

fun box(): String {
    konst a = A(Unit)
    return if (a.component1() is Unit) "OK" else "Fail ${a.component1()}"
}
