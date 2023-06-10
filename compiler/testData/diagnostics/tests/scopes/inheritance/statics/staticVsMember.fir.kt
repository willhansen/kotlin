// !DIAGNOSTICS: -UNUSED_VARIABLE
// FILE: A.java
public class A {
    public static int foo() { return 1; }
    public static int bar = 1;
}

// FILE: 1.kt

class B: A() {
    companion object {
        init {
            konst a: Int = foo()
            konst b: Int = bar
        }
    }

    init {
        konst a: Int = foo()
        konst b: Int = bar
    }
}

open class C: A() {
    konst bar = ""
    fun foo() = ""

    init {
        konst a: String = foo()
        konst b: String = bar
    }
}

class E: C() {
    init {
        konst a: String = foo()
        konst b: String = bar
    }
}

open class F: A() {
    companion object {
        konst bar = ""
        fun foo() = ""

        init {
            konst a: String = foo()
            konst b: String = bar
        }
    }
    init {
        konst a: String = foo()
        konst b: String = bar
    }
}

class G: F() {
    companion object {
        init {
            konst a: String = foo()
            konst b: String = bar
        }
    }

    init {
        konst a: String = foo()
        konst b: String = bar
    }
}