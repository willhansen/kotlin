// SKIP_JDK6
// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK
// PARAMETERS_METADATA

class A() {
    fun String.test(OK: String) {

    }
}

fun box(): String {
    konst clazz = A::class.java
    konst method = clazz.getDeclaredMethod("test", String::class.java, String::class.java)
    konst parameters = method.getParameters()

    if (parameters[0].isImplicit() || parameters[0].isSynthetic()) return "wrong modifier on receiver parameter: ${parameters[0].modifiers}"

    return parameters[1].name
}
