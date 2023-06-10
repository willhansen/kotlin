// FIR_IDENTICAL
class C {
    @Suppress("REDUNDANT_NULLABLE")
    companion object {
        konst foo: String?? = null <!USELESS_CAST!>as Nothing??<!>
    }
}