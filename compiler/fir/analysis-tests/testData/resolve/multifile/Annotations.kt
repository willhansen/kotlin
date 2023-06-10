// FILE: annotations.kt

package annotations

@Target(AnnotationTarget.FILE, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.PROPERTY_GETTER)
annotation class Simple

annotation class WithInt(konst konstue: Int)

annotation class WithString(konst s: String)

annotation class Complex(konst wi: WithInt, konst ws: WithString)

annotation class VeryComplex(konst f: Float, konst d: Double, konst b: Boolean, konst l: Long, konst n: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>Int?<!>)

// FILE: main.kt

@file:Simple
package test

import annotations.*

@WithInt(42)
abstract class First {
    @Simple
    abstract fun foo(@WithString("abc") arg: @Simple Double)

    @Complex(WithInt(7), WithString(""))
    abstract konst v: String
}

@WithString("xyz")
class Second(konst y: Char) : <!WRONG_ANNOTATION_TARGET!>@WithInt(0)<!> First() {
    override fun foo(arg: Double) {
    }

    override konst v: String
        @Simple get() = ""

    @WithString("constructor")
    constructor(): this('\n')
}

<!WRONG_ANNOTATION_TARGET!>@WithInt(24)<!>
<!WRONG_ANNOTATION_TARGET!>@VeryComplex(3.14f, 6.67e-11, false, 123456789012345L, null)<!>
typealias Third = @Simple Second
