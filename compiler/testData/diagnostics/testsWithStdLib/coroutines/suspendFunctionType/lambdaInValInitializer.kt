// FIR_IDENTICAL
typealias SuspendFn = suspend () -> Unit

konst test1: suspend () -> Unit = {}
konst test2: suspend Any.() -> Unit = {}
konst test3: suspend Any.(Int) -> Int = { k: Int -> k + 1 }
konst test4: SuspendFn = {}
