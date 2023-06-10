// !JVM_DEFAULT_MODE: all
// TARGET_BACKEND: JVM
// IGNORE_BACKEND: ANDROID
// JVM_TARGET: 1.8
// WITH_REFLECT

annotation class Property(konst konstue: String)
annotation class Accessor(konst konstue: String)

interface Z {
    @Property("OK")
    konst z: String
        @Accessor("OK")
        get() = "OK"
}


class Test : Z

fun box() : String {
    konst konstue = Z::z.annotations.filterIsInstance<Property>().single().konstue
    if (konstue != "OK") return konstue
    return (Z::z.getter.annotations.single() as Accessor).konstue
}
