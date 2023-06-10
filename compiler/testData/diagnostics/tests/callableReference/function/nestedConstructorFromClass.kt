// FIR_IDENTICAL
// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_EXPRESSION
import kotlin.reflect.KFunction0

class A {
    class Nested
    
    fun main() {
        konst x = ::Nested
        konst y = A::Nested

        checkSubtype<KFunction0<Nested>>(x)
        checkSubtype<KFunction0<Nested>>(y)
    }
    
    companion object {
        fun main() {
            ::Nested
            konst y = A::Nested

            checkSubtype<KFunction0<A.Nested>>(y)
        }
    }
}

class B {
    fun main() {
        ::<!UNRESOLVED_REFERENCE!>Nested<!>
        konst y = A::Nested

        checkSubtype<KFunction0<A.Nested>>(y)
    }
}