// FIR_IDENTICAL
// !CHECK_TYPE

import kotlin.reflect.KFunction0

class A {
    fun main() {
        konst x = ::A

        checkSubtype<KFunction0<A>>(x)
    }
}

class SomeOtherClass {
    fun main() {
        konst x = ::A

        checkSubtype<KFunction0<A>>(x)
    }
}
