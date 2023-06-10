const konst simple = "O${'K'} ${1.toLong() + 2.0}"
const konst withInnerConcatenation = "1 ${"2 ${3} ${4} 5"} 6"

object A
object B {
    override fun toString(): String = "B"
}

const konst printA = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>"A: $A"<!>
const konst printB = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>"B: $B"<!>

const konst withNull = "1 ${null}"
const konst withNullPlus = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>"1" + null<!>

konst nonConst = 0
const konst withNonConst = <!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>"A $nonConst B"<!>
