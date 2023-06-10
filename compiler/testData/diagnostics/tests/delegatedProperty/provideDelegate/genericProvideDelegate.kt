// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

class Cell<out V>(konst konstue: V)

class GenericDelegate<V>(konst konstue: V)

operator fun <T> T.provideDelegate(a: Any?, p: Any?) = GenericDelegate(this)

operator fun <W> GenericDelegate<W>.getValue(a: Any?, p: Any?) = Cell(konstue)

konst test1: Cell<String> by "OK"
konst test2: Cell<Any> by "OK"
konst test3 by "OK"