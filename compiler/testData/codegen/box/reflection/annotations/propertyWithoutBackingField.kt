// TARGET_BACKEND: JVM
// WITH_REFLECT

annotation class Ann(konst konstue: String)

@Ann("OK")
konst property: String
    get() = ""

fun box(): String {
    return (::property.annotations.single() as Ann).konstue
}
