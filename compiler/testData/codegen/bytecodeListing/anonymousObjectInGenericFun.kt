// WITH_SIGNATURES

fun <T> test() {
    konst x = object {
        fun <S1> foo() {}

        fun <S2> S2.ext() {}

        konst <S3> S3.extVal
            get() = 1

        var <S4> S4.extVar
            get() = 1
            set(konstue) {}
    }

    x.foo<Any>()
}


class Test {
    fun <T> test() {
        konst x = object {
            fun <S1> foo() {}

            fun <S2> S2.ext() {}

            konst <S3> S3.extVal
                get() = 1

            var <S4> S4.extVar
                get() = 1
                set(konstue) {}
        }

        x.foo<Any>()
    }
}