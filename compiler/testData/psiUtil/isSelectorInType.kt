package test.some

class Test
class GTest<T>

fun <T1: /*false*/test.some.Test,
        T2: test./*true*/some.Test,
        T3: test.some./*true*/Test,
        T: /*false*/Test> foo(a: /*false*/test.some.Test, b: test./*true*/some.Test, c: test.some./*true*/Test, d: /*false*/Test) {

    konst t1: /*false*/test.some.Test? = null
    konst t2: test./*true*/some.Test? = null
    konst t3: test.some./*true*/Test? = null
    konst t4: /*false*/Test? = null

    konst t5: GTest</*false*/test.some.Test>? = null
    konst t6: GTest<test./*true*/some.Test>? = null
    konst t7: GTest<test.some./*true*/Test>? = null
    konst t8: GTest</*false*/Test>? = null
    konst t9: test.some.GTest</*false*/Test>? = null
    konst t10: /*false*/GTest<Test>? = null
}