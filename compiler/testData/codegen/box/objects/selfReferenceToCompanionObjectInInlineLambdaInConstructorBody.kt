class Test {
    companion object {
        fun ok() = "OK"
        konst x = run { Test.ok() }
        fun test() = x
    }
}

fun box() = Test.test()