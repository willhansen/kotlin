// !SKIP_JAVAC
// !LANGUAGE: +InlineClasses
// ALLOW_KOTLIN_PACKAGE
// !DIAGNOSTICS: -UNUSED_PARAMETER

package kotlin.jvm

annotation class JvmInline

@JvmInline
konstue class A0(konst x: Int)

@JvmInline
<!ABSENCE_OF_PRIMARY_CONSTRUCTOR_FOR_VALUE_CLASS!>konstue<!> class A1
@JvmInline
konstue class A2<!INLINE_CLASS_CONSTRUCTOR_WRONG_PARAMETERS_SIZE!>()<!>
@JvmInline
konstue class A3(<!VALUE_CLASS_CONSTRUCTOR_NOT_FINAL_READ_ONLY_PARAMETER!>x: Int<!>)
@JvmInline
konstue class A4(<!VALUE_CLASS_CONSTRUCTOR_NOT_FINAL_READ_ONLY_PARAMETER!>var x: Int<!>)
@JvmInline
konstue class A5<!INLINE_CLASS_CONSTRUCTOR_WRONG_PARAMETERS_SIZE!>(konst x: Int, konst y: Int)<!>
@JvmInline
konstue class A6<!INLINE_CLASS_CONSTRUCTOR_WRONG_PARAMETERS_SIZE!>(x: Int, konst y: Int)<!>
@JvmInline
konstue class A7(<!VALUE_CLASS_CONSTRUCTOR_NOT_FINAL_READ_ONLY_PARAMETER!>vararg konst x: Int<!>)
@JvmInline
konstue class A8(<!VALUE_CLASS_CONSTRUCTOR_NOT_FINAL_READ_ONLY_PARAMETER!><!NON_FINAL_MEMBER_IN_FINAL_CLASS!>open<!> konst x: Int<!>)
@JvmInline
konstue class A9(final konst x: Int)

class B1 {
    companion object {
        @JvmInline
        konstue class C1(konst x: Int)
    }

    @JvmInline
    konstue class C2(konst x: Int)
}

object B2 {
    @JvmInline
    konstue class C3(konst x: Int)
}

@JvmInline
final konstue class D0(konst x: Int)
<!VALUE_CLASS_NOT_FINAL!>open<!> <!VALUE_CLASS_WITHOUT_JVM_INLINE_ANNOTATION!>konstue<!> class D1(konst x: Int)
<!VALUE_CLASS_NOT_FINAL!>abstract<!> <!VALUE_CLASS_WITHOUT_JVM_INLINE_ANNOTATION!>konstue<!> class D2(konst x: Int)
<!VALUE_CLASS_NOT_FINAL!>sealed<!> <!VALUE_CLASS_WITHOUT_JVM_INLINE_ANNOTATION!>konstue<!> class D3(konst x: Int)

<!INCOMPATIBLE_MODIFIERS, VALUE_CLASS_WITHOUT_JVM_INLINE_ANNOTATION!>konstue<!> <!INCOMPATIBLE_MODIFIERS!>data<!> class <!CONFLICTING_JVM_DECLARATIONS, CONFLICTING_JVM_DECLARATIONS, CONFLICTING_JVM_DECLARATIONS!>D4(konst x: String)<!>
