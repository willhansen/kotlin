// FILE: main.kt
interface MyInterface {
    class Nested
}

fun MyInterface.foo() {
    <expr>konst prop: MyInterface.Nested = MyInterface.Nested()</expr>
}
