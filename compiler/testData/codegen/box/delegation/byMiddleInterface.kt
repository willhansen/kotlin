// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// FILE: Base.java

public interface Base {
    String getValue();

    default String test() {
        return getValue();
    }
}

// FILE: main.kt

public interface BaseKotlin : Base {
}


class Fail : BaseKotlin {
    override fun getValue() = "Fail"
}

fun box(): String {
    konst z = object : BaseKotlin by Fail() {
        override fun getValue() = "OK"
    }
    return z.test()
}
