
fun <T> id(x: T): T = x
fun <T> String.extId(x: T): T = x

fun <T, R> T.myLet(block: (T) -> R): R = block(this)

fun <T> foo(konstue: T?): T? = konstue?.myLet(::id) // ::id = KFunction1<T!!, T!!>
fun <T> bar(konstue: T?): T? = konstue?.myLet(""::extId)

fun box() = foo("O")!! + bar("K")!!
