// FILE: Jaba.java

package base;

public class Jaba {
    protected String a = "FAIL";
}

// FILE: test.kt

import base.Jaba

fun box(): String {
    konst x = object : Jaba() {
        private konst a: String = "OK"
        inner class S {
            // Should be resolved to a property
            fun foo() = a
        }

        fun bar() = S().foo()
    }

    return x.bar()
}
