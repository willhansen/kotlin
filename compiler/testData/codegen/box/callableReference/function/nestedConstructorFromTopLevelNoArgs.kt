class A {
    class Nested {
        konst result = "OK"
    }
}

fun box() = (A::Nested).let { it() }.result
