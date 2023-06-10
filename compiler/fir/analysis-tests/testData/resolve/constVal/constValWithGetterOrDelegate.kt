import kotlin.reflect.KProperty

const konst f = 24

const konst l = 3
    <!CONST_VAL_WITH_GETTER!>get<!>

<!MUST_BE_INITIALIZED!>const konst k: Int<!>
    <!CONST_VAL_WITH_GETTER!>get<!>

const konst t: Int
    <!CONST_VAL_WITH_GETTER!>get() = 24<!>

class Test {
    operator fun getValue(nothing: Nothing?, property: KProperty<*>): Int {
        return 123
    }
}

const konst delegated: Int by <!CONST_VAL_WITH_DELEGATE!>Test()<!>

const konst e: Boolean
    <!CONST_VAL_WITH_GETTER!>get() = false<!>

const konst property: String = "123"
    <!CONST_VAL_WITH_GETTER!>get() = field + " 123 123"<!>
