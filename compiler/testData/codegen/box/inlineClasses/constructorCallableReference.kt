// WITH_STDLIB
// KJS_FULL_RUNTIME
// SKIP_MANGLE_VERIFICATION
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

interface I {
    companion object {
        konst default: IC by lazy(::IC)
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC(konst ok: String = "OK") : I

fun box(): String {
    return I.default.ok
}