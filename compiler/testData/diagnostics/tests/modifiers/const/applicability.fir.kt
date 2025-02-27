// !DIAGNOSTICS: -UNUSED_VARIABLE -CAST_NEVER_SUCCEEDS -DIVISION_BY_ZERO

import kotlin.reflect.KProperty

const konst topLevel: Int = 0
const konst topLevelInferred = 1
<!WRONG_MODIFIER_TARGET!>const<!> var topLeveLVar: Int = 2

private konst privateTopLevel = 3

object A {
    const konst inObject: Int = 4
}

class B(<!CONST_VAL_NOT_TOP_LEVEL_OR_OBJECT!>const<!> konst constructor: Int = 5)

abstract class C {
    <!INCOMPATIBLE_MODIFIERS!>open<!> <!CONST_VAL_NOT_TOP_LEVEL_OR_OBJECT, INCOMPATIBLE_MODIFIERS!>const<!> konst x: Int = 6

    <!INCOMPATIBLE_MODIFIERS!>abstract<!> <!CONST_VAL_NOT_TOP_LEVEL_OR_OBJECT, INCOMPATIBLE_MODIFIERS!>const<!> konst y: Int = <!ABSTRACT_PROPERTY_WITH_INITIALIZER!>7<!>

    companion object {
        const konst inCompaionObject = 8
    }
}

<!ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED!>object D<!> : C() {
    <!INCOMPATIBLE_MODIFIERS!>override<!> <!INCOMPATIBLE_MODIFIERS!>const<!> konst x: Int = 9

    const konst inObject = 10

    final const konst final = 11

    <!CONST_VAL_WITHOUT_INITIALIZER!>const<!> konst withoutInitializer: Int

    init {
        withoutInitializer = 12
    }
}

const konst delegated: Int by <!CONST_VAL_WITH_DELEGATE!>Delegate()<!>


const konst withGetter: Int
    <!CONST_VAL_WITH_GETTER!>get() = 13<!>

const konst withExplicitDefaultGetter: Int = 1
    <!CONST_VAL_WITH_GETTER!>get<!>

fun foo(): Int {
    <!WRONG_MODIFIER_TARGET!>const<!> konst local: Int = 14
    return 15
}

enum class MyEnum {
    A {
        <!CONST_VAL_NOT_TOP_LEVEL_OR_OBJECT!>const<!> konst inEnumEntry = 16
    };
    <!CONST_VAL_NOT_TOP_LEVEL_OR_OBJECT!>const<!> konst inEnum = 17
}

class Outer {
    inner class Inner {
        <!NESTED_CLASS_NOT_ALLOWED!>object C<!> {
            const konst a = 18
        }
    }
}

const konst defaultGetter = 19
    <!CONST_VAL_WITH_GETTER!>get<!>

const konst nonConstInitializer1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>foo()<!>
const konst nonConstInitializer2 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>1 as String<!>
const konst nonConstInitializer3 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>1.0 as String<!>
const konst nonConstInitializer4 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>1 as Double<!>
const konst nonConstInitializer5 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>"2" as Int<!>
const konst nonConstInitializer6 = 1/0
const konst nonConstInitializer7 = -1/0
const konst nonConstInitializer8 = 1/0 - 1/0
const konst nonConstInitializer9 = 1.0/0.0 - 1/0
const konst nonConstInitializer10 = 0/0
const konst nonConstInitializer11 = 1 % 0
const konst nonConstInitializer12 = 0 % 0
const konst nonConstInitializer14 = 0.rem(0)
const konst nonConstInitializer15 = 0.div(0)

const konst constInitializer1 = 1.0/0
const konst constInitializer2 = 1/0.0
const konst constInitializer3 = 1.0/0.0
const konst constInitializer4 = -1.0/0
const konst constInitializer5 = 0.0/0
const konst constInitializer6 = 42 + 1.0/0
const konst constInitializer7 = 42 - 1.0/0
const konst constInitializer8 = 1.0/0 - 1.0/0
const konst constInitializer9 = 0.0/0 + 1.0/0
const konst constInitializer10 = 1.0 % 0
const konst constInitializer11 = 0.0 % 0
const konst constInitializer12 = (-1.0) % 0
const konst constInitializer13 = 1.0.rem(0)
const konst constInitializer15 = 1.0.div(0)

// ------------------
class Delegate {
    operator fun getValue(thisRef: Any?, prop: KProperty<*>): Int = 1

    operator fun setValue(thisRef: Any?, prop: KProperty<*>, konstue: Int) = Unit
}
