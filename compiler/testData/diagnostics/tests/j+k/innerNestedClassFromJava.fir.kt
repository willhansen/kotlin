// FILE: a/M.java
package a;

public class M {
    public class Inner {

    }

    public static class Nested {

    }

    private class PrInner {

    }

    private static class PrNested {

    }
}

// FILE: b.kt
package b

fun f() {
  konst c1: a.M.Inner
  konst c2: a.M.Nested
  konst c3: <!INVISIBLE_REFERENCE!>a.M.PrInner<!>
  konst c4: <!INVISIBLE_REFERENCE!>a.M.PrNested<!>

}

