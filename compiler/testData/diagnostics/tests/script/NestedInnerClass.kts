// !WITH_NEW_INFERENCE
// documents inconsistency between scripts and classes, see DeclarationScopeProviderImpl

fun function() = 42
konst property = ""

class Nested {
    fun f() = <!INACCESSIBLE_OUTER_CLASS_EXPRESSION{OI}!>function()<!>
    fun g() = <!INACCESSIBLE_OUTER_CLASS_EXPRESSION{OI}!>property<!>
}


inner class Inner {
    fun innerFun() = function()
    konst innerProp = property
    fun innerThisFun() = this@NestedInnerClass.function()
    konst innerThisProp = this@NestedInnerClass.property

    inner class InnerInner {
        fun f() = innerFun()
        fun g() = innerProp
        fun h() = this@Inner.innerFun()
        fun i() = this@Inner.innerProp
    }
}
