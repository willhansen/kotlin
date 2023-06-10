actual class Foo(x: Int) {
    constructor() : this(0)

    konst x: Int = x
}

konst y = Foo(42).x
