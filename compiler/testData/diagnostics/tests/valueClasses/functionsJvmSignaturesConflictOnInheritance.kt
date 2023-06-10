// FIR_IDENTICAL
// !SKIP_JAVAC
// FIR_IDENTICAL
// !LANGUAGE: +InlineClasses
// ALLOW_KOTLIN_PACKAGE

package kotlin.jvm

annotation class JvmInline

@JvmInline
konstue class Name(konst name: String)
@JvmInline
konstue class Password(konst password: String)

interface NameVerifier {
    fun verify(name: Name)
}

interface PasswordVerifier {
    fun verify(password: Password)
}

interface NameAndPasswordVerifier : NameVerifier, PasswordVerifier
