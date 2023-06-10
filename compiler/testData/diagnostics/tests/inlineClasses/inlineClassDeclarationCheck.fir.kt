// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses
// !DIAGNOSTICS: -UNUSED_PARAMETER

inline class A0(konst x: Int)

<!ABSENCE_OF_PRIMARY_CONSTRUCTOR_FOR_VALUE_CLASS!>inline<!> class A1
inline class A2<!INLINE_CLASS_CONSTRUCTOR_WRONG_PARAMETERS_SIZE!>()<!>
inline class A3(<!VALUE_CLASS_CONSTRUCTOR_NOT_FINAL_READ_ONLY_PARAMETER!>x: Int<!>)
inline class A4(<!VALUE_CLASS_CONSTRUCTOR_NOT_FINAL_READ_ONLY_PARAMETER!>var x: Int<!>)
inline class A5<!INLINE_CLASS_CONSTRUCTOR_WRONG_PARAMETERS_SIZE!>(konst x: Int, konst y: Int)<!>
inline class A6<!INLINE_CLASS_CONSTRUCTOR_WRONG_PARAMETERS_SIZE!>(x: Int, konst y: Int)<!>
inline class A7(<!VALUE_CLASS_CONSTRUCTOR_NOT_FINAL_READ_ONLY_PARAMETER!>vararg konst x: Int<!>)
inline class A8(<!VALUE_CLASS_CONSTRUCTOR_NOT_FINAL_READ_ONLY_PARAMETER!><!NON_FINAL_MEMBER_IN_FINAL_CLASS!>open<!> konst x: Int<!>)
inline class A9(final konst x: Int)

class B1 {
    companion object {
        inline class C1(konst x: Int)
        <!WRONG_MODIFIER_CONTAINING_DECLARATION!>inner<!> <!VALUE_CLASS_NOT_TOP_LEVEL!>inline<!> class C11(konst x: Int)
    }

    inline class C2(konst x: Int)
    inner <!VALUE_CLASS_NOT_TOP_LEVEL!>inline<!> class C21(konst x: Int)
}

object B2 {
    inline class C3(konst x: Int)
    <!WRONG_MODIFIER_CONTAINING_DECLARATION!>inner<!> <!VALUE_CLASS_NOT_TOP_LEVEL!>inline<!> class C31(konst x: Int)
}

fun foo() {
    <!VALUE_CLASS_NOT_TOP_LEVEL, WRONG_MODIFIER_TARGET!>inline<!> class C4(konst x: Int)
}

final inline class D0(konst x: Int)
<!VALUE_CLASS_NOT_FINAL!>open<!> inline class D1(konst x: Int)
<!VALUE_CLASS_NOT_FINAL!>abstract<!> inline class D2(konst x: Int)
<!VALUE_CLASS_NOT_FINAL!>sealed<!> inline class D3(konst x: Int)

<!INCOMPATIBLE_MODIFIERS!>inline<!> <!INCOMPATIBLE_MODIFIERS!>data<!> class D4(konst x: String)
