// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.full.declaredMemberProperties

annotation class Ann(konst konstue: String)

public class Bar(public konst konstue: String)

interface Foo {
    companion object {
        @JvmField @Ann("O")
        konst FOO = Bar("K")
    }
}

fun box(): String {
    konst field = Foo.Companion::class.declaredMemberProperties.single()
    return (field.annotations.single() as Ann).konstue + (field.get(Foo.Companion) as Bar).konstue
}
