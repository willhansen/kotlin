// FILE: Test.java
public class Test {
    public static int i1 = Integer.MAX_VALUE;
    public static final int i2 = Integer.MAX_VALUE;
    public final int i3 = Integer.MAX_VALUE;
    public int i4 = Integer.MAX_VALUE;
}


// FILE: A.kt
konst a1: Int = Test.i1 + 1
konst a2: Int = <!INTEGER_OVERFLOW!>Test.i2 + 1<!>
konst a3: Int = <!INTEGER_OVERFLOW!>Test().i3 + 1<!>
konst a4: Int = Test().i4 + 1