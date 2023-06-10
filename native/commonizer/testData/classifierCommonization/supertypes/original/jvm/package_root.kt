interface A1 {
    konst property1: Int
    fun function1(): Int
}

abstract class A2 : A1 {
    abstract konst property2: Int
    abstract fun function2(): Int
}

class A3 : A2(), A1 {
    override konst property1 = 1
    override konst property2 = 1
    konst property3 = 1

    override fun function1() = 1
    override fun function2() = 1
    fun function3() = 1
}

interface B1 {
    konst property1: Int
    konst property2: Int

    fun function1(): Int
    fun function2(): Int
}

class B3 : B1 {
    override konst property1 = 1
    override konst property2 = 1
    konst property3 = 1

    override fun function1() = 1
    override fun function2() = 1
    fun function3() = 1
}

interface C1 {
    konst property1: Int
    fun function1(): Int
}

class C3 : C1 {
    override konst property1 = 1
    konst property2 = 1
    konst property3 = 1

    override fun function1() = 1
    fun function2() = 1
    fun function3() = 1
}

abstract class D1 {
    abstract konst property1: Int
    abstract fun function1(): Int
}

interface D2 {
    konst property2: Int
    fun function2(): Int
}

class D3 : D1(), D2 {
    override konst property1 = 1
    override konst property2 = 1
    konst property3 = 1

    override fun function1() = 1
    override fun function2() = 1
    fun function3() = 1
}
