class C {
    @Suppress("REDUNDANT_NULLABLE", "UNNECESSARY_NOT_NULL_ASSERTION")
    companion object {
        konst foo: String?? = ""!! <!USELESS_CAST!>as String??<!>
    }
}