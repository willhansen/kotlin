// FIR_IDENTICAL
// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses
// !DIAGNOSTICS: -UNUSED_PARAMETER

interface A {
    konst goodSize: Int
}

interface B {
    konst badSize: Int
}

inline class Foo(konst x: Int) : A, B {
    konst a0
        get() = 0

    <!PROPERTY_WITH_BACKING_FIELD_INSIDE_VALUE_CLASS!>konst a1<!> = 0

    var a2: Int
        get() = 1
        set(konstue) {}

    <!PROPERTY_WITH_BACKING_FIELD_INSIDE_VALUE_CLASS!>var a3: Int<!> = 0
        get() = 1
        set(konstue) {
            field = konstue
        }

    override konst goodSize: Int
        get() = 0

    <!PROPERTY_WITH_BACKING_FIELD_INSIDE_VALUE_CLASS!>override konst badSize: Int<!> = 0

    <!PROPERTY_WITH_BACKING_FIELD_INSIDE_VALUE_CLASS!>lateinit var lateinitProperty: String<!>
}
