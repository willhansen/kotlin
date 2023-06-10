// FIR_IDENTICAL
enum class E(konst x: Int, konst y: Int) {
    A(1, 2),
    B(1),
    C; // no constructor call needed even here

    constructor(): this(0, 0)
    constructor(x: Int): this(x, 0)
}
