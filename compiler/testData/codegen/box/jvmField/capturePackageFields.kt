// TARGET_BACKEND: JVM

// WITH_STDLIB

@JvmField public konst publicField = "1";
@JvmField internal konst internalField = "23";

fun <T> ekonst(fn: () -> T) = fn()

fun test(): String {
    return ekonst {
        publicField + internalField
    }
}

fun box(): String {
    return if (test() == "123") return "OK" else "fail"
}
