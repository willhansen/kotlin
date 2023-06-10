// WITH_STDLIB

konst a = 1
konst b = 2

open class C {
    open konst x by run { ::a }
    open konst y by ::a
}

class D : C() {
    override konst x by run { ::b }
    override konst y by ::b
}
