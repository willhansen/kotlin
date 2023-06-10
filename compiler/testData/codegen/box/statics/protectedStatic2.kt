// TARGET_BACKEND: JVM

// FILE: Base.java

public class Base {

    protected static String BASE_ONLY = "BASE";

    protected static String baseOnly() {
        return BASE_ONLY;
    }

    protected static String TEST = "BASE";

    protected static String test() {
        return TEST;
    }

    public static class Derived extends Base {
        protected static String TEST = "DERIVED";

        protected static String test() {
            return TEST;
        }
    }
}

// FILE: Kotlin.kt

package anotherPackage

import Base.Derived
import Base

fun <T> ekonst(fn: () -> T) = fn()

class Kotlin : Base.Derived() {
    fun doTest(): String {

        if (ekonst { TEST } != "DERIVED") return "fail 1"
        if (ekonst { test() } != "DERIVED") return "fail 2"

        if (ekonst { Derived.TEST } != "DERIVED") return "fail 3"
        if (ekonst { Derived.test() } != "DERIVED") return "fail 4"

        if (ekonst { Base.TEST } != "BASE") return "fail 5"
        if (ekonst { Base.test() } != "BASE") return "fail 6"

        if (ekonst { Base.BASE_ONLY } != "BASE") return "fail 7"
        if (ekonst { Base.baseOnly() } != "BASE") return "fail 8"

        if (ekonst { BASE_ONLY } != "BASE") return "fail 9"
        if (ekonst { baseOnly() } != "BASE") return "fail 10"

        return "OK"
    }
}

fun box(): String {
    return Kotlin().doTest()
}
