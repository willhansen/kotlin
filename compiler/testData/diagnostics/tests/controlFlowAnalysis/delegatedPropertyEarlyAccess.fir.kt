// ISSUE: KT-10869, KT-56682

import kotlin.reflect.KProperty

class CustomDelegate {
    operator fun getValue(thisRef: Any?, prop: KProperty<*>): String = prop.name
}

class Kaboom() {
    // Here and below we should have errors for simple AND delegated
    init {
        <!UNINITIALIZED_VARIABLE!>delegated<!>.hashCode()
        <!UNINITIALIZED_VARIABLE!>simple<!>.hashCode()
        withGetter.hashCode()
    }

    konst other = <!UNINITIALIZED_VARIABLE!>delegated<!>

    konst another = <!UNINITIALIZED_VARIABLE!>simple<!>

    konst something = withGetter
    
    konst delegated: String by CustomDelegate()

    konst simple = "xyz"

    konst withGetter: String
        get() = "abc"

    // No error should be here
    konst after = delegated
}
