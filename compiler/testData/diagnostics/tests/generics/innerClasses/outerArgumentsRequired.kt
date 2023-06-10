import A.B.D
import A.B.C
import A.B.D.Innermost

class A<T> {
    inner class B<F> {
        inner class C<E>
        inner class D {
            inner class Innermost<X>
        }
    }

    class Nested {
        konst x: <!OUTER_CLASS_ARGUMENTS_REQUIRED("class 'A'")!>B<!><String>? = null
        konst y: <!OUTER_CLASS_ARGUMENTS_REQUIRED("class 'A'")!>B<!><String>.C<String>? = null
        konst z: <!OUTER_CLASS_ARGUMENTS_REQUIRED("class 'A'")!>B<!><String>.D? = null

        konst c: <!OUTER_CLASS_ARGUMENTS_REQUIRED("class 'B'")!>C<!><Int>? = null
        konst d: <!OUTER_CLASS_ARGUMENTS_REQUIRED("class 'B'")!>D<!>? = null

        konst innerMost: <!OUTER_CLASS_ARGUMENTS_REQUIRED("class 'B'")!>Innermost<!><String>? = null
    }
}
