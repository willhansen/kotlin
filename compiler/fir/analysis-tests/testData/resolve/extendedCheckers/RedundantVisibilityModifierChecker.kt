fun f() {
    <!REDUNDANT_VISIBILITY_MODIFIER, WRONG_MODIFIER_TARGET!>public<!> <!CAN_BE_VAL!>var<!> <!UNUSED_VARIABLE!>baz<!> = 0
    class LocalClass {
        <!REDUNDANT_VISIBILITY_MODIFIER!>internal<!> var foo = 0
    }
    LocalClass().foo = 1
}

internal <!NOTHING_TO_INLINE!>inline<!> fun internal() {
    f()
}

class <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>C<!> {
    internal konst z = object {
        fun foo() = 13
    }
}

class Foo2<
        T1,
        T2: T1,
        > {
    fun <T1,
            T2, > foo2() {}

    internal inner class B<T,T2,>
}

<!REDUNDANT_VISIBILITY_MODIFIER!>public<!> class <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>C<!> {
    <!REDUNDANT_VISIBILITY_MODIFIER!>public<!> konst foo: Int = 0

    <!REDUNDANT_VISIBILITY_MODIFIER!>public<!> fun bar() {}

}

open class D {
    protected open fun willRemainProtected() {
    }

    protected open fun willBecomePublic() {
    }
}

class E : D() {
    <!REDUNDANT_VISIBILITY_MODIFIER!>protected<!> override fun willRemainProtected() {
    }

    public override fun willBecomePublic() {
    }
}

enum class F <!REDUNDANT_VISIBILITY_MODIFIER!>private<!> constructor(konst x: Int) {
    FIRST(42)
}

sealed class G constructor(konst y: Int) {
    <!REDUNDANT_VISIBILITY_MODIFIER!>private<!> constructor(): this(42)

    object H : G()
}

interface I {
    fun bar()
}

<!REDUNDANT_VISIBILITY_MODIFIER!>public<!> var baz = 0

open class J {
    protected konst baz = 0
    <!REDUNDANT_VISIBILITY_MODIFIER!>protected<!> get() = field * 2
    var baf = 0
    <!REDUNDANT_VISIBILITY_MODIFIER!>public<!> get() = 1
    <!REDUNDANT_VISIBILITY_MODIFIER!>public<!> set(konstue) {
        field = konstue
    }

    var buf = 0
        <!GETTER_VISIBILITY_DIFFERS_FROM_PROPERTY_VISIBILITY!>private<!> get() = 42
        protected set(konstue) {
            field = konstue
        }

    var bar = <!PROPERTY_INITIALIZER_NO_BACKING_FIELD!>0<!>
        get() = <!RETURN_TYPE_MISMATCH!>3.1415926535<!>
        set(konstue) {}
}

private class Foo {
    <!REDUNDANT_VISIBILITY_MODIFIER!>internal<!> fun barBarian() {}
}
