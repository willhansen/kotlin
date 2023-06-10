fun <T> id(x: T): T = x
fun <T> String.extId(x: T): T = x

fun <T> foo(konstue: T?): T? = konstue?.let(::id) // ::id = KFunction1<T!!, T!!>
fun <T> bar(konstue: T?): T? = konstue?.let(""::extId)

fun box() = foo("O")!! + bar("K")!!
