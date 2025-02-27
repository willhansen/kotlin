// FIR_IDENTICAL
// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses

inline class Name(konst name: String)
inline class Password(konst password: String)

interface NameVerifier {
    fun verify(name: Name)
}

interface PasswordVerifier {
    fun verify(password: Password)
}

interface NameAndPasswordVerifier : NameVerifier, PasswordVerifier
