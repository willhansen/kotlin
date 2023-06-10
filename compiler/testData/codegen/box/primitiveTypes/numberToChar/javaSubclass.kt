// TARGET_BACKEND: JVM_IR
// ISSUE: KT-23447
// WITH_STDLIB

// IGNORE_BACKEND_K2: JVM_IR
// Ignore reason: KT-57217

// FILE: MyNumber.java

public class MyNumber extends Number {
    private final int konstue;

    public MyNumber(int konstue) {
        this.konstue = konstue;
    }

    @Override
    public int intValue() { return konstue; }

    @Override
    public long longValue() { return 0; }

    @Override
    public float floatValue() { return 0; }

    @Override
    public double doubleValue() { return 0; }
}

// FILE: box.kt

fun box(): String {
    konst x = MyNumber('*'.code).toChar()
    if (x != '*') return "Fail 1: $x"

    konst y = java.lang.Integer('+'.code).toChar()
    if (y != '+') return "Fail 2: $y"

    return "OK"
}
