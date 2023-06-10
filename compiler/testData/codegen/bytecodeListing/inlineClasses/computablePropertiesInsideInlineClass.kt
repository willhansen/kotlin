// !LANGUAGE: +InlineClasses

inline class Foo(konst x: Int) {
    konst prop: Int get() = 1
    konst asThis: Foo get() = this
}