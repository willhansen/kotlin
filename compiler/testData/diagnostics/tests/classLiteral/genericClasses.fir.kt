// !DIAGNOSTICS: -UNUSED_VARIABLE

class A<T> {
    class Nested<N>

    inner class Inner<I>
}

konst a1 = A::class
konst a2 = <!CLASS_LITERAL_LHS_NOT_A_CLASS!>A<*>::class<!>
konst a3 = <!CLASS_LITERAL_LHS_NOT_A_CLASS!>A<String>::class<!>
konst a4 = <!CLASS_LITERAL_LHS_NOT_A_CLASS!>A<out String?>::class<!>

konst n1 = A.Nested::class
konst n2 = <!CLASS_LITERAL_LHS_NOT_A_CLASS!>A.Nested<*>::class<!>

konst i1 = A.Inner::class
konst i2 = <!CLASS_LITERAL_LHS_NOT_A_CLASS!>A<*>.Inner<*>::class<!>
konst i3 = <!CLASS_LITERAL_LHS_NOT_A_CLASS!>A<Int>.Inner<CharSequence>::class<!>

konst m1 = Map::class
konst m2 = <!CLASS_LITERAL_LHS_NOT_A_CLASS!>Map<Int, *>::class<!>
konst m3 = Map.Entry::class

konst b1 = Int::class
konst b2 = Nothing::class
