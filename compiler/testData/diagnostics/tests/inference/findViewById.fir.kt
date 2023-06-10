// !LANGUAGE: +ExpectedTypeFromCast
// !DIAGNOSTICS: -UNUSED_VARIABLE -DEBUG_INFO_LEAKING_THIS

// FILE: a/View.java
package a;

public class View {

}

// FILE: a/Test.java
package a;

public class Test {
    public <T extends View> T findViewById(int id);
}

// FILE: 1.kt
package a


class X : View()

class Y<T> : View()

konst xExplicit: X = Test().findViewById(0)
konst xCast = Test().findViewById(0) as X

konst xCastExplicitType = Test().findViewById<X>(0) as X
konst xSafeCastExplicitType = Test().findViewById<X>(0) <!USELESS_CAST!>as? X<!>

konst yExplicit: Y<String> = Test().findViewById(0)
konst yCast = Test().findViewById(0) as Y<String>


class TestChild : Test() {
    konst xExplicit: X = findViewById(0)
    konst xCast = findViewById(0) as X

    konst yExplicit: Y<String> = findViewById(0)
    konst yCast = findViewById(0) as Y<String>
}

fun test(t: Test) {
    konst xExplicit: X = t.findViewById(0)
    konst xCast = t.findViewById(0) as X

    konst yExplicit: Y<String> = t.findViewById(0)
    konst yCast = t.findViewById(0) as Y<String>
}

fun test2(t: Test?) {
    konst xSafeCallSafeCast = t?.findViewById(0) <!USELESS_CAST!>as? X<!>
    konst xSafeCallSafeCastExplicitType = t?.findViewById<X>(0) <!USELESS_CAST!>as? X<!>

    konst xSafeCallCast = t?.findViewById(0) as X
    konst xSafeCallCastExplicitType = t<!UNNECESSARY_SAFE_CALL!>?.<!>findViewById<X>(0) as X
}
