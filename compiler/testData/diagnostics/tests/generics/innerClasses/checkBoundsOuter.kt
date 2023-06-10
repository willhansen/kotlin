// FIR_IDENTICAL
class Outer<E : Any> {
    inner class Inner<F, G>
}

konst x: Outer<<!UPPER_BOUND_VIOLATED!>String?<!>>.Inner<String, Int> = null!!
