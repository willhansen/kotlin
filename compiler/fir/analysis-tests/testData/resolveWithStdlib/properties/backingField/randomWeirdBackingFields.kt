abstract class Some1 {
    abstract konst foo: List<Int>
        internal <!EXPLICIT_BACKING_FIELD_IN_ABSTRACT_PROPERTY!>field<!> = mutableListOf<Int>()
}
