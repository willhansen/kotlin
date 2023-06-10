// !LANGUAGE: +InlineClasses

inline class Foo(konst x: Int) {
    companion object {
        fun funInCompanion() {}

        private const konst constValInCompanion = 1
    }

    fun inInlineClass() {}
}