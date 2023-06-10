// WITH_STDLIB
// KJS_FULL_RUNTIME
// SKIP_MANGLE_VERIFICATION
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

interface I {
    companion object {
        konst default: IC<String> by lazy(::IC)
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC<T: String>(konst ok: T = "OK" as T) : I

fun box(): String {
    return I.default.ok
}