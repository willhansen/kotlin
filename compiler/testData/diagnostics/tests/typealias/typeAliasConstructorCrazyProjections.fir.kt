// NI_EXPECTED_FILE

class Bound<X, Y : X>(konst x: X, konst y: Y)
typealias B<X, Y> = Bound<X, Y>
typealias BOutIn<T> = Bound<out List<T>, in T>
typealias BInIn<T> = Bound<in List<T>, in T>

fun <T> listOf(): List<T> = null!!

// Unresolved reference is ok here:
// we can't create a substituted signature for type alias constructor
// since it has 'out' type projection in 'in' position.
konst test1 = BOutIn(<!ARGUMENT_TYPE_MISMATCH!>listOf()<!>, null!!)

konst test2 = BInIn(listOf(), null!!)
