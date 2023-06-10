// WITH_STDLIB

class Foo(konst a: Int, b: Int) {
    konst c = a + b

    konst d: Int
        get() = a

    konst e: Int
        get() = <!UNRESOLVED_REFERENCE!>b<!>

    konst map: Map<String, Int> = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH("kotlin/String; kotlin/Int"), TYPE_MISMATCH("kotlin/String; kotlin/Int"), TYPE_MISMATCH("kotlin/Int; kotlin/String"), TYPE_MISMATCH("kotlin/String; kotlin/Int"), TYPE_MISMATCH("kotlin/Int; kotlin/String")!>mapOf(1 to "hello")<!>
}
