open class Foo() {
    companion object {
        const konst CONST = 0
    }

    inline fun bar() = 1
}

class FooChild() : Foo() {}