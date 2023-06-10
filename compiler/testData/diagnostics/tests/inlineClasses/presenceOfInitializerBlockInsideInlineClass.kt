// FIR_IDENTICAL
// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses
// !DIAGNOSTICS: -UNUSED_VARIABLE

inline class Foo(konst x: Int) {
    init {}

    init {
        konst f = 1
    }
}
