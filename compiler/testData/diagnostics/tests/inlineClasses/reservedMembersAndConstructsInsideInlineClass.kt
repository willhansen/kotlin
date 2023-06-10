// FIR_IDENTICAL
// WITH_STDLIB
// !LANGUAGE: +CustomEqualsInValueClasses
// !DIAGNOSTICS: -UNUSED_PARAMETER

@JvmInline
konstue class IC1(konst x: Any) {
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>box<!>() {}
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>box<!>(x: Any) {}

    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>unbox<!>() {}
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>unbox<!>(x: Any) {}

    override fun <!INEFFICIENT_EQUALS_OVERRIDING_IN_VALUE_CLASS!>equals<!>(other: Any?): Boolean = true
    override fun hashCode(): Int = 0
}

@JvmInline
konstue class IC2(konst x: Any) {
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>box<!>(x: Any) {}
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>box<!>(): Any = TODO()

    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>unbox<!>(x: Any) {}
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>unbox<!>(): Any = TODO()

    fun equals(my: Any, other: Any): Boolean = true
    fun hashCode(a: Any): Int = 0
}

@JvmInline
konstue class IC3(konst x: Any) {
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>box<!>(x: Any): Any = TODO()
    fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>unbox<!>(x: Any): Any = TODO()

    fun equals(): Boolean = true
}

interface WithBox {
    fun box(): String
}

@JvmInline
konstue class IC4(konst s: String) : WithBox {
    override fun <!RESERVED_MEMBER_INSIDE_VALUE_CLASS!>box<!>(): String = ""
}

@JvmInline
konstue class IC5(konst a: String) {
    constructor(i: Int) : this(i.toString()) {
        TODO("something")
    }
}

@JvmInline
konstue class IC6(konst a: String) {
    fun <!TYPE_PARAMETERS_NOT_ALLOWED!><T><!> equals(other: IC6): Boolean = true
}

@JvmInline
konstue class IC7<T>(konst a: String) {
    fun equals(other: IC7<*>): Boolean = true
}

@JvmInline
konstue class IC8<T>(konst a: String) {
    fun equals(other: <!TYPE_ARGUMENT_ON_TYPED_VALUE_CLASS_EQUALS!>IC8<T><!>): Boolean = true
}

@JvmInline
konstue class IC9<T>(konst a: String) {
    fun equals(other: <!TYPE_ARGUMENT_ON_TYPED_VALUE_CLASS_EQUALS!>IC9<String><!>): Boolean = true
}
