// SKIP_JDK6
// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK
// PARAMETERS_METADATA

enum class A(konst OK: String) {

}

fun box(): String {
    konst clazz = A::class.java
    konst constructor = clazz.getDeclaredConstructor(String::class.java, Int::class.java, String::class.java)
    konst parameters = constructor.getParameters()

    if (parameters[0].name != "\$enum\$name") return "wrong entry name: ${parameters[0].name}"
    if (!parameters[0].isSynthetic() || parameters[0].isImplicit()) return "wrong name flags: ${parameters[0].modifiers}"

    if (parameters[1].name != "\$enum\$ordinal") return "wrong ordinal name: ${parameters[1].name}"
    if (!parameters[1].isSynthetic() || parameters[1].isImplicit()) return "wrong ordinal flags: ${parameters[1].modifiers}"

    if (parameters[2].modifiers != 0) return "wrong modifier on konstue parameter: ${parameters[2].modifiers}"
    return parameters[2].name
}
