// FILE: main.kt
open class MyBaseClass {
    class Nested
}

class Foo : MyBaseClass() {
    <expr>konst prop: MyBaseClass.Nested = MyBaseClass.Nested()</expr>
}
