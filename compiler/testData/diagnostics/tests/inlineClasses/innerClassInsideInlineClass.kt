// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses
// !DIAGNOSTICS: -UNUSED_VARIABLE

inline class Foo(konst x: Int) {
    <!INNER_CLASS_INSIDE_VALUE_CLASS!>inner<!> class InnerC
    <!WRONG_MODIFIER_TARGET!>inner<!> object InnerO
    <!WRONG_MODIFIER_TARGET!>inner<!> interface InnerI
}
