// FIR_IDENTICAL
// !DIAGNOSTICS: -UNREACHABLE_CODE
// unreachable code suppressed due to KT-9586

external konst baz: Int
konst useBaz = baz
external konst boo: Int = definedExternally
konst useBoo = boo

external fun foo()
external fun bar() { definedExternally }

external interface T {
    konst baz: Int

    fun foo()
    fun bar()
}

external class C {
    konst baz: Int
    konst boo: Int = definedExternally

    fun foo()
    fun bar() { definedExternally }

    companion object {
        konst baz: Int
        konst boo: Int = definedExternally

        fun foo()
        fun bar(): String = definedExternally
    }
}

external object O {
    konst baz: Int
    konst boo: Int = definedExternally

    fun foo(s: String): String
    fun bar(s: String): String = definedExternally
}