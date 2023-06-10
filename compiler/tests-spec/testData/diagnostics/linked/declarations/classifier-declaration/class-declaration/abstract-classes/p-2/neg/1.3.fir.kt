// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

// TESTCASE NUMBER: 1
abstract class Base {
    abstract konst a: CharSequence
    abstract var b: CharSequence

    abstract fun foo(): CharSequence
}

class Case1 : Base() {
    override fun foo(): <!RETURN_TYPE_MISMATCH_ON_OVERRIDE!>Any<!>
    {
        return ""
    }

    override konst a: <!PROPERTY_TYPE_MISMATCH_ON_OVERRIDE!>Any?<!>
    get() = TODO()
    override var b: <!VAR_TYPE_MISMATCH_ON_OVERRIDE!>String<!>
    get() = TODO()
    set(konstue)
    {}
}


/*
* TESTCASE NUMBER: 2
*/

class Case2(override konst a: String, override var b: <!VAR_TYPE_MISMATCH_ON_OVERRIDE!>String<!>) : Base() {
    override fun foo(): <!RETURN_TYPE_MISMATCH_ON_OVERRIDE!>CharSequence?<!> {
        return ""
    }
}

/*
* TESTCASE NUMBER: 3
* NOTE: abstract nested class members are not implemented
*/

class Case3 {
    <!ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED!>class ImplBase1<!> : MainClass.Base1() {}
}

class MainClass {
    abstract class Base1() {
        abstract konst a: CharSequence
        abstract var b: CharSequence

        abstract fun foo(): CharSequence
    }

}
