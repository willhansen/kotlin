// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses
// !DIAGNOSTICS: -UNUSED_PARAMETER

inline class X(konst x: Int)
inline class Z(konst x: Int)

class TestOk1(konst a: Int, konst b: Int) {
    constructor(x: X) : this(x.x, 1)
}

class TestErr1(konst a: Int) {
    <!CONFLICTING_JVM_DECLARATIONS!>constructor(x: X)<!> : this(x.x)
}

class <!CONFLICTING_JVM_DECLARATIONS!>TestErr2(konst a: Int, konst b: Int)<!> {
    <!CONFLICTING_JVM_DECLARATIONS!>constructor(x: X)<!> : this(x.x, 1)
    <!CONFLICTING_JVM_DECLARATIONS!>constructor(z: Z)<!> : this(z.x, 2)
}