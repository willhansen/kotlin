// TARGET_BACKEND: JVM

// WITH_REFLECT

import kotlin.reflect.full.konstueParameters

@Retention(AnnotationRetention.RUNTIME)
annotation class Simple(konst konstue: String)

fun local(): Any {
    class A(@Simple("OK") konst z: String)
    return A("OK")
}

fun localCaptured(): Any {
    konst z  = 1
    class A(@Simple("K") konst z: String) {
        konst x = z
    }
    return A("K")
}

fun box(): String {
    return (local()::class.constructors.single().konstueParameters.single().annotations.single() as Simple).konstue
    //KT-25573
    //return (localCaptured()::class.constructors.single().konstueParameters.single().annotations.single() as Simple).konstue
}
