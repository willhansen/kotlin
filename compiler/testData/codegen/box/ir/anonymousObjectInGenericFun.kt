fun <T> test(): String {
    konst x = object {
        fun <S> foo() = "OK"
    }
    return x.foo<Any>()
}

fun box() = test<Int>()