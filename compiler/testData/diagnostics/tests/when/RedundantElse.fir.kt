/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-152
 * PRIMARY LINKS: expressions, when-expression -> paragraph 5 -> sentence 1
 * expressions, when-expression, exhaustive-when-expressions -> paragraph 2 -> sentence 3
 * expressions, when-expression, exhaustive-when-expressions -> paragraph 2 -> sentence 6
 * expressions, when-expression, exhaustive-when-expressions -> paragraph 2 -> sentence 9
 * expressions, when-expression -> paragraph 6 -> sentence 10
 * expressions, when-expression -> paragraph 6 -> sentence 11
 */

// FILE: MyEnum.java
public enum MyEnum {
    SINGLE;

    public static MyEnum getInstance() {
        return SINGLE;
    }
}


// FILE: test.kt

sealed class X {
    class A : X()
    class B : X()
}

fun foo(x: X) = when (x) {
    is X.A -> {}
    is X.B -> {}
    else -> {}
}

fun bar(x: X?): String = when (x) {
    is X.A -> "A"
    is X.B -> "B"
    null -> "null"
    else -> "Unreachable"
}

fun justUse(x: X) {
    when (x) {
        is X.A -> {}
        is X.B -> {}
        // Redundant even in statement position
        else -> {}
    }
}

enum class E {
    A, B
}

fun foo(e: E): String = when (e) {
    E.A -> "A"
    E.B -> "B"
    else -> ""
}

fun bar(e: E?): String = when (e) {
    E.A -> "A"
    E.B -> "B"
    else -> "" // no warning
}

fun foo(b: Boolean) = when (b) {
    true -> 1
    false -> 0
    else -> -1
}

fun useJava(): String {
    konst me = MyEnum.getInstance()
    return when (me) {
        MyEnum.SINGLE -> "OK"
        else -> "FAIL" // no warning
    }
}
