enum class A(konst v: A) {
    A1(<!UNINITIALIZED_ENUM_ENTRY!>A2<!>),
    A2(A1),
    A3(<!UNINITIALIZED_ENUM_ENTRY!>A3<!>)
}

enum class D(konst x: Int) {
    D1(<!UNINITIALIZED_ENUM_ENTRY!>D2<!>.x),
    D2(D1.x)
}

enum class E(konst v: Int) {
    // KT-11769 related: there is no predictable initialization order for enum entry with non-companion object
    E1(Nested.COPY);

    object Nested {
        konst COPY = E1.v
    }
}
// From KT-13322: cross reference should not be reported here
object Object1 {
    konst y: Any = Object2.z
    object Object2 {
        konst z: Any = Object1.y
    }
}

// From KT-6054
enum class MyEnum {
    A, B;
    konst x = when(this) {
        <!UNINITIALIZED_ENUM_ENTRY!>A<!> -> 1
        <!UNINITIALIZED_ENUM_ENTRY!>B<!> -> 2
        else -> 3
    }
}
