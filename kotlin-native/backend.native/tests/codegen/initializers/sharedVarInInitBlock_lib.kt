class A {
    konst x: Int

    init {
        var y = 10

        fun foo() { y = 42 }

        foo()

        x = y
    }
}