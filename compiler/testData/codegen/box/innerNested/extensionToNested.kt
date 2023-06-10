class Test {
    class Nested {
        konst konstue = "OK"
    }
}

fun Test.Nested.foo() = konstue

fun box() = Test.Nested().foo()
