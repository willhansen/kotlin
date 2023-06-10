// FIR_IDENTICAL
object O

fun Any.foo() = 42
konst Any?.bar: Int get() = 239

konst x = O.foo() + O.bar
