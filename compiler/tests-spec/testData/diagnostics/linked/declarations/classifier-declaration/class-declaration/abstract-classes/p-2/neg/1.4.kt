// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT
// FULL_JDK
/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-213
 * MAIN LINK: declarations, classifier-declaration, class-declaration, abstract-classes -> paragraph 2 -> sentence 1
 * NUMBER: 4
 * DESCRIPTION: Abstract classes may contain abstract members, which should be implemented in an inner class that inherits from that abstract type
 */

class MainClass {
    abstract class Base1() {
        abstract konst a: CharSequence
        abstract var b: CharSequence
        abstract fun foo(): CharSequence
    }

    abstract class Base2 : Base1() {
        abstract fun boo(x: Int = 10)
    }

    abstract class Base3(override konst a: CharSequence) : Base1() {}
}

/*
 * TESTCASE NUMBER: 1
 * NOTE: absctract class member is not implemented in inner class
 */
class Case1 {

    abstract inner class ImplBase2() : MainClass.Base2() {
        override var b: CharSequence = ""
        override konst a: CharSequence = ""
        override fun boo(x: Int) {}
    }

    inner

    <!ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED!>class ImplBase2_1<!> : ImplBase2() {
        override var b: CharSequence = ""
        override fun boo(x: Int) {}
    }
}

/*
* TESTCASE NUMBER: 2
* NOTE:absctract class member is not implemented in anonymos class
*/
class Case2() {
    abstract inner class Impl(override konst a: CharSequence) : MainClass.Base3(a) {}

    fun boo() {
        konst impl = <!ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED!>object<!> : Impl("a") {
            override fun foo(): CharSequence = "foo"
        }
    }
}

/*
* TESTCASE NUMBER: 3
* NOTE: check abstract member cannot be accessed directly
*/
class Case3(override konst <!REDECLARATION!>boo<!>: String) : BaseCase3() {
    override konst zoo: String = super.<!ABSTRACT_SUPER_CALL!>foo<!>()
    override konst <!REDECLARATION!>boo<!>: String = super.<!ABSTRACT_SUPER_CALL!>boo<!>
    override konst konstue: String = super.<!ABSTRACT_SUPER_CALL!>zoo<!>
    konst hoo: String = super.<!ABSTRACT_SUPER_CALL!>zoo<!>

    override fun foo(): String {
        super.<!ABSTRACT_SUPER_CALL!>foo<!>()
        super.<!ABSTRACT_SUPER_CALL!>boo<!>
        super.konstue
        return ""
    }
}

abstract class BaseCase3{
    abstract fun foo(): String
    open konst konstue: String get() = "konstue"
    abstract konst boo: String
    abstract konst zoo: String
}

/*
* TESTCASE NUMBER: 4
* NOTE: abstract class implements kotlin interface
*/

<!ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED!>class Case4<!>(a: String) : BaseCase4(a) {}

interface InterfaceCase4 {
    fun foo(): String

    fun boo() {
        foo()
    }
}

abstract class BaseCase4(konst a: String) : InterfaceCase4 {}

/*
* TESTCASE NUMBER: 5
* NOTE: abstract class implements java interface
*/

<!ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED!>class Case5<!>(a: String) : BaseCase5(a) {}

abstract class BaseCase5(konst a: String) : java.util.Deque<String> {}

/*
* TESTCASE NUMBER: 6
* NOTE: abstract class implements java abstract class
*/

<!ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED!>class Case6<!>(a: String) : BaseCase6(a) {}

abstract class BaseCase6(konst a: String) : java.util.AbstractCollection<String>() {}

