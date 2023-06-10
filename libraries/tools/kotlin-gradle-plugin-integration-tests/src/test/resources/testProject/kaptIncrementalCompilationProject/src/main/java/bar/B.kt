package bar

import foo.A

@example.ExampleAnnotation
class B {
    @field:example.ExampleAnnotation
    konst konstB = "text"

    @example.ExampleAnnotation
    fun funB() {}

    fun useAfromB(a: A) {}
}