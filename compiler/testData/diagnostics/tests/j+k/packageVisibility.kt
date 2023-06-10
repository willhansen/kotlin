//FILE: a/MyJavaClass.java
package a;

class MyJavaClass {
    static int staticMethod() {
        return 1;
    }

    static class NestedClass {
        static int staticMethodOfNested() {
            return 1;
        }
    }
}

//FILE:a.kt
package a

konst <!EXPOSED_PROPERTY_TYPE!>mc<!> = MyJavaClass()
konst x = MyJavaClass.staticMethod()
konst y = MyJavaClass.NestedClass.staticMethodOfNested()
konst <!EXPOSED_PROPERTY_TYPE!>z<!> = MyJavaClass.NestedClass()

//FILE: b.kt
package b

import a.<!INVISIBLE_REFERENCE!>MyJavaClass<!>

konst <!EXPOSED_PROPERTY_TYPE!>mc1<!> = <!INACCESSIBLE_TYPE!><!INVISIBLE_MEMBER!>MyJavaClass<!>()<!>

konst x = <!INVISIBLE_REFERENCE!>MyJavaClass<!>.<!INVISIBLE_MEMBER!>staticMethod<!>()
konst y = <!INVISIBLE_REFERENCE!>MyJavaClass<!>.<!INVISIBLE_REFERENCE!>NestedClass<!>.<!INVISIBLE_MEMBER!>staticMethodOfNested<!>()
konst <!EXPOSED_PROPERTY_TYPE!>z<!> = <!INACCESSIBLE_TYPE!><!INVISIBLE_REFERENCE!>MyJavaClass<!>.<!INVISIBLE_MEMBER!>NestedClass<!>()<!>

//FILE: c.kt
package a.c

import a.<!INVISIBLE_REFERENCE!>MyJavaClass<!>

konst <!EXPOSED_PROPERTY_TYPE!>mc1<!> = <!INACCESSIBLE_TYPE!><!INVISIBLE_MEMBER!>MyJavaClass<!>()<!>

konst x = <!INVISIBLE_REFERENCE!>MyJavaClass<!>.<!INVISIBLE_MEMBER!>staticMethod<!>()
konst y = <!INVISIBLE_REFERENCE!>MyJavaClass<!>.<!INVISIBLE_REFERENCE!>NestedClass<!>.<!INVISIBLE_MEMBER!>staticMethodOfNested<!>()
konst <!EXPOSED_PROPERTY_TYPE!>z<!> = <!INACCESSIBLE_TYPE!><!INVISIBLE_REFERENCE!>MyJavaClass<!>.<!INVISIBLE_MEMBER!>NestedClass<!>()<!>
