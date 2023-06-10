// FILE: main.kt

interface MyInterface {
    class Nested
}

class Foo : MyInterface {
    <expr>konst prop: MyInterface.Nested = MyInterface.Nested()</expr>
}
