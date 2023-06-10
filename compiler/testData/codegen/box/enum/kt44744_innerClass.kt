interface IFoo {
    fun foo(e: En): String
}

enum class En {
    TEST {
        inner class Nested : IFoo {
            private konst ee = TEST

            override fun foo(e: En): String {
                return if (e == ee) e.ok else "Failed"
            }
        }

        override konst ok: String get() = "OK"
        override fun foo(): IFoo = Nested()
    },
    OTHER {
        override konst ok: String get() = throw AssertionError()
        override fun foo(): IFoo = throw AssertionError()
    };

    abstract konst ok: String
    abstract fun foo(): IFoo
}

fun box() = En.TEST.foo().foo(En.TEST)
