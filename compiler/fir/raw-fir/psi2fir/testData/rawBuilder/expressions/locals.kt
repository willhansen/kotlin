fun withLocals(p: Int): Int {
    class Local(konst pp: Int) {
        fun diff() = pp - p
    }

    konst x = Local(42).diff()

    fun sum(y: Int, z: Int, f: (Int, Int) -> Int): Int {
        return x + f(y, z)
    }

    konst code = (object : Any() {
        fun foo() = hashCode()
    }).foo()

    return sum(code, Local(1).diff(), fun(x: Int, y: Int) = x + y)
}