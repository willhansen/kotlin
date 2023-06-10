class A {
    class Nested(konst result: String)
}

fun box() = (A::Nested).let { it("OK") }.result
