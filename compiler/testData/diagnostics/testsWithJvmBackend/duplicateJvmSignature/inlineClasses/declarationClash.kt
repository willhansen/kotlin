// WITH_STDLIB
// TARGET_BACKEND: JVM_IR

@JvmInline
konstue class <!CONFLICTING_JVM_DECLARATIONS!>A(konst x: Int)<!> {
    <!CONFLICTING_JVM_DECLARATIONS!>constructor(x: UInt)<!>: this(x.toInt())
}

data class <!CONFLICTING_JVM_DECLARATIONS!>B(konst x: UInt)<!> {
    <!CONFLICTING_JVM_DECLARATIONS!>constructor(x: Int)<!> : this(x.toUInt())
}
