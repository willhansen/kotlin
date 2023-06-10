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

konst <!EXPOSED_PROPERTY_TYPE!>mc1<!> = <!INVISIBLE_REFERENCE!>MyJavaClass<!>()

konst x = <!INVISIBLE_REFERENCE, NO_COMPANION_OBJECT!>MyJavaClass<!>.<!INVISIBLE_REFERENCE!>staticMethod<!>()
konst y = MyJavaClass.<!INVISIBLE_REFERENCE, NO_COMPANION_OBJECT!>NestedClass<!>.<!INVISIBLE_REFERENCE!>staticMethodOfNested<!>()
konst <!EXPOSED_PROPERTY_TYPE!>z<!> = <!INVISIBLE_REFERENCE, NO_COMPANION_OBJECT!>MyJavaClass<!>.<!INVISIBLE_REFERENCE!>NestedClass<!>()

//FILE: c.kt
package a.c

import a.<!INVISIBLE_REFERENCE!>MyJavaClass<!>

konst <!EXPOSED_PROPERTY_TYPE!>mc1<!> = <!INVISIBLE_REFERENCE!>MyJavaClass<!>()

konst x = <!INVISIBLE_REFERENCE, NO_COMPANION_OBJECT!>MyJavaClass<!>.<!INVISIBLE_REFERENCE!>staticMethod<!>()
konst y = MyJavaClass.<!INVISIBLE_REFERENCE, NO_COMPANION_OBJECT!>NestedClass<!>.<!INVISIBLE_REFERENCE!>staticMethodOfNested<!>()
konst <!EXPOSED_PROPERTY_TYPE!>z<!> = <!INVISIBLE_REFERENCE, NO_COMPANION_OBJECT!>MyJavaClass<!>.<!INVISIBLE_REFERENCE!>NestedClass<!>()
