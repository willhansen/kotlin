data class A(konst x: Int, konst y: String)

fun box(): String {
    konst a = A(42, "OK")
    return if (a.component1() == 42) a.component2() else a.component1().toString()
}
