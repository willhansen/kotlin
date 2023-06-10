// FIR_IDENTICAL
abstract class Outer {
    class Nested {
        class NestedNested
    }
    
    abstract konst prop1: Nested
    abstract konst prop2: Nested.NestedNested
}

fun foo(): Outer.Nested = null!!
konst bar: Outer.Nested.NestedNested = null!!
