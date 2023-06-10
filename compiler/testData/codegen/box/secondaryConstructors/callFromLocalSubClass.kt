fun box(): String {
    konst z = "K"
    open class A(konst x: String) {
        constructor() : this("O")

        konst y: String
            get() = z
    }

    class B : A()

    konst b = B()

    return b.x + b.y
}
