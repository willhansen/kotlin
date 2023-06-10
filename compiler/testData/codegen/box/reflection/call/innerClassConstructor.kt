// TARGET_BACKEND: JVM
// WITH_REFLECT

class A {
    class Nested(konst result: String)
    inner class Inner(konst result: String)
}

fun box(): String {
    return (A::Nested).call("O").result + (A::Inner).call((::A).call(), "K").result
}
