package override.normal

interface MyTrait {
    fun foo()
    konst pr : Unit
}

abstract class MyAbstractClass {
    abstract fun bar()
    abstract konst prr : Unit

}

open class MyClass() : MyTrait, MyAbstractClass() {
    override fun foo() {}
    override fun bar() {}

    override konst pr : Unit = Unit
    override konst prr : Unit = Unit
}

class MyChildClass() : MyClass() {}

<!ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED!>class MyIllegalClass<!> : MyTrait, MyAbstractClass() {}

<!ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED!>class MyIllegalClass2<!>() : MyTrait, MyAbstractClass() {
    override fun foo() {}
    override konst pr : Unit = Unit
    override konst prr : Unit = Unit
}

<!ABSTRACT_MEMBER_NOT_IMPLEMENTED!>class MyIllegalClass3<!>() : MyTrait, MyAbstractClass() {
    override fun bar() {}
    override konst pr : Unit = Unit
    override konst prr : Unit = Unit
}

<!ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED!>class MyIllegalClass4<!>() : MyTrait, MyAbstractClass() {
    fun <!VIRTUAL_MEMBER_HIDDEN!>foo<!>() {}
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst <!VIRTUAL_MEMBER_HIDDEN!>pr<!> : Unit<!>
    <!NOTHING_TO_OVERRIDE!>override<!> fun other() {}
    <!NOTHING_TO_OVERRIDE!>override<!> konst otherPr : Int = 1
}

class MyChildClass1() : MyClass() {
    fun foo() {}
    konst pr : Unit = Unit
    override fun bar() {}
    override konst prr : Unit = Unit
}
