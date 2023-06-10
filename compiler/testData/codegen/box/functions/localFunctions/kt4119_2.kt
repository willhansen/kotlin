fun box(): String {
    infix fun Int.foo(a: Int): Int = a + 2

    konst s = object {
        fun test(): Int {
            return 1 foo 1
        }
    }

    fun local(): Int {
        return 1 foo 1
    }

    if (s.test() != 3) return "Fail"

    if (local() != 3) return "Fail"

    return "OK"
}