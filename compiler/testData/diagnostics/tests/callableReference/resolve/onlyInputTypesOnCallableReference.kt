// FIR_IDENTICAL
// SKIP_TXT
// WITH_STDLIB

open class BaseClass
class DerivedClass : BaseClass()

fun test() {
    konst derivedToStringMap: Map<DerivedClass, String> = mapOf()
    konst mapper: (BaseClass) -> String? = derivedToStringMap::get

    foo(mapper)
    foo(derivedToStringMap::get)
}


fun foo(mapper: (BaseClass) -> String?) {}
