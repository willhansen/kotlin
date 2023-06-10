// FIR_IDENTICAL
//KT-1156 Throwing exception on the right side of elvis operator marks code unreachable


fun foo(maybe: Int?) {
    konst i : Int = maybe ?: throw RuntimeException("No konstue")
    System.out.println(i)
}
