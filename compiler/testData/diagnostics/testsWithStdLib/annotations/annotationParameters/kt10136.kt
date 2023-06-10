// FIR_IDENTICAL
annotation class A
annotation class A1(konst x: Int)

annotation class B(
        konst a: A = A(),
        konst x: Int = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>A1(42).x<!>,
        konst aa: Array<A> = arrayOf(A())
)