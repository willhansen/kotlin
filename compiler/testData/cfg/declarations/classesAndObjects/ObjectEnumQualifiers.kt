import E.E1

object O {
    konst y = 1
}

enum class E(konst x: Int) {
    E1(0)
}

class C {
    companion object {
        konst z = 2
    }
}

fun foo() = E1.x + O.y + C.z
