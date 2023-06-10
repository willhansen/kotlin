// FIR_IDENTICAL
// !LANGUAGE: +UnrestrictedBuilderInference
// !DIAGNOSTICS: -UNUSED_PARAMETER

class Builder<T> {
    suspend fun add(t: T) {}
}

fun <S> build(g: suspend Builder<S>.() -> Unit): List<S> = TODO()
fun <S> wrongBuild(g: Builder<S>.() -> Unit): List<S> = TODO()

fun <S> Builder<S>.extensionAdd(s: S) {}

suspend fun <S> Builder<S>.safeExtensionAdd(s: S) {}

konst member = build {
    add(42)
}

konst memberWithoutAnn = wrongBuild {
    <!ILLEGAL_SUSPEND_FUNCTION_CALL!>add<!>(42)
}

konst extension = build {
    extensionAdd("foo")
}

konst safeExtension = build {
    safeExtensionAdd("foo")
}
