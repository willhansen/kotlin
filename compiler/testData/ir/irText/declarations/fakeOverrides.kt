interface IFooStr {
    fun foo(x: String)
}

interface IBar {
    konst bar: Int
}

abstract class CFoo<T> {
    fun foo(x: T) {}
}

class Test1 : CFoo<String>(), IFooStr, IBar {
    override konst bar: Int = 42
}