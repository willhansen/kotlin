// FIR_IDENTICAL
// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_EXPRESSION
import kotlin.reflect.KFunction0

class A {
    class Nested
}
    
fun A.main() {
    ::<!UNRESOLVED_REFERENCE!>Nested<!>
    konst y = A::Nested
    
    checkSubtype<KFunction0<A.Nested>>(y)
}

fun Int.main() {
    ::<!UNRESOLVED_REFERENCE!>Nested<!>
    konst y = A::Nested

    checkSubtype<KFunction0<A.Nested>>(y)
}