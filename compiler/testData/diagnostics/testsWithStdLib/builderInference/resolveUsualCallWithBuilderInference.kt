// FIR_IDENTICAL
// ALLOW_KOTLIN_PACKAGE
// !LANGUAGE: +UnrestrictedBuilderInference
// !DIAGNOSTICS: -UNUSED_PARAMETER
// FILE: annotation.kt

package kotlin

annotation class BuilderInference

// FILE: test.kt

class Builder<T> {
    fun add(t: T) {}
}

fun <S> build(g: Builder<S>.() -> Unit): List<S> = TODO()
fun <S> wrongBuild(g: Builder<S>.() -> Unit): List<S> = TODO()

fun <S> Builder<S>.extensionAdd(s: S) {}

fun <S> Builder<S>.safeExtensionAdd(s: S) {}

konst member = build {
    add(42)
}

konst memberWithoutAnn = wrongBuild {
    add(42)
}

konst extension = build {
    extensionAdd("foo")
}

konst safeExtension = build {
    safeExtensionAdd("foo")
}
