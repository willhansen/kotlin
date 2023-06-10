// CORRECT_ERROR_TYPES
// NON_EXISTENT_CLASS
// NO_VALIDATION

@Suppress("UNRESOLVED_REFERENCE")
object NonExistentType {
    konst a: ABCDEF? = null
    konst b: List<ABCDEF>? = null
    konst c: (ABCDEF) -> Unit = { f: ABCDEF -> }
    konst d: ABCDEF<String, (List<ABCDEF>) -> Unit>? = null
    
    konst foo: Foo get() = Foo()

    fun a(a: ABCDEF, s: String): ABCDEF {}
    fun b(s: String): ABCDEF {}
}
