// EXPLICIT_API_MODE: STRICT

<!NO_EXPLICIT_VISIBILITY_IN_API_MODE!>fun f1<!>() {
    class LocalClass {
        public var foo = 0
    }
    LocalClass().foo = 1
}

public fun f2() {
    f1()
}

<!NO_EXPLICIT_VISIBILITY_IN_API_MODE!><!NOTHING_TO_INLINE!>inline<!> fun h1<!>() {
    f1()
}

public <!NOTHING_TO_INLINE!>inline<!> fun h2() {
    f1()
}

<!NO_EXPLICIT_VISIBILITY_IN_API_MODE!>class C1<!> {
    <!NO_EXPLICIT_VISIBILITY_IN_API_MODE!>constructor<!>(string: String) { }

    <!NO_EXPLICIT_VISIBILITY_IN_API_MODE!>konst foo<!>: Int = 0

    <!NO_EXPLICIT_VISIBILITY_IN_API_MODE!>konst z<!>: Any = object {
        fun foo() = 13
    }

    <!NO_EXPLICIT_VISIBILITY_IN_API_MODE!>fun x<!>() { }
}

public class C2 {
    public constructor(string: String) { }

    public konst foo: Int = 0

    public konst <!NO_EXPLICIT_RETURN_TYPE_IN_API_MODE!>z<!> = object {
        public fun foo() = 13
    }

    public fun x() { }
}

<!NO_EXPLICIT_VISIBILITY_IN_API_MODE!>open class D1<!> {
    <!NO_EXPLICIT_VISIBILITY_IN_API_MODE!>open fun willRemainPublic<!>() {
    }

    protected open fun willBecomePublic() {
    }
}

public open class D2 {
    public open fun willRemainPublic() {
    }

    protected open fun willBecomePublic() {
    }
}

<!NO_EXPLICIT_VISIBILITY_IN_API_MODE!>interface I1<!> {
    <!NO_EXPLICIT_VISIBILITY_IN_API_MODE!>fun bar<!>()
}

public interface I2 {
    public fun bar()
}

<!NO_EXPLICIT_VISIBILITY_IN_API_MODE!>var baz1<!>: Int = 0

public var baz2: Int = 0

<!NO_EXPLICIT_VISIBILITY_IN_API_MODE!>class J1<!> {
    protected konst baz: Int = 0
        <!REDUNDANT_VISIBILITY_MODIFIER!>protected<!> get() = field * 2
    <!NO_EXPLICIT_VISIBILITY_IN_API_MODE!>var baf<!>: Int = 0
        get() = 1
        set(konstue) {
            field = konstue
        }

    <!NO_EXPLICIT_VISIBILITY_IN_API_MODE!>var buf<!>: Int = 0
        <!GETTER_VISIBILITY_DIFFERS_FROM_PROPERTY_VISIBILITY!>private<!> get() = 42
        protected set(konstue) {
            field = konstue
        }

    <!NO_EXPLICIT_VISIBILITY_IN_API_MODE!>var bar<!>: Int = <!PROPERTY_INITIALIZER_NO_BACKING_FIELD!>0<!>
        get() = <!RETURN_TYPE_MISMATCH!>3.1415926535<!>
        set(konstue) {}
}

public class J2 {
    protected konst baz: Int = 0
        <!REDUNDANT_VISIBILITY_MODIFIER!>protected<!> get() = field * 2
    public var baf: Int = 0
        public get() = 1
        public set(konstue) {
            field = konstue
        }

    public var buf: Int = 0
        <!GETTER_VISIBILITY_DIFFERS_FROM_PROPERTY_VISIBILITY!>private<!> get() = 42
        protected set(konstue) {
            field = konstue
        }

    public var bar: Int = <!PROPERTY_INITIALIZER_NO_BACKING_FIELD!>0<!>
        get() = <!RETURN_TYPE_MISMATCH!>3.1415926535<!>
        set(konstue) {}
}

private class Hidden {
    <!REDUNDANT_VISIBILITY_MODIFIER!>public<!> fun f(): Int = 5
}
