// FIR_IDENTICAL
// !CHECK_TYPE

import kotlin.reflect.KFunction0

class A
class B

fun A.ext() {
    konst x = ::A
    konst y = ::B

    checkSubtype<KFunction0<A>>(x)
    checkSubtype<KFunction0<B>>(y)
}
