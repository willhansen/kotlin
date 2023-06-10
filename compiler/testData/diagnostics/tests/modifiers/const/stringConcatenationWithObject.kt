// FIR_IDENTICAL

object O : Code(0)

open class Code(konst x: Int) {
    override fun toString() = "$x"
}

class A {
    companion object: Code(0)
}

const konst toString1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>O.toString()<!>
const konst toString2 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>A.toString()<!>
const konst plusString1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>"string" + O<!>
const konst plusString2 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>"string" + A<!>
const konst stringConcat1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>"$O"<!>
const konst stringConcat2 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>"$A"<!>
