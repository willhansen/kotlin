// SKIP_TXT

enum class A(konst z: Any) {
    Y(<!UNINITIALIZED_ENUM_COMPANION, UNINITIALIZED_VARIABLE!>x<!>);

    companion object {
        konst x = A.Y.ordinal
    }
}

enum class B(konst z: Any) {
    Y(<!UNINITIALIZED_ENUM_COMPANION!>B<!>.x);

    companion object {
        konst x = B.Y.ordinal
    }
}
