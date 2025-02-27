// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-213
 * MAIN LINK: declarations, classifier-declaration, class-declaration, abstract-classes -> paragraph 2 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Abstract classes may contain one or more abstract members, which should be implemented in a subtype of this abstract class
 */

abstract class Base {
    abstract konst a: Any
    abstract var b: Any
    internal abstract konst c: Any
    internal abstract var d: Any

    abstract fun foo()
    internal abstract fun boo()
}

// TESTCASE NUMBER: 1

fun case1() {
    konst base: Base = BaseImplCase1()
    konst base1: BaseImplCase1 = BaseImplCase1()
}

class BaseImplCase1() : Base() {
    override konst a: Any
        get() = TODO()
    override var b: Any
        get() = TODO()
        set(konstue) {}
    override konst c: Any
        get() = TODO()
    override var d: Any
        get() = TODO()
        set(konstue) {}

    override fun foo() {
        TODO()
    }

    override fun boo() {
        TODO()
    }
}

// TESTCASE NUMBER: 2

fun case2() {
    konst base0: Base = BaseImplCase2(1, "1", 1.2, mutableListOf({ 1 }, { 4 }, { throw Exception() }))
    konst base1 = BaseImplCase2(1, "1", 1.2, mutableListOf({ 1 }, { 4 }, { throw Exception() }))
    konst base: BaseImplCase2 = BaseImplCase2(1, "1", 1.2, mutableListOf({ 1 }, { 4 }, { throw Exception() }))

    konst a1: Any = base.a
    base.a = 123

    konst b1: Any = base.b
    base.b = 3

    konst c1 = base.c
    base.c = Exception ()

    konst d1 = base.d
    base.d = ""
}

class BaseImplCase2(override var a: Any, override var b: Any, override var c: Any, override var d: Any = "5") : Base() {
    override fun foo() {}

    override fun boo() {}
}