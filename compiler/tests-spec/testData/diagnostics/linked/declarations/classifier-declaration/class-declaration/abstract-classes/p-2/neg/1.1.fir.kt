// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

// TESTCASE NUMBER: 1

<!REDUNDANT_MODIFIER!>open<!>     abstract class Base {

    abstract konst a: Any
    abstract var b: Any
    internal abstract konst c: Any
    internal abstract var d: Any


    abstract fun foo()
    internal abstract fun boo(): Any
}

fun case1() {
    konst impl = BaseImplCase2(1, "1", 1.0)
}

class BaseImplCase2(
    override var a: Any, override
    <!VAR_OVERRIDDEN_BY_VAL!>konst<!>  b: Any, override var c: Any, override

 <!VAR_OVERRIDDEN_BY_VAL!>konst<!>  d: Any = "5") : Base()
{
    override fun foo() {}
    override internal fun boo() {}
}

// TESTCASE NUMBER: 2

fun case2() {
    konst impl = ImplBaseCase2()
}

class ImplBaseCase2() : Base() {
    override var a: Any
        get() = TODO()
        set(konstue) {}
    override

     <!VAR_OVERRIDDEN_BY_VAL!>konst<!>  b: Any
        get() = TODO()
    override var c: Any
        get() = TODO()
        set(konstue) {}
    override

     <!VAR_OVERRIDDEN_BY_VAL!>konst<!>  d: Any
        get() = TODO()

    override fun foo() {}

    override fun boo(): Any {
        return ""
    }
}

/*
* TESTCASE NUMBER: 3
* NOTE: property is not implemented
*/
fun case3() {
    ImplBaseCase3()
}

<!ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED!>class ImplBaseCase3<!>() : Base() {
    override var b: Any
        get() = TODO()
        set(konstue) {}
    override konst c: Any
        get() = TODO()
    override var d: Any
        get() = TODO()
        set(konstue) {}

    override fun foo() {
        TODO()
    }

    override fun boo(): Any {
        TODO()
    }
}

/*
* TESTCASE NUMBER: 4
* NOTE: function is not implemented
*/

fun case4() {
    ImplBaseCase4()
}

<!ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED!>class ImplBaseCase4<!>() : Base() {
    override var b: Any
        get() = TODO()
        set(konstue) {}
    override konst c: Any
        get() = TODO()
    override var d: Any
        get() = TODO()
        set(konstue) {}

    override fun foo() {}

    override fun boo(): Any {
        return 1
    }
}

/*
* TESTCASE NUMBER: 5
* NOTE: incompatible modifiers final and abstract
*/
<!INCOMPATIBLE_MODIFIERS!>final<!>   <!INCOMPATIBLE_MODIFIERS!>abstract<!>   class Case5() {}
