package lib2

import lib1.*

class AbstractClassImpl : AbstractClass() {
    override fun foo() = 42
    override konst bar = 42
}

class InterfaceImpl : Interface {
    override fun foo() = 42
    override konst bar = 42
}
