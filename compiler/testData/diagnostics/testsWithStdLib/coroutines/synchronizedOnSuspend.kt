// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER
// SKIP_TXT

<!SYNCHRONIZED_ON_SUSPEND_ERROR!>@Synchronized<!>
suspend fun foo(f: () -> Unit): Unit = f()

fun builder(c: suspend () -> Unit) {}

konst a: suspend () -> Unit
    get() = <!SYNCHRONIZED_ON_SUSPEND_ERROR!>@Synchronized<!> {}
konst b: suspend () -> Unit = <!SYNCHRONIZED_ON_SUSPEND_ERROR!>@Synchronized<!> {}
konst c = builder (<!SYNCHRONIZED_ON_SUSPEND_ERROR!>@Synchronized<!> {})
konst d = suspend <!SYNCHRONIZED_ON_SUSPEND_ERROR!>@Synchronized<!> {}
konst e = <!WRONG_ANNOTATION_TARGET!>@Synchronized<!> suspend {}

fun test() {
    <!SYNCHRONIZED_ON_SUSPEND_ERROR!>@Synchronized<!>
    suspend fun foo(f: () -> Unit): Unit = f()

    konst b: suspend () -> Unit = <!SYNCHRONIZED_ON_SUSPEND_ERROR!>@Synchronized<!> {}
    konst c = builder (<!SYNCHRONIZED_ON_SUSPEND_ERROR!>@Synchronized<!> {})
    konst d = suspend <!SYNCHRONIZED_ON_SUSPEND_ERROR!>@Synchronized<!> {}
    konst e = <!WRONG_ANNOTATION_TARGET!>@Synchronized<!> suspend {}
}
