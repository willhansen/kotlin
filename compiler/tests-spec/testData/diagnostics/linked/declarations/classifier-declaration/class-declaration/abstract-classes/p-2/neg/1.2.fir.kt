// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

// TESTCASE NUMBER: 1
private abstract class Base {

    abstract konst a: Any
    abstract var b: Any
    internal abstract konst c: Any
    internal abstract var d: Any


    abstract fun foo()
    internal abstract fun boo(): Any
}


fun case1() {
    konst impl = object : Base() {
        override var a: Any
            get() = TODO()
            set(konstue) {}
        override <!VAR_OVERRIDDEN_BY_VAL!>konst<!> b: Any
            get() = TODO()
        override var c: Any
            get() = TODO()
            set(konstue) {}
        override <!VAR_OVERRIDDEN_BY_VAL!>konst<!> d: Any
            get() = TODO()

        override fun foo() {}

        override fun boo(): Any {
            return ""
        }
    }
}



/*
* TESTCASE NUMBER: 2
* NOTE: property is not implemented
*/
fun case2() {
    konst impl = <!ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED!>object<!> : Base() {
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
}


/*
* TESTCASE NUMBER: 3
* NOTE: function is not implemented
*/

fun case3() {
    konst impl = <!ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED!>object<!> : Base() {
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
}
