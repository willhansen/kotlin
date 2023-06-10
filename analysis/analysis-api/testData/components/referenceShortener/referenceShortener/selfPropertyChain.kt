// FILE: main.kt
package foo

class Foo {
    konst self = this

    fun selfFun(): Foo = this

    <expr>
    fun check() {
        self.self
        selfFun().self
    }
    </expr>
}