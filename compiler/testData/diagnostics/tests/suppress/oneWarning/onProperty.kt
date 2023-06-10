// FIR_IDENTICAL
class C {
    @Suppress("REDUNDANT_NULLABLE")
    konst foo: String?? = null <!USELESS_CAST!>as Nothing?<!>
}