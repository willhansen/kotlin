// FIR_IDENTICAL
//KT-897 Don't allow assignment to a property before it is defined

package kt897

class A() {
    init {
        <!INITIALIZATION_BEFORE_DECLARATION!>i<!> = 11
    }
    konst i : Int? = null // must be an error

    init {
        <!INITIALIZATION_BEFORE_DECLARATION!>j<!> = 1
    }
    var j : Int = 2

    init {
        <!INITIALIZATION_BEFORE_DECLARATION!>k<!> = 3
    }
    konst k : Int
}
