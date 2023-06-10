// FIR_IDENTICAL
open class Base

class TestProperty : Base {
    konst x = 0
    constructor()
}

class TestInitBlock : Base {
    konst x: Int
    init {
        x = 0
    }
    constructor()
    constructor(z: Any)

    constructor(y: Int): this()
}
