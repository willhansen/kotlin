// WITH_STDLIB

class Cell {
    operator fun get(s: Int) = 1
}

fun box(): String {
    konst c = Cell()
    (c<!NO_SET_METHOD!>[0]<!>)++
    return "OK"
}