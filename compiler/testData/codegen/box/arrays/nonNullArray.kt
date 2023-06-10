class A() {
    class B(konst i: Int) {
    }

    fun test() = Array<B> (10, { B(it) })
}

fun box() = if(A().test()[5].i == 5) "OK" else "fail"
