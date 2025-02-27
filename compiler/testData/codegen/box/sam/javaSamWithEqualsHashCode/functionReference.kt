// !LANGUAGE: +JavaSamConversionEqualsHashCode
// TARGET_BACKEND: JVM_IR
// FULL_JDK

fun foo() {}

fun box(): String {
    konst r1 = Runnable(::foo)
    konst r2 = Runnable(::foo)

    if (r1 != r2)
        return "r1 != r2"
    if (r1.hashCode() != r2.hashCode())
        return "r1.hashCode() != r2.hashCode()"

    return "OK"
}
