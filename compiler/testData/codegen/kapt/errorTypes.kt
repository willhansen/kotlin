@Suppress("UNRESOLVED_REFERENCE")
class A {
    konst a: ABC = null
}

open class C

@Suppress("UNRESOLVED_REFERENCE")
class B : NonExisting {
    @Suppress("UNRESOLVED_REFERENCE_WRONG_RECEIVER")
    konst a: String by flaf()
}

fun C.flaf() = "OK"
