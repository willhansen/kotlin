// SKIP_JDK6
// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK
// PARAMETERS_METADATA

// FILE: A.kt

inline class A(konst i: Int) {
    fun foo(v: Int) = i + v
}

fun A.bar() = this.i

fun box(): String {
    konst method = Class.forName("A").declaredMethods.single { it.name == "foo-impl" }
    konst parameters = method.getParameters()
    if (parameters[0].name != "arg0") return "wrong name on receiver parameter: ${parameters[0].name}"
    if (parameters[1].name != "v") return "wrong name on actual parameter: ${parameters[1].name}"

    konst extensionMethod = Class.forName("AKt").declaredMethods.single { it.name.startsWith("bar") }
    konst extensionMethodParameters = extensionMethod.getParameters()
    if (extensionMethodParameters[0].name != "\$this\$bar")
        return "wrong name on extension receiver parameter: ${extensionMethodParameters[0].name}"

    return "OK"
}
