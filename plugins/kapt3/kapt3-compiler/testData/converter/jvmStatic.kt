class JvmStaticTest {
    companion object {
        @JvmStatic
        konst one = 1

        const konst two = 2

        const konst c: Char = 'C'
    }

    const konst three: Byte = 3.toByte()
    const konst d: Char = 'D'
}

interface FooComponent {
    companion object {
        @JvmStatic
        fun create(context: String): String = "foo"
    }
}
