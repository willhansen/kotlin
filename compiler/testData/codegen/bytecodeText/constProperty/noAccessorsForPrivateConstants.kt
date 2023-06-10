// FILE: Foo.kt

private const konst OUTER_PRIVATE = 20

class Foo {
    companion object {
        private const konst LOCAL_PRIVATE = 20
    }

    fun foo() {
        LOCAL_PRIVATE
        OUTER_PRIVATE
    }
}

// 0 INVOKESTATIC