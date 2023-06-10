// !LANGUAGE: +ProhibitInvisibleAbstractMethodsInSuperclasses
// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT
// FULL_JDK

// MODULE: base
// FILE: AbstractClassCase1.kt
package base

// TESTCASE NUMBER: 1
abstract class AbstractClassCase1() {
    <!INCOMPATIBLE_MODIFIERS!>private<!> <!INCOMPATIBLE_MODIFIERS!>abstract<!> fun priv()
    protected abstract fun prot()
    internal abstract fun int()
    public abstract fun pub()

    <!INCOMPATIBLE_MODIFIERS!>private<!> <!INCOMPATIBLE_MODIFIERS!>abstract<!> konst priv1: String
    protected abstract konst prot1: String
    internal abstract konst int1: String
    public abstract konst pub1: String
}

<!INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER_ERROR!>class Case1<!> : AbstractClassCase1(){
    override fun prot() {}

    override fun int() {
        prot()
    }

    override fun pub() {}

    override konst prot1: String
        get() = ""
    override konst int1: String
        get() = ""
    override konst pub1: String
        get() = ""

}

fun case1(){
    konst a = Case1()
    a.<!INVISIBLE_REFERENCE!>priv<!>()
    a.<!INVISIBLE_REFERENCE!>prot<!>()
    a.int()
    a.pub()

    a.<!INVISIBLE_REFERENCE!>priv1<!>
    a.<!INVISIBLE_REFERENCE!>prot1<!>
    a.int1
    a.pub1
}

//MODULE: implBase(base)
//FILE: Impl.kt
package implBase
import base.*

// TESTCASE NUMBER: 2
fun case2() {
    konst a = Case1()
    a.<!INVISIBLE_REFERENCE!>priv<!>()
    a.<!INVISIBLE_REFERENCE!>prot<!>()
    a.<!INVISIBLE_REFERENCE!>int<!>()
    a.pub()

    a.<!INVISIBLE_REFERENCE!>priv1<!>
    a.<!INVISIBLE_REFERENCE!>prot1<!>
    a.<!INVISIBLE_REFERENCE!>int1<!>
    a.pub1
}
