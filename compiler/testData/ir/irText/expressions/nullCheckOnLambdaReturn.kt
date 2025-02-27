// TARGET_BACKEND: JVM
// FILE: nullCheckOnLambdaReturn.kt
fun checkAny(fn: () -> Any) = fn()

fun checkAnyN(fn: () -> Any?) = fn()

fun <T> id(x: T) = x

fun test1() = checkAny { J.foo() }

konst test2: () -> Any = { J.foo() }

konst test3: () -> Any = { J.foo() } as () -> Any

konst test4: () -> Any = id { J.foo() }

fun test5() = checkAnyN { J.foo() }

fun test6() = checkAnyN { J.nnFoo() }

// FILE: J.java
import org.jetbrains.annotations.*;

public class J {
    public static String foo() { return null; }

    public static @NotNull String nnFoo() { return null; }
}