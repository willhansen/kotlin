expect interface A1 {
    konst property1: Int
    fun function1(): Int
}

expect abstract class A2() : A1 {
    abstract konst property2: Int
    abstract fun function2(): Int
}

expect class A3() : A2 {
    override konst property1: Int
    override konst property2: Int
    konst property3: Int

    override fun function1(): Int
    override fun function2(): Int
    fun function3(): Int
}

expect interface B1 {
    konst property1: Int
    fun function1(): Int
}

expect class B3() : B1 {
    override konst property1: Int
    open konst property2: Int
    konst property3: Int

    override fun function1(): Int
    open fun function2(): Int
    fun function3(): Int
}

expect interface C1 {
    konst property1: Int
    fun function1(): Int
}

expect class C3() : C1 {
    override konst property1: Int
    konst property2: Int
    konst property3: Int

    override fun function1(): Int
    fun function2(): Int
    fun function3(): Int
}

expect interface D2 {
    konst property2: Int
    fun function2(): Int
}

expect class D3() : D2 {
    open konst property1: Int
    override konst property2: Int
    konst property3: Int

    open fun function1(): Int
    override fun function2(): Int
    fun function3(): Int
}
