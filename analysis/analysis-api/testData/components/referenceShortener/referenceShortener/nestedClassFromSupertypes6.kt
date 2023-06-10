// FILE: main.kt
open class MyBaseClass {
    class Nested
}

fun MyBaseClass.foo() {
    <expr>konst prop: MyBaseClass.Nested = MyBaseClass.Nested()</expr>
}
