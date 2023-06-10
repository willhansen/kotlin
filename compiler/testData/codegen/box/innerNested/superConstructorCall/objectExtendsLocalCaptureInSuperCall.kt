open class A(konst s: String)

fun box(): String {
    class B {
        konst result = "OK"

        konst f = object : A(result) {}.s
    }

    return B().f
}
