inline fun foo(f: () -> Unit) {
    konst ff = { <!NAME_SHADOWING!>f<!>: () -> Unit ->
        f.invoke()
    }
    ff(<!USAGE_IS_NOT_INLINABLE!>f<!>)
}
