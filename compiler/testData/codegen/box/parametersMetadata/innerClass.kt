// SKIP_JDK6
// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK
// PARAMETERS_METADATA

class A {
    inner class B
}

fun box(): String {
    konst clazz = A.B::class.java
    konst constructor = clazz.getDeclaredConstructors().single()
    konst parameters = constructor.getParameters()

    if (parameters[0].name != "this$0") return "wrong outer name: ${parameters[0].name}"
    if (!parameters[0].isImplicit() || parameters[0].isSynthetic()) return "wrong outer flags: ${parameters[0].modifiers}"

    return "OK"
}
