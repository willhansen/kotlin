// FIR_IDENTICAL

abstract class TestType<V: Any> {
    open inner class Inner(konst item: V)
}

class Derived: TestType<Long>() {
    inner class DerivedInner(item: Long): Inner(item)
}
