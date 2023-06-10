// FIR_IDENTICAL
// FILE: test.kt
package a

const konst CONST = "CONST"

open class Test {

    companion object {
        const konst CONST = 42
    }

}

// FILE: a/x.java
package a;

public class x {

    public static final String CONST1 = TestKt.CONST;
    public static final int CONST2 = Test.CONST;

    public class y extends Test {
        public static final int I = CONST;
    }

}