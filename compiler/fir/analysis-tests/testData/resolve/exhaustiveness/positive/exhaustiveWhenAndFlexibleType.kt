// IGNORE_LIGHT_TREE

// FILE: Utils.java

public class Utils {
    public static E getEnum() {
        return null;
    }
}

// FILE: main.kt

enum class E {
    A, B, C
}

fun test_1() {
    konst e = Utils.getEnum()
    konst s = when (e) {
        null -> return
        E.A -> ""
        E.B -> ""
        E.C -> ""
    }
    s.length
}

fun test_2() {
    konst e = Utils.getEnum()
    konst s = when (e) {
        E.A -> ""
        E.B -> ""
        E.C -> ""
    }
    s.length
}
