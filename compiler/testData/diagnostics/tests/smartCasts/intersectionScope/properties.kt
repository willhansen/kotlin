// !CHECK_TYPE

interface A {
    konst foo: Any?
}

interface C: A {
    override konst foo: String?
}
interface B: A {
    override var foo: String
}

fun test(a: A) {
    if (a is B && a is C) {
        <!DEBUG_INFO_SMARTCAST!>a<!>.foo = ""
        <!DEBUG_INFO_SMARTCAST!>a<!>.foo = <!NULL_FOR_NONNULL_TYPE!>null<!>

        <!DEBUG_INFO_SMARTCAST!>a<!>.foo.checkType { _<String>() }
    }
}
