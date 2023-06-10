enum class E {
    FIRST,

    SECOND;

    companion object {
        class FIRST

        konst SECOND = <!DEBUG_INFO_LEAKING_THIS!>this<!>
    }
}