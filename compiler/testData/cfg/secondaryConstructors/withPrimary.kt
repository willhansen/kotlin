class A(konst w: Char) {
    konst x: Int
    var y: Int
    konst z: Int
    konst v = -1

    constructor(): this('a') {
        y = 2
    }

    // anonymous
    init {
        x = w
        z = 8
    }

    constructor(a: Int, b: Int = 3): this(b.toChar()) {
        y = x
    }

    // anonymous
    init {
        y = 9
    }
}