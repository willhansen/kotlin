object O {
    konst x = "OK"

    operator fun invoke() = x
}

typealias A = O

fun box(): String = A()
