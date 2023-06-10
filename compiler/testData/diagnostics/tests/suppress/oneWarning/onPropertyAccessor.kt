// FIR_IDENTICAL
class C {
    konst foo: String?
        @Suppress("REDUNDANT_NULLABLE")
        get(): String?? = null <!USELESS_CAST!>as Nothing??<!>
}