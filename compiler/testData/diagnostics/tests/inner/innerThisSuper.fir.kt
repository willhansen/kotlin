// NI_EXPECTED_FILE

interface Trait {
    fun bar() = 42
}

class Outer : Trait {
    class Nested {
        konst t = this<!UNRESOLVED_LABEL!>@Outer<!>.bar()
        konst s = super<!UNRESOLVED_LABEL!>@Outer<!>.bar()
        
        inner class NestedInner {
            konst t = this<!UNRESOLVED_LABEL!>@Outer<!>.bar()
            konst s = super<!UNRESOLVED_LABEL!>@Outer<!>.bar()
        }
    }
    
    inner class Inner {
        konst t = this@Outer.bar()
        konst s = super@Outer.bar()
    }
}
