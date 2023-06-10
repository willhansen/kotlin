// FIR_IDENTICAL
class Test1(konst x: Int, konst y: Int)

class Test2(x: Int, konst y: Int) {
    konst x = x
}

class Test3(x: Int, konst y: Int) {
    konst x: Int

    init {
        this.x = x
    }
}