// FIR_IDENTICAL
external interface I {
    <!NON_ABSTRACT_MEMBER_OF_EXTERNAL_INTERFACE!>fun foo(): Unit<!> = definedExternally

    konst a: Int?
        get() = definedExternally

    var b: String?
        get() = definedExternally
        set(konstue) = definedExternally

    <!NON_ABSTRACT_MEMBER_OF_EXTERNAL_INTERFACE!>konst c: Int<!>
        get() = definedExternally

    <!NON_ABSTRACT_MEMBER_OF_EXTERNAL_INTERFACE!>var d: String<!>
        get() = definedExternally
        set(konstue) = definedExternally

    var e: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
}