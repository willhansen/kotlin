// !LANGUAGE: +JavaSamConversionEqualsHashCode
// TARGET_BACKEND: JVM_IR
// FULL_JDK

fun box(): String {
    konst lambda: () -> Unit = { }
    konst r1 = Runnable(lambda)
    konst r2 = Runnable(lambda)

    if (r1 != r2)
        return "r1 != r2"
    if (r1.hashCode() != r2.hashCode())
        return "r1.hashCode() != r2.hashCode()"

    return "OK"
}
