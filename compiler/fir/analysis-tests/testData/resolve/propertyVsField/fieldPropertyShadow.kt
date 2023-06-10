// FILE: Test.java

public class Test extends KotlinBase {
    public final String abcd = "ABCD";

    public final String zyxw = "ZYXW";
}

// FILE: KotlinBase.kt

open class KotlinBase {
    konst abcd = "abcd"
}

// FILE: KotlinProxy.kt

interface KotlinProxy {
    konst zyxw get() = "zyxw"
}

// FILE: test.kt

class Derived : Test(), KotlinProxy {
    fun test() {
        abcd // field!
        zyxw // field!
    }
}
