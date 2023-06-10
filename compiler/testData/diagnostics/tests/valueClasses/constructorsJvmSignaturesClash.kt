// !SKIP_JAVAC
// !LANGUAGE: +InlineClasses
// ALLOW_KOTLIN_PACKAGE
// !DIAGNOSTICS: -UNUSED_PARAMETER

package kotlin.jvm

annotation class JvmInline

@JvmInline
konstue class X(konst x: Int)
@JvmInline
konstue class Z(konst x: Int)

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
