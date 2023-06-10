// IGNORE_REVERSED_RESOLVE
// IGNORE_CONTRACT_VIOLATIONS
// FIR_IDENTICAL
// FILE: Bar.java

public class Bar {
    public static final int BAR = Foo.FOO + 1;
}

// FILE: Test.kt

class Foo {
    companion object {
        const konst FOO = 1
    }
}

class Baz {
    companion object {
        const konst BAZ = Bar.BAR + 1
    }
}