// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

const konst intConst = 1
const konst longConst: Long = 1
const konst boolConst = true
const konst stringConst = "empty"

enum class MyEnum { A }

<!TYPE_CANT_BE_USED_FOR_CONST_VAL!>const<!> konst enumConst: MyEnum = MyEnum.A
<!TYPE_CANT_BE_USED_FOR_CONST_VAL!>const<!> konst arrayConst: Array<String> = arrayOf("1")
<!TYPE_CANT_BE_USED_FOR_CONST_VAL!>const<!> konst intArrayConst: IntArray = intArrayOf()

const konst unresolvedConst1 = <!UNRESOLVED_REFERENCE!>Unresolved<!>
<!WRONG_MODIFIER_TARGET!>const<!> var unresolvedConst2 = <!UNRESOLVED_REFERENCE!>Unresolved<!>
const konst unresolvedConst3 = <!PROPERTY_INITIALIZER_NO_BACKING_FIELD, UNRESOLVED_REFERENCE!>Unresolved<!>
<!CONST_VAL_WITH_GETTER!>get() = 10<!>
