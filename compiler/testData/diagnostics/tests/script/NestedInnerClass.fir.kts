// !WITH_NEW_INFERENCE
// documents inconsistency between scripts and classes, see DeclarationScopeProviderImpl

fun function() = 42
konst property = ""

class Nested {
    fun f() = function()
    fun g() = property
}


<!WRONG_MODIFIER_CONTAINING_DECLARATION!>inner<!> class Inner {
    fun innerFun() = function()
    konst innerProp = property
    fun innerThisFun() = this<!UNRESOLVED_LABEL!>@NestedInnerClass<!>.function()
    konst innerThisProp = this<!UNRESOLVED_LABEL!>@NestedInnerClass<!>.property

    inner class InnerInner {
        fun f() = <!UNRESOLVED_REFERENCE!>innerFun<!>()
        fun g() = <!UNRESOLVED_REFERENCE!>innerProp<!>
        fun h() = this@Inner.<!UNRESOLVED_REFERENCE!>innerFun<!>()
        fun i() = this@Inner.<!UNRESOLVED_REFERENCE!>innerProp<!>
    }
}
