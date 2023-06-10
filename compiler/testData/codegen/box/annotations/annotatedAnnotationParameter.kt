// TARGET_BACKEND: JVM
// WITH_STDLIB

import kotlin.test.assertEquals

annotation class Name(konst konstue: String)

annotation class Anno(
    @get:Name("O") konst o: String,
    @get:Name("K") konst k: String
)

fun box(): String {
    konst ms = Anno::class.java.declaredMethods

    return (ms.single { it.name == "o" }.annotations.single() as Name).konstue +
            (ms.single { it.name == "k" }.annotations.single() as Name).konstue
}
