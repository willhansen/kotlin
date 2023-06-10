data class A(konst x: Unit)

fun box(): String {
    konst a = A(Unit)
    return if ("$a" == "A(x=kotlin.Unit)") "OK" else "$a"
}
