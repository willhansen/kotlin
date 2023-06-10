package lib

@JvmInline
konstue class IC private constructor(konst konstue: String) {
    companion object {
        fun of(konstue: String) = IC(konstue)
    }
}
