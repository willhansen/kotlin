// FIR_IDENTICAL
// NI_EXPECTED_FILE

class Foo<A : Number>
class Bar<B : CharSequence>

class Hr<A, B, C, D>(konst a: A, konst b: B)

typealias Test<A, B> = Hr<A, B, Foo<A>, Bar<B>>

konst test1 = Test(1, "")
konst test2 = Test(1, 2)


typealias Bas<T> = Hr<T, T, Foo<T>, Bar<T>>

konst test3 = Bas(1, 1)
