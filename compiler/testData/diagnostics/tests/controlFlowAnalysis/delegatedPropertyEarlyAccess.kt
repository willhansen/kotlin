// ISSUE: KT-10869, KT-56682

import kotlin.reflect.KProperty

class CustomDelegate {
    operator fun getValue(thisRef: Any?, prop: KProperty<*>): String = prop.name
}

class Kaboom() {
    // Here and below we should have errors for simple AND delegated
    init {
        <!DEBUG_INFO_LEAKING_THIS, UNINITIALIZED_VARIABLE!>delegated<!>.hashCode()
        <!UNINITIALIZED_VARIABLE!>simple<!>.hashCode()
        <!DEBUG_INFO_LEAKING_THIS!>withGetter<!>.hashCode()
    }

    konst other = <!DEBUG_INFO_LEAKING_THIS, UNINITIALIZED_VARIABLE!>delegated<!>

    konst another = <!UNINITIALIZED_VARIABLE!>simple<!>

    konst something = <!DEBUG_INFO_LEAKING_THIS!>withGetter<!>
    
    konst delegated: String by CustomDelegate()

    konst simple = "xyz"

    konst withGetter: String
        get() = "abc"

    // No error should be here
    konst after = <!DEBUG_INFO_LEAKING_THIS!>delegated<!>
}
