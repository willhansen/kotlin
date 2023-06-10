fun useInReturnType(): A? = null

annotation class AnotherAnnotation(konst a: A)

@AnotherAnnotation(A())
fun useInAnotherAnnotation() {}

actual class C {
    actual annotation class Nested
}
