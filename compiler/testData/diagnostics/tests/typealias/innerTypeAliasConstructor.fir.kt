// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -TOPLEVEL_TYPEALIASES_ONLY

class Pair<X, Y>(konst x: X, konst y: Y)

class C<T> {
    typealias P = Pair<T, T>
    typealias P1<X> = Pair<X, T>
    typealias P2<Y> = Pair<T, Y>
}

// C<...>.P[<...>]() syntax doesn't work due to the way qualified expressions are resolved now.
// This restriction can be removed later.
konst test0 = C<Int>.P(<!ARGUMENT_TYPE_MISMATCH!>1<!>, <!ARGUMENT_TYPE_MISMATCH!>1<!>)
konst test1 = C<Int>.P1<String>("", <!ARGUMENT_TYPE_MISMATCH!>1<!>)
konst test2 = C<Int>.P2<String>(<!ARGUMENT_TYPE_MISMATCH!>1<!>, "")
konst test3 = C<Int>.P1("", <!ARGUMENT_TYPE_MISMATCH!>1<!>)
konst test4 = C<Int>.P2(<!ARGUMENT_TYPE_MISMATCH!>1<!>, "")

// C.P() syntax could work if we add captured type parameters as type variables in a constraint system for corresponding call.
// However, this should be consistent with inner classes capturing type parameters.
konst test5 = C.P(<!ARGUMENT_TYPE_MISMATCH!>1<!>, <!ARGUMENT_TYPE_MISMATCH!>1<!>)
konst test6 = C.P1("", <!ARGUMENT_TYPE_MISMATCH!>1<!>)
konst test7 = C.P2(<!ARGUMENT_TYPE_MISMATCH!>1<!>, "")
