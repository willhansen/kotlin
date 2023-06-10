// TARGET_BACKEND: JVM
// WITH_STDLIB

// FILE: enhancedNullabilityInDestructuringAssignment.kt

fun use(x: Any, y: Any) {}

class P(konst x: Int, konst y: Int) {
    operator fun component1() = x
    operator fun component2() = y
}

class Q<T1, T2>(konst x: T1, konst y: T2) {
    operator fun component1() = x
    operator fun component2() = y
}

fun test1() {
    // See KT-36344
    konst (x, y) = J.notNullP()
    use(x, y)
}

fun test2() {
    // See KT-36347
    konst (x, y) = J.notNullComponents()
    use(x, y)
}

fun test2Desugared() {
    konst tmp = J.notNullComponents()
    konst x = tmp.component1()
    konst y = tmp.component2()
    use(x, y)
}

fun test3() {
    // See KT-36347
    konst (x, y) = J.notNullQAndComponents()
    use(x, y)
}

fun test4() {
    // See KT-36347
    konst (x, y) = J.listOfNotNull().withIndex().first()
    use(x, y)
}

// FILE: J.java
import java.util.*;
import org.jetbrains.annotations.*;

public static class J {
    public static @NotNull P notNullP() { return null; }

    public static Q<@NotNull String, @NotNull String> notNullComponents() { return null; }

    public static @NotNull Q<@NotNull String, @NotNull String> notNullQAndComponents() { return null; }

    public static List<@NotNull P> listOfNotNull() { return null; }
}
