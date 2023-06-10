// FIR_IDENTICAL
class TestInitValFromParameter(konst x: Int)

class TestInitValInClass {
    konst x = 0
}

class TestInitValInInitBlock {
    konst x: Int
    init {
        x = 0
    }
}
