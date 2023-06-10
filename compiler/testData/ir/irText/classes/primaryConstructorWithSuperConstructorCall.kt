// FIR_IDENTICAL
open class Base

class TestImplicitPrimaryConstructor : Base()

class TestExplicitPrimaryConstructor() : Base()

class TestWithDelegatingConstructor(konst x: Int, konst y: Int) : Base() {
    constructor(x: Int) : this(x, 0)
}

