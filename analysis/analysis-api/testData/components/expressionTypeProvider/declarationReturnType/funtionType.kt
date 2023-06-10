konst f1: () -> Unit = {}
konst f2 = {}
konst f3: String.() -> String = { this }
fun <T> f4(): Int.(T) -> String = { "" }
konst f5 = fun(x:Int) { return "$x" }
konst f6 = fun() { 56 }
konst f7: () -> Unit = fun() { f1() }