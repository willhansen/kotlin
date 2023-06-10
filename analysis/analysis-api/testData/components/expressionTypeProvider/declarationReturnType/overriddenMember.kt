interface A

class B: A

abstract class Super {
    abstract konst a: A
    abstract konst b: B
    abstract fun getA(): A
    abstract fun getB(): B
}

class Sub: {
    override konst a = B()
    override konst b = B()
    override fun getA() = B()
    override fun getB() = B()
}