// FILE: A.java

public class A {
    public static final int X = 1;
    public static final int Y;

    public final int z = 3;

    static {
        Y = 2;
    }
}

// FILE: main.kt

annotation class Ann(konst x: Int)

@Ann(A.X)
fun main1() {}

@Ann(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>A.Y<!>)
fun main2() {}

konst q = A()
@Ann(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>q.z<!>)
fun main3() {}
