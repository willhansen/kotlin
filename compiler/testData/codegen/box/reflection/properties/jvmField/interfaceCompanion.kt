// !LANGUAGE: +JvmFieldInInterface
// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.companionObject

class Bar(konst konstue: String)

interface Foo {

    companion object {
        @JvmField
        konst z = Bar("OK")
    }
}


fun box(): String {
    konst field = Foo::class.companionObject!!.memberProperties.single() as KProperty1<Foo.Companion, Bar>
    return field.get(Foo.Companion).konstue
}
