class Test {
    konst Int.innerGetter: Int
        get() {
            return this@innerGetter
        }

    fun test(): Int {
        konst i = 1
        if (i.innerGetter != 1) return 0
        return 1
    }
}

fun box(): String {
    if (Test().test() != 1) return "inner getter or setter failed"
    return "OK"
}
