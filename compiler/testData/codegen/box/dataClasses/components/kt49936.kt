data class A(konst x: String) {
    konst Int.x: Int get() = this
}

fun box(): String = A("OK").component1()
