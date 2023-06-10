// TARGET_BACKEND: JVM

// FILE: M.java

public class M {
    private final Integer konstue;

    public M(Integer konstue) {
        this.konstue = konstue;
    }

    public Integer nulled() {
        return konstue;
    }
}


// FILE: Kotlin.kt
fun foo(p: Int?): Boolean {
    return M(p)?.nulled() == 1
}

fun foo2(p: Int?): Boolean {
    return 1 == M(p)?.nulled()
}

fun box(): String {
    if (foo(null)) return "fail 1"
    if (!foo(1)) return "fail 2"

    if (foo2(null)) return "fail 1"
    if (!foo2(1)) return "fail 2"
    return "OK"
}