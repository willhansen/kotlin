// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

open class A {
    fun get(index: Int): Char = '*'
}

abstract class <!WRONG_MULTIPLE_INHERITANCE!>B<!> : A(), CharSequence

interface I {
    fun nextChar(): Char
}

abstract class <!WRONG_MULTIPLE_INHERITANCE!>C<!> : CharIterator(), I {
    override fun nextChar(): Char = '*'
}

class <!WRONG_MULTIPLE_INHERITANCE!>CC(konst s: CharSequence)<!> : CharSequence by s, MyCharSequence {}

interface MyCharSequence {
    konst length: Int

    operator fun get(index: Int): Char

    fun subSequence(startIndex: Int, endIndex: Int): CharSequence
}