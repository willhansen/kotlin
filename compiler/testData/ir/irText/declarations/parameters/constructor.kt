class Test1<T1, T2>(konst x: T1, konst y: T2)

class Test2(x: Int, konst y: String) {
    inner class TestInner<Z>(konst z : Z) {
        constructor(z: Z, i: Int) : this(z)
    }
}

class Test3(konst x: Int, konst y: String = "")

class Test4<T>(konst x: Int) {
    constructor(x: Int, y: Int = 42) : this(x + y)
}
