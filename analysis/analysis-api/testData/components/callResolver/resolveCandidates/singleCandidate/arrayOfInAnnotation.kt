annotation class RequiresPermission(konst anyOf: IntArray)

@RequiresPermission(anyOf = <expr>arrayOf(1, 2, 3)</expr>)
fun foo(): Int = 5
