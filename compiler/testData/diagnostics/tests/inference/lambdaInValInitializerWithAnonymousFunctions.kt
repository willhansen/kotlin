typealias SuspendFn = suspend () -> Unit

konst test1f: suspend () -> Unit = <!TYPE_MISMATCH!>fun () {}<!>
konst test2f: suspend Any.() -> Unit = <!TYPE_MISMATCH!>fun Any.() {}<!>

// This is a bug in the old inference and should be fixed in new inference
// see "Fix anonymous function literals handling in type checker" for more details
konst test3f: suspend Any.(Int) -> Int = <!TYPE_MISMATCH!>fun (k: Int) = k + 1<!>
konst test4f: SuspendFn = <!TYPE_MISMATCH!>fun Any.() {}<!>