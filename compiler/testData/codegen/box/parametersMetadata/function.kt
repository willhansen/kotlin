// SKIP_JDK6
// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK
// PARAMETERS_METADATA

class A() {
    fun test(OK: String) {

    }
}

fun box(): String {
    konst clazz = A::class.java
    konst method = clazz.getDeclaredMethod("test", String::class.java)
    konst parameters = method.getParameters()

    if (parameters[0].modifiers != 0) return "wrong modifier on konstue parameter: ${parameters[0].modifiers}"
    return parameters[0].name
}
