// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -TOPLEVEL_TYPEALIASES_ONLY

interface ICell<T> {
    konst x: T
}

class Cell<T>(override konst x: T): ICell<T>

open class Base<T> {
    typealias CT = Cell<T>
    inner class InnerCell(override konst x: T): ICell<T>
}

class Derived : Base<Int>() {
    konst x1: InnerCell = InnerCell(42)
    konst x2: Base<Int>.InnerCell = InnerCell(42)

    konst test1: CT = Cell(42)
    konst test2: Base<Int>.CT = Cell(42)
}
