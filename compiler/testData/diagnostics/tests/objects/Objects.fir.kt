// COMPARE_WITH_LIGHT_TREE
package toplevelObjectDeclarations

open class Foo(y: Int) {
    open fun foo(): Int = 1
}

class T : <!NO_VALUE_FOR_PARAMETER, SUPERTYPE_NOT_INITIALIZED!>Foo<!> {}

<!NO_VALUE_FOR_PARAMETER{LT}!>object A : <!NO_VALUE_FOR_PARAMETER{PSI}, SUPERTYPE_NOT_INITIALIZED!>Foo<!> {
    konst x: Int = 2

    fun test(): Int {
        return x + foo()
    }
}<!>

object B : <!SINGLETON_IN_SUPERTYPE!>A<!> {}

konst c = <!NO_VALUE_FOR_PARAMETER{LT}!>object : <!NO_VALUE_FOR_PARAMETER{PSI}, SUPERTYPE_NOT_INITIALIZED!>Foo<!> {}<!>

konst x = A.foo()

konst y = object : Foo(x) {
    init {
        x + 12
    }

    override fun foo(): Int = 1
}

konst z = y.foo()
