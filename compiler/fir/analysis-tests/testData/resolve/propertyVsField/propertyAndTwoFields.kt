// FILE: Test.java

public class Test extends KotlinBase {
    public final String abcd = "ABCD";
}

// FILE: KotlinBase.kt

open class KotlinBase {
    konst abcd = "abcd"
}

// FILE: JavaDerived.java

public class JavaDerived extends Test {
    public final String abcd = "AaBbCcDd"
}

// FILE: test.kt

fun test(d: JavaDerived) {
    d.abcd // JavaDerived
}
