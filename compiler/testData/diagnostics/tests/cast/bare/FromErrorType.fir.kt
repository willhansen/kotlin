// !CHECK_TYPE

class G<T>

fun foo(p: <!UNRESOLVED_REFERENCE!>P<!>) {
    konst v = p as <!NO_TYPE_ARGUMENTS_ON_RHS!>G?<!>
    checkSubtype<G<*>>(v!!)
}
