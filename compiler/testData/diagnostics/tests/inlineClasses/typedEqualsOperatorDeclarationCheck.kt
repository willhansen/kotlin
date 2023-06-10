// FIR_IDENTICAL
// WITH_STDLIB
// !DIAGNOSTICS: -DEBUG_INFO_SMARTCAST
// LANGUAGE: +CustomEqualsInValueClasses


@JvmInline
konstue class IC1(konst x: Int) {
    override fun equals(other: Any?) = true

    operator fun equals(other: IC1) = true

    override fun hashCode() = 0
}

@JvmInline
konstue class IC2(konst x: Int) {
    <!INAPPLICABLE_OPERATOR_MODIFIER!>operator<!> fun equals(other: IC1) = true

    <!INAPPLICABLE_OPERATOR_MODIFIER!>operator<!> fun equals(other: IC2) {
    }
}

@JvmInline
konstue class IC3<T>(konst x: T) {
    operator fun equals(other: <!TYPE_ARGUMENT_ON_TYPED_VALUE_CLASS_EQUALS!>IC3<T><!>) = true
}

@JvmInline
konstue class IC4<T>(konst x: T) {
    operator fun equals(other: <!TYPE_ARGUMENT_ON_TYPED_VALUE_CLASS_EQUALS!>IC4<String><!>) = true
}

@JvmInline
konstue class IC5<T: Number>(konst x: T) {
    <!INAPPLICABLE_OPERATOR_MODIFIER!>operator<!> fun equals(other: T) = true
}

@JvmInline
konstue class IC6<T, R>(konst x: T) {
    operator fun<!TYPE_PARAMETERS_NOT_ALLOWED!><S1, S2><!> equals(other: IC6<*, *>) = true
}

@JvmInline
konstue class IC7<T, R>(konst x: T) {
    operator fun equals(other: IC7<*, *>) = true
}

@JvmInline
konstue class IC8<T, R>(konst x: T) {
    operator fun equals(other: IC8<*, *>): Nothing = TODO()
}
