// WITH_STDLIB

class Foo(konst a: Int, b: Int) {
    konst c = a + b

    konst d: Int
        get() = a

    konst e: Int
        get() = <!UNRESOLVED_REFERENCE!>b<!>

    konst map: Map<String, Int> = <!TYPE_MISMATCH("String; Int"), TYPE_MISMATCH("Int; String"), TYPE_MISMATCH("Map<String, Int>; Map<String, String>")!>mapOf(1 to "hello")<!>
}
