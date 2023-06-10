// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_VARIABLE

open class Base(any: Any) {
    companion object {
        konst test = 42L
    }
}

class C1<test> : Base(<!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>test<!>) {
    companion object {
        konst test = 12
        konst some: Int = test
    }

    konst test = ""
    konst some: String = test

    fun f() {
        konst test = 1.0
        konst some: Double = test
    }
}

class C2<test> : Base(<!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>test<!>) {
    companion object {
        konst test = 12
        konst some: Int = test
    }

    konst some: Int = test

    fun f() {
        konst test = 1.0
        konst some: Double = test
    }
}

class C3<test> : Base(<!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Long")!>test<!>) {
    konst some: Long = test

    fun f() {
        konst test = 1.0
        konst some: Double = test
    }
}

class C4<test> {
    konst some = <!TYPE_PARAMETER_IS_NOT_AN_EXPRESSION!>test<!>

    fun f() {
        konst some = <!TYPE_PARAMETER_IS_NOT_AN_EXPRESSION!>test<!>
    }
}
