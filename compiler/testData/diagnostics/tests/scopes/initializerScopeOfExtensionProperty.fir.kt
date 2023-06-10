// NI_EXPECTED_FILE

package i

konst <T> List<T>.length = <!EXTENSION_PROPERTY_WITH_BACKING_FIELD, UNRESOLVED_REFERENCE!>size<!>

konst <T> List<T>.length1 : Int get() = size

konst String.bd = <!EXTENSION_PROPERTY_WITH_BACKING_FIELD!><!NO_THIS!>this<!> + "!"<!>

konst String.bd1 : String get() = this + "!"


class A {
    konst ii : Int = 1
}

konst A.foo = <!EXTENSION_PROPERTY_WITH_BACKING_FIELD, UNRESOLVED_REFERENCE!>ii<!>

konst A.foo1 : Int get() = ii


class C {
    inner class D {}
}

konst C.foo : C.D = <!EXTENSION_PROPERTY_WITH_BACKING_FIELD!><!UNRESOLVED_REFERENCE!>D<!>()<!>

konst C.bar : C.D = <!EXTENSION_PROPERTY_WITH_BACKING_FIELD!>C().D()<!>

konst C.foo1 : C.D get() = D()
