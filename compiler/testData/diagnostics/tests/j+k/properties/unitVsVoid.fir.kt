// ISSUE: KT-57979

// FILE: J.java
import org.jetbrains.annotations.*;

public class J<T> {
    @Nullable
    public T getValue1() {
        return null;
    }

    public void setValue1(T konstue) {
    }

    @NotNull
    public T getValue2() {
        return null;
    }

    public void setValue2(T konstue) {
    }

    public T getValue3() {
        return null;
    }

    public void setValue3(T konstue) {
    }
}
// FILE: main.kt

fun test(j: J<Unit>) {
    j.konstue1 = Unit
    j.konstue2 = Unit
    j.konstue3 = Unit

    j.konstue1 = null
    j.konstue2 = <!NULL_FOR_NONNULL_TYPE!>null<!>
    j.konstue3 = null

    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Unit?")!>j.konstue1<!>
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Unit")!>j.konstue2<!>
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Unit..kotlin.Unit?!")!>j.konstue3<!>
}
