// FIR_IDENTICAL
// !LANGUAGE: +NestedClassesInAnnotations

annotation class Foo {
    class Nested

    <!WRONG_MODIFIER_CONTAINING_DECLARATION!>inner<!> class Inner

    enum class E { A, B }
    object O
    interface I
    annotation class Anno(konst e: E)

    companion object {
        konst x = 1
        const konst y = ""
    }


    <!ANNOTATION_CLASS_MEMBER!>constructor(s: Int) {}<!>
    <!ANNOTATION_CLASS_MEMBER!>init {}<!>
    <!ANNOTATION_CLASS_MEMBER!>fun function() {}<!>
    <!ANNOTATION_CLASS_MEMBER!>konst property get() = Unit<!>
}
