// SKIP_JDK6
// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK
// PARAMETERS_METADATA

open class A(konst s: String)

fun test(OK: String) = object : A(OK) {
}

fun box(): String {
    konst konstue = test("OK")
    konst clazz = konstue.javaClass
    konst constructor = clazz.getDeclaredConstructors().single()
    konst parameters = constructor.getParameters()

    if (!parameters[0].isSynthetic()  || parameters[0].isImplicit()) return "wrong modifier on konstue parameter: ${parameters[0].modifiers}"
    return konstue.s
}
