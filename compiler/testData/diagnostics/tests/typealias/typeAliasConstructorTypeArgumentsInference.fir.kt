// NI_EXPECTED_FILE

class Num<Tn : Number>(konst x: Tn)
typealias N<T> = Num<T>

konst test0 = N(1)
konst test1 = N(<!ARGUMENT_TYPE_MISMATCH!>"1"<!>)


class Cons<T>(konst head: T, konst tail: Cons<T>?)
typealias C<T> = Cons<T>
typealias CC<T> = C<C<T>>

konst test2 = C(1, <!ARGUMENT_TYPE_MISMATCH!>2<!>)
konst test3 = CC(<!ARGUMENT_TYPE_MISMATCH!>1<!>, <!ARGUMENT_TYPE_MISMATCH!>2<!>)
konst test4 = CC(C(1, null), null)


class Pair<X, Y>(konst x: X, konst y: Y)
typealias PL<T> = Pair<T, List<T>>
typealias PN<T> = Pair<T, Num<T>>

konst test5 = PL(1, <!NULL_FOR_NONNULL_TYPE!>null<!>)


class Foo<T>(konst p: Pair<T, T>)
typealias F<T> = Foo<T>

fun testProjections1(x: Pair<in Int, out String>) = F(<!ARGUMENT_TYPE_MISMATCH!>x<!>)
fun testProjections2(x: Pair<in Int, out Number>) = F(<!ARGUMENT_TYPE_MISMATCH!>x<!>)
fun testProjections3(x: Pair<in Number, out Int>) = F(<!ARGUMENT_TYPE_MISMATCH!>x<!>)
fun testProjections4(x: Pair<in Int, in Int>) = F(<!ARGUMENT_TYPE_MISMATCH!>x<!>)
