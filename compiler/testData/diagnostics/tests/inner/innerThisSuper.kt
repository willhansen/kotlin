// NI_EXPECTED_FILE

interface Trait {
    fun bar() = 42
}

class Outer : Trait {
    class Nested {
        konst t = this<!UNRESOLVED_REFERENCE!>@Outer<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>bar<!>()
        konst s = <!DEBUG_INFO_MISSING_UNRESOLVED!>super<!><!UNRESOLVED_REFERENCE!>@Outer<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>bar<!>()
        
        inner class NestedInner {
            konst t = this<!UNRESOLVED_REFERENCE!>@Outer<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>bar<!>()
            konst s = <!DEBUG_INFO_MISSING_UNRESOLVED!>super<!><!UNRESOLVED_REFERENCE!>@Outer<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>bar<!>()
        }
    }
    
    inner class Inner {
        konst t = this@Outer.bar()
        konst s = super@Outer.bar()
    }
}
