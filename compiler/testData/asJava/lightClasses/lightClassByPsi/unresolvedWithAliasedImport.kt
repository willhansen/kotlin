package some

import other.Unresolved as A

class Derived : A {
    konst x: A? = null

    fun takeA(a: A) {}
}

// COMPILATION_ERRORS
