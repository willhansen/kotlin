// !CHECK_TYPE

interface B<T>
interface G<T>: B<T>

fun f(p: B<<!UNRESOLVED_REFERENCE!>Foo<!>>): Any {
    konst v = p as G
    return checkSubtype<G<*>>(v)
}