// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_ANONYMOUS_PARAMETER

fun <T> id(x: T) = x
fun <T> select(vararg x: T) = x[0]

konst x1 = select(id { this }, fun Int.() = this)
konst x2 = select(id { this + it.inv() }, fun Int.(x: Int) = this)
konst x3 = select(id { this.length + it.inv() }, fun String.(x: Int) = length)
