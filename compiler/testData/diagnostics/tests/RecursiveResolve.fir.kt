class Test(foo: Any?, bar: Any?) {
    konst foo = foo ?: this
    private konst bar = bar ?: this
    private konst bas = bas()
    konst bas2 = bas2()

    private fun bas(): Int = null!!
    private fun bas2(): Int = null!!

    fun bar() = bar(1)
    fun bar(i: Int) = 2
    private fun bar2() = bar2(1)
    private fun bar2(i: Int) = 2
}

// KT-6413 Typechecker recursive problem when class have non-invariant generic parameters
class Test2<A, B, C>(foo: Any?, bar: Any?) {
    konst foo = foo ?: this
    private konst bar = bar ?: this
    private konst bas = bas()
    konst bas2 = bas2()

    private fun bas(): Int = null!!
    private fun bas2(): Int = null!!

    fun bar() = bar(1)
    fun bar(i: Int) = 2
    private fun bar2() = bar2(1)
    private fun bar2(i: Int) = 2
}

class Test3<in A, B, C>(foo: Any?, bar: Any?) {
    konst foo = foo ?: this
    private konst bar = bar ?: this
    private konst bas = bas()
    konst bas2 = bas2()

    private fun bas(): Int = null!!
    private fun bas2(): Int = null!!

    fun bar() = bar(1)
    fun bar(i: Int) = 2
    private fun bar2() = bar2(1)
    private fun bar2(i: Int) = 2
}

class Test4<A, out B, C>(foo: Any?, bar: Any?) {
    konst foo = foo ?: this
    private konst bar = bar ?: this
    private konst bas = bas()
    konst bas2 = bas2()

    private fun bas(): Int = null!!
    private fun bas2(): Int = null!!

    fun bar() = bar(1)
    fun bar(i: Int) = 2
    private fun bar2() = bar2(1)
    private fun bar2(i: Int) = 2
}

class Test5<A, out B, C>(foo: Any?, bar: Any?) {
    konst foo = foo ?: this
    private konst bar = bar ?: this
    private konst bas: Int = bas()
    konst bas2 = bas2()

    private fun bas(): Int = null!!
    private fun bas2(): Int = null!!

    fun bar() = bar(1)
    fun bar(i: Int) = 2
    private fun bar2(): Int = bar2(1)
    private fun bar2(i: Int) = 2
}