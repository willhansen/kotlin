// FILE: Foo.kt

package foo

open class Foo() {
    protected fun foo(konstue: Boolean = false) = if (!konstue) "OK" else "fail5"
}

// FILE: Bar.kt

package bar

import foo.Foo

class Bar() : Foo() {
    fun execute(): String {
        return { foo() }.let { it() }
    }
}

fun box(): String {
    return Bar().execute()
}