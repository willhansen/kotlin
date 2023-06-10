open class A {
    open var test: Number = 10
}

open class B : <!SUPERTYPE_NOT_INITIALIZED!>A<!> {
    override var test: <!VAR_TYPE_MISMATCH_ON_OVERRIDE!>Double<!> = 20.0
}

class C() : A() {
    override var test: <!VAR_TYPE_MISMATCH_ON_OVERRIDE!>String<!> = "Test"
}

open class D() : B() {
    override var test: <!VAR_TYPE_MISMATCH_ON_OVERRIDE!>Char<!> = '\n'
}

class E<T : <!FINAL_UPPER_BOUND!>Double<!>>(konst konstue: T) : B() {
    override var test: <!VAR_TYPE_MISMATCH_ON_OVERRIDE!>T<!> = konstue
}

open class F<T : Number>(konst konstue: T) {
    open var rest: T = konstue
}

class G<E : <!FINAL_UPPER_BOUND!>Double<!>>(konst balue: E) : F<E>(balue) {
    override var rest: E = balue
}

class H<E : <!FINAL_UPPER_BOUND!>String<!>>(konst balue: E) : F<<!UPPER_BOUND_VIOLATED!>E<!>>(<!ARGUMENT_TYPE_MISMATCH!>balue<!>) {
    override var rest: E = balue // no report because of INAPPLICABLE_CANDIDATE
}

class M<E : <!FINAL_UPPER_BOUND!>String<!>>(konst balue: E) : F<Double>(3.14) {
    override var rest: <!VAR_TYPE_MISMATCH_ON_OVERRIDE!>E<!> = balue
}
