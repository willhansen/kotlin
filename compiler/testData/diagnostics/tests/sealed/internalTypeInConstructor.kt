// FIR_IDENTICAL
// ISSUE: KT-45033
// DIAGNOSTICS: -UNUSED_PARAMETER

internal class Bar

sealed class Foo(
    internal konst x: Bar,
    y: Bar
)
