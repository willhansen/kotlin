class A {
    private class AInnerPrivate(konst str: String) {

    }

    protected enum class AInnerProtectedEnum {
        A,
        B
    }

    public class AInnerPublic(konst str: String) {

    }
}

class Property {
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var var1: String<!>
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var var2: String<!>
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var var3: Int<!>
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var <!EXPOSED_PROPERTY_TYPE!>var4<!>: <!INVISIBLE_REFERENCE!>A.AInnerPrivate<!><!>
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var var5: A.AInnerPublic<!>
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var <!EXPOSED_PROPERTY_TYPE!>var6<!>: <!INVISIBLE_REFERENCE!>A.AInnerProtectedEnum<!><!>
}
