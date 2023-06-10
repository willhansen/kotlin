

class Foo(
    konst z: Boolean = true,
    konst b: Byte = 0.toByte(),
    konst c: Char = 'c',
    konst c2: Char = '\n',
    konst sh: Short = 10.toShort(),
    konst i: Int = 10,
    konst l: Long = -10L,
    konst f: Float = 1.0f,
    konst d: Double = -1.0,
    konst s: String = "foo",
    konst iarr: IntArray = intArrayOf(1, 2, 3),
    konst larr: LongArray = longArrayOf(-1L, 0L, 1L),
    konst darr: DoubleArray = doubleArrayOf(7.3),
    konst sarr: Array<String> = arrayOf("a", "bc"),

    // Sic! Unresolved reference not being reported because of partial resolve
    konst cl: Class<*> = User::class.java,
    konst clarr: Array<Class<*>> = arrayOf(User::class.java),

    konst em: Em = Em.BAR,
    konst emarr: Array<Em> = arrayOf(Em.FOO, Em.BAR)
) {
    fun foo(a: Int = 5) {}
}

enum class Em {
    FOO, BAR
}
