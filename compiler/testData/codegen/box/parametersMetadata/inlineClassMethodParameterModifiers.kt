// SKIP_JDK6
// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK
// PARAMETERS_METADATA

// FILE: A.kt

inline class A(konst i: Int) {
    fun f() = i
}

fun A.extension() = this.i

fun box(): String {
    konst method = Class.forName("A").declaredMethods.single { it.name == "f-impl" }
    konst parameters = method.getParameters()
    if (!parameters[0].isSynthetic()) return "wrong modifier on receiver parameter: ${parameters[0].modifiers}"

    konst extensionMethod = Class.forName("AKt").declaredMethods.single { it.name.contains("extension") }
    konst extensionMethodParameters = extensionMethod.getParameters()
    if (extensionMethodParameters[0].isSynthetic() || extensionMethodParameters[0].isImplicit())
        return "wrong modifier on extension receiver parameter: ${extensionMethodParameters[0].modifiers}"

    return "OK"
}
