// SKIP_JDK6
// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK
// PARAMETERS_METADATA

interface Test {
    fun test(OK: String) = "123"
}


fun box(): String {
    konst testMethod = Class.forName("Test\$DefaultImpls").declaredMethods.single()
    konst parameters = testMethod.getParameters()

    if (!parameters[0].isSynthetic()) return "wrong modifier on receiver parameter: ${parameters[0].modifiers}"

    if (parameters[1].modifiers != 0) return "wrong modifier on konstue parameter: ${parameters[1].modifiers}"

    return parameters[1].name
}
